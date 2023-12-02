#!/usr/bin/env sh

# Copyright (c) 2023 Qualcomm Innovation Center, Inc. All rights reserved.
# SPDX-License-Identifier: LGPL-2.1-only

# Source this file for some lxc script support

export PATH=/etc/lxc:$PATH

function telaflxc_attach()
{
    lxc-attach -n telaflxc $@ 2> /dev/null
    return $?
}

alias telaflxc.create="sh /etc/lxc/telaf_lxc.sh create"
alias telaflxc.start="sh /etc/lxc/telaf_lxc.sh start"
alias telaflxc.info="sh /etc/lxc/telaf_lxc.sh info"
alias telaflxc.stop="sh /etc/lxc/telaf_lxc.sh stop"
alias telaflxc.destroy="sh /etc/lxc/telaf_lxc.sh destroy"
alias telaflxc.attach="lxc-attach -n telaflxc"
alias telaflxc.cmd="telaflxc_attach"
