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
telaf_ubi_num=3
telaf_ubi_part=oem1
telaf_mount_point=/mnt/legato

IsTelAfExisted () {
    if [ -e "${telaf_mount_point}/start" ]; then
        echo "telaf partition has already been mounted"
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

FindAndMountUBI () {
    partition=$1
    dir=$2
    device=/dev/ubi${telaf_ubi_num}_0
    block_device=/dev/ubiblock${telaf_ubi_num}_0

    IsTelAfExisted

    mtd_block_number=`cat /proc/mtd | grep -iw $partition | sed 's/^mtd//' | awk -F ':' '{print $1}'`
    echo "MTD : Detected block device : $dir for $partition on mtd$mtd_block_number"
    mkdir -p $dir

    ubiattach -m $mtd_block_number -d ${telaf_ubi_num} /dev/ubi_ctrl
    WaitDevReady "-c" "${device}"
    if [ $? -ne 0 ]; then
        echo "Failed to wait on device, exiting."
        exit 1
    fi

    ubiblock --create $device
    WaitDevReady "-b" "${block_device}"
    if [ $? -ne 0 ]; then
        echo "Failed to wait ubi $block_device."
        exit 1
    fi

    mount -t squashfs $block_device $dir -o ro
    if [ $? -ne 0 ] ; then
        echo "Unable to mount squashfs onto ubiblock${TELAF_UBI_NUM}."
        exit 1
    fi

    return 0
}

FindAndMountUBI $telaf_ubi_part $telaf_mount_point

exit 0
