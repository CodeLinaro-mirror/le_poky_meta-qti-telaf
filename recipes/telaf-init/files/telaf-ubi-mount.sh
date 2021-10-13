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

# Mount TelAf partition
telaf_mount_point=/mnt/legato

IsTelAfExisted () {
    if [ -e "${telaf_mount_point}/start" ]; then
        echo "telaf partition has already been mounted" > /dev/kmsg
        exit 0;
    fi
}

WaitDevReady()
{
    local maxTrials=200
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

GetTelAfVolumeID () {
    act_slot=`cat /proc/cmdline | sed 's/.*SLOT_SUFFIX=//' | awk '{print $1}'`
    telaf_ab_name=telaf$act_slot
    volcount=`cat /sys/class/ubi/ubi0/volumes_count`

    for vid in `seq 0 $volcount`; do
        name=`cat /sys/class/ubi/ubi0_$vid/name`
        if [ "$name" == "telaf" ] || [ "$name" == "$telaf_ab_name" ]; then
            echo $vid
            break
        fi
    done
}

FindAndMountUBI() {
    dir=$1
    volid=$(GetTelAfVolumeID)
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

    mount -t squashfs $block_device $dir -o ro
    if [ $? -ne 0 ] ; then
        echo "Unable to mount squashfs onto $block_device." > /dev/kmsg
        return 1
    fi

    return 0
}

IsTelAfExisted

# Find correct TelAF volume and mount it
eval FindAndMountUBI "$telaf_mount_point"
if [ $? -ne 0 ] ; then
    echo "Unable to mount TelAF onto $telaf_mount_point" > /dev/kmsg
    exit -1
fi

echo "Success to mount TelAF onto $telaf_mount_point" > /dev/kmsg
exit 0
