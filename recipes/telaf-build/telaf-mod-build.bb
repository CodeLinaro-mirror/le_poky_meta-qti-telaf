inherit module

DESCRIPTION = "TelAF Kernel Module Build"
LICENSE = "GPL-2.0"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/files/common-licenses/${LICENSE};md5=801f80980d171dd6425610833a22dbe6"

PR = "r0"

DEPENDS += "virtual/kernel"

SRC_URI = "file://gpioWakeup"

S = "${WORKDIR}/gpioWakeup"

do_install_append() {
    install -d ${D}/usr/lib/modules/
    install -m 0755 ${S}/gpioWakeup.ko -D ${D}${libdir}/modules/gpioWakeup.ko
}

FILES_${PN} += "${libdir}/modules/*"