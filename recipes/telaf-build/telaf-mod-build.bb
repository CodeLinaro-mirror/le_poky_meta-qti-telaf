inherit module

DESCRIPTION = "TelAF Kernel Module Build"
LICENSE = "GPL-2.0-only"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/files/common-licenses/${LICENSE};md5=801f80980d171dd6425610833a22dbe6"


PR = "r0"

DEPENDS += "virtual/kernel"

SRC_URI = "file://gpioWakeup"

S = "${WORKDIR}/gpioWakeup"

KERNEL_CC:append:sa525m = " ${SECURITY_CFLAGS} "

do_install:append() {
    install -d ${D}/usr/lib/modules/
    #Signing and installing the gpioWakup kernel module
    ${STAGING_DIR_NATIVE}/usr/libexec/aarch64-oe-linux/gcc/aarch64-oe-linux/11.4.0/strip \
    --strip-debug ${S}/gpioWakeup.ko
    LD_LIBRARY_PATH=${WORKSPACE}/kernel-${PREFERRED_VERSION_linux-msm}/kernel_platform/prebuilts/kernel-build-tools/linux-x86/lib64/ \
    ${STAGING_KERNEL_BUILDDIR}/scripts/sign-file sha512 ${STAGING_KERNEL_BUILDDIR}/certs/signing_key.pem \
    ${STAGING_KERNEL_BUILDDIR}/certs/signing_key.x509 ${S}/gpioWakeup.ko
    install -m 0755 ${S}/gpioWakeup.ko -D ${D}/usr/lib/modules/gpioWakeup.ko

    # Delete kernel-module-gpiowakeup-5.15.137-debug-gddbbb6ad2a37, which is only for debug purpose 
    rm -fr ${D}/lib
}

FILES:${PN} += "/usr/lib/modules/gpioWakeup.ko"
