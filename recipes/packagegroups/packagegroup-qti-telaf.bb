SUMMARY = "QTI package group for telaf modules"
LICENSE = "BSD-3-Clause-Clear"

PACKAGE_ARCH = "${MACHINE_ARCH}"
PR= "r0"

DEPENDS += "telaf-image"
DEPENDS:append = " ${@bb.utils.contains('BUILD_VARIANT', 'full', 'telaf-pa-default-build telaf-test-build', '', d)}"

inherit packagegroup

PACKAGES = "\
             packagegroup-qti-telaf \
           "

# telaf packages which are common across various machines
RDEPENDS:${PN} += "\
    vsomeip \
    boost \
    logd \
    keyutils \
    policycoreutils-hll \
    policycoreutils-loadpolicy \
    common-api-c++ \
    common-api-c++-someip \
    dlt-daemon \
    telaf-init \
    "

# telaf-mod-build's do_install:append hardcodes an aarch64-oe-linux strip path
# for module signing, which doesn't exist for sa510m's armv7 toolchain. Drop it
# from RDEPENDS so bitbake doesn't try to build it for sa510m.
#RDEPENDS:${PN}:append = " ${@bb.utils.contains('BUILD_VARIANT', 'full', 'telaf-mod-build', '', d)}"

