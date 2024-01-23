#!/usr/bin/env sh

# Copyright (c) 2023 Qualcomm Innovation Center, Inc. All rights reserved.
# SPDX-License-Identifier: LGPL-2.1-only

# Source this file for some lxc script support

export PATH=/etc/lxc:$PATH

TELAF_LXC_CMD_SCP=/etc/lxc/telaf_lxc.sh

function telaflxc_cmds()
{
    case "$1" in
        create|c)
            sh ${TELAF_LXC_CMD_SCP} create "$2"
            ;;
        start|st)
            sh ${TELAF_LXC_CMD_SCP} start
            ;;
        info|ls|status)
            sh ${TELAF_LXC_CMD_SCP} info
            ;;
        stop|sp)
            sh ${TELAF_LXC_CMD_SCP} stop
            ;;
        destroy|d)
            sh ${TELAF_LXC_CMD_SCP} destroy
            ;;
        attach|cmd)
            shift
            lxc-attach -n telaflxc "$@"
            ;;
        *)
            echo "Usage: telaflxc <create|start|info|stop|destroy|attach>"
            return 1
            ;;
    esac

    return $?
}

alias telaflxc=telaflxc_cmds

alias telaflxc.create="telaflxc_cmds create"
alias telaflxc.start="telaflxc_cmds start"
alias telaflxc.info="telaflxc_cmds info"
alias telaflxc.stop="telaflxc_cmds stop"
alias telaflxc.destroy="telaflxc_cmds destroy"
alias telaflxc.attach="telaflxc_cmds attach"
alias telaflxc.cmd="telaflxc_cmds cmd"
