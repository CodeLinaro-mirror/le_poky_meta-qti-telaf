inherit module

DESCRIPTION = "TelAF Kernel Module Build"
LICENSE = "GPL-2.0"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/files/common-licenses/${LICENSE};md5=801f80980d171dd6425610833a22dbe6"

PR = "r0"

DEPENDS += "virtual/kernel"

SRC_URI = "file://gpioWakeup"

S = "${WORKDIR}/gpioWakeup"