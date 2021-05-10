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

# Mount TelAf partition and run it
if [ -e "/etc/telaf.env" ]; then
    source /etc/telaf.env
fi

umount_all()
{
    umount /etc/ld.so.conf
    umount /etc/ld.so.cache
    umount /etc/hosts

    return ${TELAF_OK}
}

MOUNTPOINT_TELAF="/mnt/legato"

if [ -e "${MOUNTPOINT_TELAF}/systems/current/read-only" ]
then
    export PATH=/legato/systems/current/bin:$PATH
    TELAF_START=/legato/systems/current/bin/start
else
    echo "Only support read-only TelAf!"
    exit ${TELAF_ERR}
fi

case "$1" in
    start)
        echo "TelAf start sequence"
        umount /legato 2>/dev/null
        mount -o bind $MOUNTPOINT_TELAF /legato
        test -x $TELAF_START && $TELAF_START
        ;;

    stop)
        echo "TelAf shutdown sequence"
        test -x $TELAF_START && $TELAF_START stop
        umount /legato
        umount_all
        ;;

    *)
        echo "Only support start & stop!"
        exit ${TELAF_ERR}
        ;;

esac

echo "Finished TelAf $1 Sequence"
