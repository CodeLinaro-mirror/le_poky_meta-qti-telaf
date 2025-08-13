SUMMARY = "QTI package group for telaf modules"
LICENSE = "BSD-3-Clause-Clear"

PACKAGE_ARCH = "${MACHINE_ARCH}"
PR= "r0"

DEPENDS += "telaf-image"
DEPENDS:append = " ${@bb.utils.contains('BUILD_VARIANT', 'full', 'telaf-pa-legacy-build telaf-pa-default-build telaf-test-build', '', d)}"

inherit packagegroup

PACKAGES = "\
             packagegroup-qti-telaf \
           "

# telaf packages which are common across various machines
# dlt-daemon excluded
RDEPENDS:${PN} += "\
    vsomeip \
    boost \
    logd \
    keyutils \
    policycoreutils-hll \
    policycoreutils-loadpolicy \
    common-api-c++ \
    common-api-c++-someip \
    telaf-init \
    "

RDEPENDS:${PN}:append = " ${@bb.utils.contains('BUILD_VARIANT', 'full', 'telaf-mod-build', '', d)}"

