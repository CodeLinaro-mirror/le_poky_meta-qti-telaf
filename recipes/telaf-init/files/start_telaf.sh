#!/bin/sh
# Copyright (c) 2021 The Linux Foundation. All rights reserved.
#
# Redistribution and use in source and binary forms, with or without
# modification, are permitted provided that the following conditions are
# met:
#     * Redistributions of source code must retain the above copyright
#       notice, this list of conditions and the following disclaimer.
#     * Redistributions in binary form must reproduce the above
#       copyright notice, this list of conditions and the following
#       disclaimer in the documentation and/or other materials provided
#       with the distribution.
#     * Neither the name of The Linux Foundation nor the names of its
#       contributors may be used to endorse or promote products derived
#       from this software without specific prior written permission.
#
# THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESS OR IMPLIED
# WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
# MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT
# ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS
# BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
# CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
# SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
# BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
# WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
# OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN
# IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

# Changes from Qualcomm Innovation Center are provided under the following license:
# Copyright (c) 2023 Qualcomm Innovation Center, Inc. All rights reserved.
# SPDX-License-Identifier: BSD-3-Clause-Clear

# Mount TelAf partition and run it
if [ -e "/etc/telaf.env" ]; then
    source /etc/telaf.env
fi

MOUNTPOINT_TELAF="/mnt/legato"

umount_etc()
{
    umount -l /etc/ld.so.conf
    umount -l /etc/ld.so.cache
    umount -l /etc/hosts

    return ${TELAF_OK}
}

umount_telaf()
{
    umount -l /legato/apps
    umount -l /legato/systems/current
    umount -l /legato
    umount -l /mnt/legato
    umount -l /app

    return ${TELAF_OK}
}

IsProcessRunning()
{
   local processName="$1"
   ps -e -o comm= | grep -x "$processName" > /dev/null
}

WaitProcessToExit()
{
   local processName="$1"
   local timeOutCount="$2"

    while [ "$timeOutCount" -ne 0 ]; do
        if IsProcessRunning "$processName"; then
           usleep 1000000
       else
           break
       fi
       timeOutCount=$(expr "$timeOutCount" - 1)

       if [ "$timeOutCount" -eq 0 ]; then
           echo "Stop process'$processName' timeout"
       fi
   done
}

TelafGraceFullShutDown()
{
    if IsProcessRunning "startSystem";
    then
        nice -n -10 app "stopLegato" 2> /dev/null
    fi
}

CleanTelafRunningProcess()
{
    if IsProcessRunning "watchdog";
    then
        # Important: 'startSystem' needs to exit before 'watchdog', and the
        # 'watchdog' need to be killed with 'SIGTERM' to avoid watchdog bite.
        killall -9 startSystem
        WaitProcessToExit "startSystem" "10"

        nice -n -5 killall -TERM watchdog
        WaitProcessToExit "watchdog" "5"
    fi

    if IsProcessRunning "supervisor";
    then
        killall -9 supervisor
    fi

    ServiceList=$(ps -ef|grep telaf|grep taf |awk '{print $1}')
    if [ -n "$ServiceList" ]; then
        kill -9 ${ServiceList}
    fi

    CoreSvcList="logCtrlDaemon|configTree|serviceDirectory|updateDaemon|deviceManager"
    RemainCoreSvc=$(ps aux | grep -E "$CoreSvcList" | grep -v "grep" |awk '{print $1}')
    if [ -n "$RemainCoreSvc" ]; then
        kill -9 $RemainCoreSvc
    fi

    SERVICES_LIST=$(ps -ef|grep telaf|grep taf |awk '{print $4}')
    if [ -n "$SERVICES_LIST" ]; then
        # Since the above was using hard kill (-9), so the systemd will wait
        # these services "$SERVICES_LIST" to exit, here don't need to wait.
        echo TelAF: SERVICES_LIST:$SERVICES_LIST  > /dev/kmsg
    fi
}

if [ -e "${MOUNTPOINT_TELAF}/systems/current/read-only" ]
then
    export PATH=/legato/systems/current/bin:$PATH
    TELAF_START=/legato/systems/current/bin/start
else
    echo "Only support read-only TelAf!" > /dev/kmsg
    exit ${TELAF_ERR}
fi

# Remove /app if telaf is updated
FOTA_STATE_FILE="/data/le_fs/fotaState"
NAD_OTA_STATUS_FILE="/cache/recovery/nad_ota_status"
fota_success=6

if [ -e ${FOTA_STATE_FILE} ]; then
    echo "fota state file exists." > /dev/kmsg
    state=$(od -An -j 0 -N 4 -t d ${FOTA_STATE_FILE})
    if [ $state -eq $fota_success ]; then
        if [ -e ${NAD_OTA_STATUS_FILE} ]; then
            echo "nad ota status file exists." > /dev/kmsg
            for update_images in `cat ${NAD_OTA_STATUS_FILE}`
            do
                telaf_updated=$(echo $update_images | grep "telaf")
                if [ "$telaf_updated" != "" ]; then
                    echo "firmware update success withe telaf, clean up app volume."  > /dev/kmsg
                    rm -fr /app/*
                    break
                fi
            done
        fi
    fi
fi

case "$1" in
    start)
        echo "TelAf start sequence" > /dev/kmsg

        # Add boot KPI markers
        kpi_file="/sys/kernel/boot_kpi/kpi_values"
        if [[ -e "$kpi_file" ]]; then
            echo -n "L - TelAF is starting" > "$kpi_file"
        fi

        # These paths "/legato/systems/current" and "/legato" are needed during "telaf stop", in
        # this case, here need to clean it and remount for the coming telaf start.
        mount_point=$(mount | awk '{print $3}' | grep -Fx "/legato/systems/current")
        echo "mount_point=$mount_point"
        if [ -n "${mount_point}" ]; then
            umount -l /legato/systems/current
            sync
        fi

        mount_point=$(mount | awk '{print $3}' | grep -Fx "/legato")
        echo "mount_point=$mount_point"
        if [ -n "${mount_point}" ]; then
            umount -l /legato
            sync
        fi

        mount -o bind $MOUNTPOINT_TELAF /legato
        test -x $TELAF_START && $TELAF_START

        if [[ -e "$kpi_file" ]]; then
            echo -n "L - TelAF is started" > "$kpi_file"
        fi
        ;;

    stop)
        echo "TelAf stop sequence" > /dev/kmsg

        # Use graceful shut down to safely exit.
        #TelafGraceFullShutDown

        # Hard kill all the processes if not exit.
        CleanTelafRunningProcess

        # Umount "/legato/apps" which was mounted by supervisor
        umount -l /legato/apps
        umount_etc

        ;;

    umount)
        echo "TelAf umount sequence" > /dev/kmsg
        umount_telaf
        ;;

    *)
        echo "Only support start, stop and umount!"  > /dev/kmsg
        exit ${TELAF_ERR}
        ;;

esac

echo "Finished TelAf $1 Sequence" > /dev/kmsg
