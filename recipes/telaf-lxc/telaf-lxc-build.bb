inherit pkgconfig
DESCRIPTION = "Telematics Application Framework for LXC Container"

HOMEPAGE = "https://git.codelinaro.org/"
LICENSE = "MPL-2.0 & BSD-3-Clause & BSD-3-Clause-Clear"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/files/common-licenses/BSD-3-Clause;md5=550794465ba0ec5312d6919e203a55f9 \
    file://${COREBASE}/meta-qti-bsp/files/common-licenses/BSD-3-Clause-Clear;md5=3771d4920bd6cdb8cbdf1e8344489ee0 \
    file://${COREBASE}/meta/files/common-licenses/MPL-2.0;md5=815ca599c9df247a0c7f619bab123dad"

# Host dependencies
DEPENDS += "ninja-native"
DEPENDS += "cmake-native"
DEPENDS += "coreutils-native"
DEPENDS += "squashfs-tools-native"
DEPENDS += "mtd-utils-native"

# Target dependencies
DEPENDS += "openssl"
DEPENDS += "libxml2"
DEPENDS += "xmllib"
DEPENDS += "telux"
DEPENDS += "telux-lib"

DEPENDS += "vsomeip"

DEPENDS += "refpolicy-mls"

FILESPATH =+ "${WORKSPACE}:"
SRC_URI += "file://telaf/"
SRC_URI += "file://legato/"

S = "${WORKDIR}/telaf"
S_L = "${WORKDIR}/legato"

PARALLEL_MAKE = ""

TELAF_TARGET_STAGE_DIR = "${S_L}/legato-af/build/${MACHINE}/_staging_system.${MACHINE}.update_ro"

do_compile[nostamp]  = "1"

do_compile() {
    export WORK_ROOT=${WORKDIR}
    export OECORE_TARGET_SYSROOT=${RECIPE_SYSROOT}
    if [ -f ${S}/VERSION ]; then
        export LEGATO_VERSION=`cat ${S}/VERSION 2>/dev/null`
    fi

    # To build LXC TelAF system, set BUILD_FLAVOR to lxc as below.
    # If BUILD_FLAVOR is not set, "default" TelAF system will be built.
    export BUILD_FLAVOR="lxc"

    oe_runmake distclean
    oe_runmake ${MACHINE}
}

# Place the TelAF container system into the container rootfs/legato folder
do_install_append() {
    install -m 0755  -d ${D}/legato

    cp -r -d --no-preserve=ownership ${TELAF_TARGET_STAGE_DIR}/* ${D}/legato/

    # TelAF power manager nodes, /sys/power/wake_lock and /sys/power/unwake_lock are not available
    # in the container. So remove them.
    rm -rf ${D}/legato/systems/current/appsWriteable/powerMgr

}

FILES_${PN} += "/legato"

# The telaf libraries are non versioned and triggers the following QA errors:
# -------------
# ERROR: telaf-lxc-build-1.0-r0 do_package_qa: [dev-so]
# QA Issue: non -dev/-dbg/nativesdk- package telaf-lxc-build contains symlink .so
# '/legato/apps/cee85e31eea65079c4e92e8e516df185/read-only/lib/libComponent_helloWorldComponent.so'
# '/legato/systems/current/lib/libjansson.so'
#
# -------------
# To avoid the QA error, skip so testing. TelAF when building, checks the whole system.
INSANE_SKIP_${PN} = "dev-so"

# Fix for QA warning: File <> in package telaf-lxc-build doesn't have GNU_HASH(didn't pass LDFLAGS?)
TARGET_CC_ARCH += "${LDFLAGS}"
