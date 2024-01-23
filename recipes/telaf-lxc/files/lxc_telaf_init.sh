#!/usr/bin/env sh

# Copyright (c) 2023 Qualcomm Innovation Center, Inc. All rights reserved.
# SPDX-License-Identifier: BSD-3-Clause-Clear

export PATH=/legato/systems/current/bin:/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin

# One RW folder will be mounted in the container on /tmp
# Create required RW folders for TelAF using overlays on /tmp

function clear_dir()
{
    rm -rf ${1}
    mkdir -p ${1}
}

function prepare_dirs_for_telaf()
{
    mkdir -p /tmp/legato_logs
    mkdir -p /tmp/legato

    mkdir -p /data/le_fs
    chmod 0777 /data/le_fs

    mkdir -p /data/persist
    mkdir -p /data/ManagedServices

    mount -o bind /mnt/legato /legato
    [ $? -eq 0 ] || exit $?
}

function lxc_start_telaf()
{
    prepare_dirs_for_telaf

    busybox syslogd -C2000 -O /tmp/syslog -b 5 &
    [ $? -eq 0 ] || exit $?

    telaf start
}

function lxc_stop_telaf()
{
    # Keep all stuff in our RW dir for inspecting

    telaf stop

    # Try to stop syslogd daemon process
    /usr/bin/killall syslogd

    # Umount all bind dirs
    umount /legato
}

case "$1" in
   start)
      [ "$TELAF_IN_CONTAINER" == "y" ] && lxc_start_telaf
      ;;
   stop)
      [ "$TELAF_IN_CONTAINER" == "y" ] && lxc_stop_telaf
      ;;
   *)
      echo $"Usage: $0 {start|stop}"
      exit 1
esac
exit $?
