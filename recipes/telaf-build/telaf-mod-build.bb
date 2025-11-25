inherit module

DESCRIPTION = "TelAF Kernel Module Build"
LICENSE = "GPL-2.0-only"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/files/common-licenses/${LICENSE};md5=801f80980d171dd6425610833a22dbe6"


PR = "r0"

DEPENDS += "virtual/kernel"

SRC_URI = "file://gpioWakeup"

S = "${WORKDIR}/gpioWakeup"

KERNEL_CC:append:sa510m = " ${SECURITY_CFLAGS} "

do_install:append() {
    install -d ${D}/usr/lib/modules/
    install -m 0755 ${S}/gpioWakeup.ko -D ${D}/usr/lib/modules/gpioWakeup.ko
    # Delete kernel-module-gpiowakeup-5.15.137-debug-gddbbb6ad2a37, which is only for debug purpose 
    rm -fr ${D}/lib
}

FILES:${PN} += "/usr/lib/modules/gpioWakeup.ko"
