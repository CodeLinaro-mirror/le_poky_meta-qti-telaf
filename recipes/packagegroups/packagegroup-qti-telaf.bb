SUMMARY = "QTI package group for telaf modules"
LICENSE = "BSD-3-Clause-Clear"

PACKAGE_ARCH = "${MACHINE_ARCH}"
PR= "r0"

DEPENDS += "telaf-image telaf-test-build"
inherit packagegroup

PACKAGES = "\
             packagegroup-qti-telaf \
           "

# telaf packages which are common across various machines
RDEPENDS:${PN} += "\
    telaf-init \
    vsomeip \
    boost \
    logd \
    keyutils \
    policycoreutils-hll \
    policycoreutils-loadpolicy \
    common-api-c++ \
    common-api-c++-someip \
    dlt-daemon \
    "
