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
#
# Copyright (c) 2022 Qualcomm Innovation Center, Inc. All rights reserved.
#
# Redistribution and use in source and binary forms, with or without
# modification, are permitted (subject to the limitations in the
# disclaimer below) provided that the following conditions are met:
#
#    * Redistributions of source code must retain the above copyright
#      notice, this list of conditions and the following disclaimer.
#
#    * Redistributions in binary form must reproduce the above
#      copyright notice, this list of conditions and the following
#      disclaimer in the documentation and/or other materials provided
#      with the distribution.
#
#    * Neither the name of Qualcomm Innovation Center, Inc. nor the names of its
#      contributors may be used to endorse or promote products derived
#      from this software without specific prior written permission.
#
# NO EXPRESS OR IMPLIED LICENSES TO ANY PARTY'S PATENT RIGHTS ARE
# GRANTED BY THIS LICENSE. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT
# HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED
# WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
# MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
# IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
# ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
# DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
# GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
# INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER
# IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
# OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN
# IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

# Mount TelAf partition
telaf_mount_point=/mnt/legato
telaf_app_mount_point=/app

# verity feature status for telaf
VERITY_ENV="/etc/verity.env"

IsTelAfExisted () {
    if [ -e "${telaf_mount_point}/start" ]; then
        echo "telaf partition has already been mounted" > /dev/kmsg
        exit 0;
    fi
}

WaitDevReady()
{
    local maxTrials=800
    local ret=0

    while [ ! "$1" "$2" ]; do
        usleep 10000
        maxTrials=$( echo $(( ${maxTrials} - 1 )) )
        if [ ${maxTrials} -eq 0 ]; then
            ret=1
            break
        fi
    done
    return ${ret}
}

GetVolumeID () {
    if [ "x${SLOT_SUFFIX}" == "x" ]; then
       SLOT_SUFFIX="_a"
    fi

    fs_ab_name=${1}${SLOT_SUFFIX}
    volcount=`cat /sys/class/ubi/ubi0/volumes_count`

    for vid in `seq 0 $volcount`; do
        name=`cat /sys/class/ubi/ubi0_$vid/name`
        if [ "$name" == "${1}" ] || [ "$name" == "$fs_ab_name" ]; then
            echo $vid
            break
        fi
    done
}

IsGPIOEnabled () {
    gpio_enable_status=`cat /proc/cmdline | awk -F'recoveryinfo_gpio=' '{print $2}' | awk '{print $1}' | tr -d '"'`
    if [ "$gpio_enable_status" == "-1" ]; then
        echo "gpio status not valid" > /dev/kmsg
    fi

    return ${gpio_enable_status}
}

SlotSwitchReboot () {
    local abctl_cmd="/usr/bin/nad-abctl"
    # Set image_set_status fields in recoveryinfo struct
    #  'A &B' Usable     :  SET_AB_USABLE(0)
    #  'A' corrupted     :  DONT_USE_SET_A(1)
    #  'B' corrupted     :  DONT_USE_SET_B(2)
    #  'A &B' corrupted  :  DONT_USE_SET_AB(3)

    # Set owner fields in recoveryinfo struct
    #  OWNER_XBL         :  1
    #  OWNER_HLOS        :  2
    local owner_hlos=2
    local dont_use_set_a=1
    local dont_use_set_b=2
    local dont_use_set_ab=3
    local current_image_set_status=0
    local telaf_a="telaf_a"
    local telaf_b="telaf_b"
    local volid=$(GetVolumeID telaf)

    if [ ! -e ${abctl_cmd} ]; then
        echo "${abctl_cmd} not found, reboot to edl " > /dev/kmsg
        /bin/sh -c 'reboot edl'
        exit 0
    fi

    # Permission set to recoveryinfo which is used in nad-abctl
    mtd_device=`cat /proc/mtd | grep recoveryinfo | awk -F ':' '{print $1}'`
    if [ -z "${mtd_device}" ]; then
        echo " recoveryinfo part not found, reboot to edl " > /dev/kmsg
        /bin/sh -c 'reboot edl'
        exit 0
    fi
    chmod 666 /dev/${mtd_device}

    telaf_ab_name=$(cat /sys/class/ubi/ubi0_${volid}/name)
    if [ "$telaf_ab_name" == "$telaf_a" ] || [ "$telaf_ab_name" == "$telaf_b" ] ; then
        if [ "x${SLOT_SUFFIX}" == "x" ]; then
            echo "SLOT_SUFFIX not present or invalid, reboot to edl" > /dev/kmsg
            /bin/sh -c 'reboot edl'
            exit 0
        fi

        #Get current image set status
        (${abctl_cmd} --get_image_set_status)
        current_image_set_status=$?

        if [ "$current_image_set_status" -eq "-1" ]; then
            echo "Error: incorrect image set status" > /dev/kmsg
            /bin/sh -c 'reboot edl'
            exit 0
        fi

        if [ "$SLOT_SUFFIX" = "_a" ] && [ "$current_image_set_status" != "$dont_use_set_b" ]; then
            echo "telaf A volume corrupted " > /dev/kmsg
            ${abctl_cmd} --set_image_set_status ${dont_use_set_a}
            if [ "$?" -eq "-1" ]; then
                echo "Error: Set error status for Slot A failed" > /dev/kmsg
                /bin/sh -c 'reboot edl'
                exit 0
            fi
        elif [ "$SLOT_SUFFIX" = "_b" ] && [ "$current_image_set_status" != "$dont_use_set_a" ]; then
            echo "telaf B volume corrupted " > /dev/kmsg
            ${abctl_cmd} --set_image_set_status ${dont_use_set_b}
            if [ "$?" -eq "-1" ]; then
                echo "Error: Set error status for Slot B failed" > /dev/kmsg
                /bin/sh -c 'reboot edl'
                exit 0
            fi
        else
            echo "telaf A and B volume corrupted" > /dev/kmsg
            ${abctl_cmd} --set_image_set_status ${dont_use_set_ab}
            if [ "$?" -eq "-1" ]; then
                echo "Error: Set error status for Slot A and slot B failed" > /dev/kmsg
                /bin/sh -c 'reboot edl'
                exit 0
            fi
        fi
        ${abctl_cmd} --set_owner ${owner_hlos}
        if [ "$?" -eq "-1" ]; then
            echo "Error: set owner failed" > /dev/kmsg
            /bin/sh -c 'reboot edl'
            exit 0
        fi
        echo "Reboot for switching slots or EDL mode" > /dev/kmsg
        /bin/sh -c 'reboot'
    else
        echo "Cannot get TelAF volume , reboot to edl " > /dev/kmsg
        /bin/sh -c 'reboot edl'
        exit 0
    fi
}

FindAndMountUBI() {
    dir=$1
    volid=$(GetVolumeID telaf)
    if [ "$volid" == "" ]; then
        echo "Cannot get TelAF volume." > /dev/kmsg
        return 1
    fi

    telaf_vol_name=`cat /sys/class/ubi/ubi0_$volid/name`
    echo "Get TelAF volume: $volid, name: $telaf_vol_name." > /dev/kmsg
    device=/dev/ubi0_$volid
    block_device=/dev/ubiblock0_$volid

    mkdir -p $dir
    ubiblock --create $device
    WaitDevReady "-b" "${block_device}"
    if [ $? -ne 0 ]; then
       echo "Failed to wait on ${block_device}, exiting." > /dev/kmsg
       return 1
    fi

    if [ ! -e "${VERITY_ENV}" ]; then
        VERITY_ENV="/proc/cmdline"
    fi

    if grep 'nad_avb=1' ${VERITY_ENV} > /dev/null; then
        # The system certificate CA is in the system volume, verified-boot utility
        # need to use this CA to verify the user certificate.
        volid=$(GetVolumeID rootfs)
        if [ "$volid" == "" ]; then
            echo "Cannot get system volume." > /dev/kmsg
            return 1
        fi
        if dd if=/dev/ubi0_$volid count=1 bs=4 2>/dev/null | grep 'hsqs' > /dev/null; then
            CERT_CA_PATH=/dev/ubiblock0_$volid
        else
            CERT_CA_PATH=/dev/mapper/system
        fi
        dm_verity_name=telaf
        dm_verity_device=/dev/mapper/${dm_verity_name}
        verified-boot -n ${dm_verity_name} -d $block_device -p ${CERT_CA_PATH} > /dev/kmsg
        if [ $? -ne 0 ] ; then
            echo CERT_CA_PATH=${CERT_CA_PATH} > /dev/kmsg
            echo "Created dm-verity device ${dm_verity_device} failed." > /dev/kmsg
            return 1
        fi
        WaitDevReady "-b" "${dm_verity_device}"
        if [ $? -ne 0 ]; then
           echo "Failed to wait on ${dm_verity_device}, exiting." > /dev/kmsg
           return 1
        fi
        block_device=${dm_verity_device}
    fi

    mount -t squashfs $block_device $dir -o ro
    if [ $? -ne 0 ] ; then
        echo "Unable to mount squashfs onto $block_device." > /dev/kmsg
        return 1
    fi

    return 0
}

GetTelAfAppVolumeID () {
    volcount=`cat /sys/class/ubi/ubi0/volumes_count`

    for vid in `seq 0 $volcount`; do
        name=`cat /sys/class/ubi/ubi0_$vid/name`
        if [ "$name" == "telaf_app" ]; then
            echo $vid
            break
        fi
    done
}

FindAndMountApp() {
    dir=$1
    volid=$(GetTelAfAppVolumeID)
    if [ "$volid" == "" ]; then
        echo "Cannot get TelAF volume." > /dev/kmsg
        return 1
    fi

    telaf_vol_name=`cat /sys/class/ubi/ubi0_$volid/name`
    echo "Get TelAF volume: $volid, name: $telaf_vol_name." > /dev/kmsg
    device=/dev/ubi0_$volid

    mount -t ubifs $device $dir -o rw,rootcontext=system_u:object_r:telaf_fw_t:s0
    if [ $? -ne 0 ] ; then
        echo "Unable to mount ubifs onto TelAF $dir." > /dev/kmsg
        return 1
    fi

    return 0
}

IsTelAfExisted

# Find correct TelAF volume and mount it
eval FindAndMountUBI "$telaf_mount_point"
if [ $? -ne 0 ] ; then
    echo "Unable to mount TelAF_ro onto $telaf_mount_point" > /dev/kmsg
    IsGPIOEnabled
    if [ $? -eq 1 ]; then
        #GPIO Enabled moving device to EDL.
        echo "GPIO Enabled boot to EDL" > /dev/kmsg
        /bin/sh -c 'reboot edl'
    else
        echo "GPIO disabled switch the slots or boot to EDL" > /dev/kmsg
        SlotSwitchReboot
    fi
    exit -1
fi

# Find correct TelAF App volume and mount it
eval FindAndMountApp "$telaf_app_mount_point"
if [ $? -ne 0 ] ; then
    echo "Unable to mount TelAF_rw onto $telaf_app_mount_point" > /dev/kmsg
    exit -1
fi

echo "Success to mount TelAF onto $telaf_mount_point and $telaf_app_mount_point" > /dev/kmsg
exit 0
