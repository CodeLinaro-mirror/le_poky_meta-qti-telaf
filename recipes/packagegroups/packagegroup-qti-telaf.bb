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
    telaf-mod-build \
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

def get_depends_noship(d):
    if d.getVar('HAS_TELAF_NOSHIP', True) == 'true':
        return "uds-stack"
    else:
        return ""
RDEPENDS:${PN} += "${@get_depends_noship(d)}"
