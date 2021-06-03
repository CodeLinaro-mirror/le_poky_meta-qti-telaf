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

FindAndMountMTD () {
    partition=$1
    dir=$2

    mtd_block_number=`cat /proc/mtd | grep -iw $partition | sed 's/^mtd//' | awk -F ':' '{print $1}'`
    echo "MTD : Detected block device : mtd_block_number: $mtd_block_number, dir: $dir, for partition
                                      : $partition" > /dev/kmsg
    mkdir -p $dir
    telaf_block=/dev/mtdblock$mtd_block_number

    WaitDevReady "-b" "${telaf_block}"
    if [ $? -ne 0 ]; then
       echo "Failed to wait on device, exiting."
       exit 1
    fi

    mount -t squashfs $telaf_block $telaf_mount_point -o ro
    if [ $? -ne 0 ]; then
       echo "Failed to mount volume $telaf_block." > /dev/kmsg
       exit 1
    fi

    return 0
}

IsTelAfExisted

for ubivol in /sys/class/ubi/ubi[0-99]_*/name; do
volname=`cat $ubivol`
#Find telaf volume in A/B and NON A/B
#telaf_a will tell it is A/B partition
#Break the loop when telaf is found
if [ "$volname" == "telaf" ] || [ "$volname" == "telaf_a" ]; then
    echo "Found telaf Volume: $volname" > /dev/kmsg
    break
fi
done

#Check to find A/B or NON A/B
#SLOT_SUFFIX is set by set-slotsuffix.service
if [ "$volname" == "telaf" ]; then
      echo "Mounting telaf for NON A/B" > /dev/kmsg
      eval FindAndMountMTD telaf $telaf_mount_point
elif [ "$volname" == "telaf_a" ]; then
      echo "Mounting telaf for A/B" > /dev/kmsg
      eval FindAndMountMTD telaf$SLOT_SUFFIX $telaf_mount_point
else
      echo "Mounting telaf for UBI" > /dev/kmsg
      eval FindAndMountUBI $telaf_ubi_part $telaf_mount_point
fi

exit 0
