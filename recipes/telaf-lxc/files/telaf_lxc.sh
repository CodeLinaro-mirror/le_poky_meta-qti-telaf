#!/usr/bin/env sh

# Copyright (c) 2023 Qualcomm Innovation Center, Inc. All rights reserved.
# SPDX-License-Identifier: LGPL-2.1-only

TELAF_LXC_CON_NAME=telaflxc
PVM_VETH_IP_ADDR="192.168.2.200"

CONTAINER_LOG_STORAGE="/tmp/container_log"
LXC_CONTAINER_PATH="/tmp/container"
LXC_CONTAINER_CONF="/etc/lxc/lxc_telaf.conf"
PVM_SHARED_LXC_RW="/data/lxc_rw"

function prepare_lxc_dirs()
{
    # Follow the configuration in /etc/lxc/lxc.conf
    mkdir -p ${LXC_CONTAINER_PATH}

    # For all logs from lxc-cmds
    mkdir -p ${CONTAINER_LOG_STORAGE}

    # Prepare all dirs for the telaf lxc container
    mkdir -p ${PVM_SHARED_LXC_RW}/data
    mkdir -p ${PVM_SHARED_LXC_RW}/persist
    mkdir -p ${PVM_SHARED_LXC_RW}/app
    mkdir -p ${PVM_SHARED_LXC_RW}/tmp
}

function telaflxc_create()
{
    prepare_lxc_dirs

    if [ -n "${1}" ]; then
        LXC_CONTAINER_CONF="${1}"
    fi

    logger -- "LXC configuration: ${LXC_CONTAINER_CONF}"

    lxc-create -n ${TELAF_LXC_CON_NAME} \
            -f ${LXC_CONTAINER_CONF} \
            -t none \
            -o ${CONTAINER_LOG_STORAGE}/lxc-create.log \
            -l TRACE
}

function telaflxc_start()
{
    lxc-start -n ${TELAF_LXC_CON_NAME} -o ${CONTAINER_LOG_STORAGE}/lxc-start.log -l TRACE

    lxc-info -n ${TELAF_LXC_CON_NAME} | grep Link: \
             | awk '{print $2}' \
             | xargs -t -I {} /sbin/ifconfig {} ${PVM_VETH_IP_ADDR}
}

function telaflxc_info()
{
    lxc-info -n ${TELAF_LXC_CON_NAME}
}

function telaflxc_stop()
{
    lxc-stop -n ${TELAF_LXC_CON_NAME} -o ${CONTAINER_LOG_STORAGE}/lxc-stop.log -l TRACE
}

function telaflxc_destroy()
{
    lxc-destroy -n ${TELAF_LXC_CON_NAME}  -o ${CONTAINER_LOG_STORAGE}/lxc-destroy.log -l TRACE
    rm -rf ${LXC_CONTAINER_PATH}/${TELAF_LXC_CON_NAME}
}

function help()
{
    echo "Usage: telaf_lxc.sh <action-cmd>"
    echo "<action-cmd> list:"
    echo "  - create, start, info, stop, destroy"
}

case "$1" in
   create)
      telaflxc_create "$2"
      ;;
   start)
      telaflxc_start
      ;;
   info)
      telaflxc_info
      ;;
   stop)
      telaflxc_stop
      ;;
   destroy)
      telaflxc_destroy
      ;;
   *)
      help
      exit 1
esac
exit $?
