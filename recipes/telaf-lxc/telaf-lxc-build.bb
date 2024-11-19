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

# Target dependencies
DEPENDS += "openssl"
DEPENDS += "libcap"
DEPENDS += "vsomeip"
DEPENDS += "refpolicy-mls-auto"

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

    if ${@bb.utils.contains('MULTILIB_VARIANTS', 'lib32', 'true', 'false', d)}; then
        if ${@bb.utils.contains('CFLAGS', '-D_TIME_BITS=64', 'true', 'false', d)}; then
            MKTOOLS_X_C_FLAGS="-X -D_TIME_BITS=64 -X -D_FILE_OFFSET_BITS=64"
            MKTOOLS_X_C_FLAGS+=" -C -D_TIME_BITS=64 -C -D_FILE_OFFSET_BITS=64"
            export MKTOOLS_X_C_FLAGS
        fi
    fi

    oe_runmake distclean
    oe_runmake ${MACHINE}
}

# Place the TelAF container system into the container rootfs/legato folder
do_install:append() {
    install -m 0755 -d ${D}/mnt/legato
    cp -r -d --no-preserve=ownership ${TELAF_TARGET_STAGE_DIR}/* ${D}/mnt/legato/

    # TelAF power manager nodes, /sys/power/wake_lock and /sys/power/unwake_lock are not available
    # in the container. So remove them.
    rm -rf ${D}/mnt/legato/systems/current/appsWriteable/powerMgr

}

FILES:${PN} += "/mnt/legato"

# The telaf libraries are non versioned and triggers the following QA errors:
# -------------
# ERROR: telaf-lxc-build-1.0-r0 do_package_qa: [dev-so]
# QA Issue: non -dev/-dbg/nativesdk- package telaf-lxc-build contains symlink .so
# '/legato/apps/cee85e31eea65079c4e92e8e516df185/read-only/lib/libComponent_helloWorldComponent.so'
# '/legato/systems/current/lib/libjansson.so'
#
# -------------
# To avoid the QA error, skip so testing. TelAF when building, checks the whole system.
INSANE_SKIP:${PN} = "dev-so"
# Avoid [already-stripped] and [ldflags] QA errors.
INSANE_SKIP:${PN} += "already-stripped"

# Fix for QA warning: File <> in package telaf-lxc-build doesn't have GNU_HASH(didn't pass LDFLAGS?)
INSANE_SKIP:${PN} += "ldflags"

# Add default telaf user and appdefault user
inherit useradd
USERADD_PACKAGES = "${PN}"
USERADD_PARAM:${PN} += "-M -U telaf;"
USERADD_PARAM:${PN} += "-M -U appdefault;"
USERADD_PARAM:${PN} += "-M -g root securityunpack;"
# Default service users
USERADD_PARAM:${PN} += "-M -U tafaudiosvc;"
USERADD_PARAM:${PN} += "-M -U tafcansvc;"
USERADD_PARAM:${PN} += "-M -U tafdatacallsvc;"
USERADD_PARAM:${PN} += "-M -U tafdevinfosvc;"
USERADD_PARAM:${PN} += "-M -U tafdiagsvc;"
USERADD_PARAM:${PN} += "-M -U tafhmsvc;"
USERADD_PARAM:${PN} += "-M -U tafivssinfosvc;"
USERADD_PARAM:${PN} += "-M -U tafivssradiosvc;"
USERADD_PARAM:${PN} += "-M -U tafivsssimsvc;"
USERADD_PARAM:${PN} += "-M -U tafkeystoresvc;"
USERADD_PARAM:${PN} += "-M -U taflocationsvc;"
USERADD_PARAM:${PN} += "-M -U taflxcinitapp;"
USERADD_PARAM:${PN} += "-M -U tafmrcsvc;"
USERADD_PARAM:${PN} += "-M -U tafmngdaudiosvc;"
USERADD_PARAM:${PN} += "-M -U tafmngdconnsvc;"
USERADD_PARAM:${PN} += "-M -U tafmngdpmsvc;"
USERADD_PARAM:${PN} += "-M -U tafnetsvc;"
USERADD_PARAM:${PN} += "-M -U tafpmsvc;"
USERADD_PARAM:${PN} += "-M -U tafradiosvc;"
USERADD_PARAM:${PN} += "-M -U tafremotesimsvc;"
USERADD_PARAM:${PN} += "-M -U tafrpcproxy;"
USERADD_PARAM:${PN} += "-M -U tafsmssvc;"
USERADD_PARAM:${PN} += "-M -U tafsimcardsvc;"
USERADD_PARAM:${PN} += "-M -U tafsomeipgwsvc;"
USERADD_PARAM:${PN} += "-M -U tafthermsvc;"
USERADD_PARAM:${PN} += "-M -U taftimesvc;"
USERADD_PARAM:${PN} += "-M -U tafupdatesvc;"
USERADD_PARAM:${PN} += "-M -U tafvoicecallsvc;"
# TelAF Reserved Users
USERADD_PARAM:${PN} += "-M -U taftestapp;"
USERADD_PARAM:${PN} += "-M -U tafsampleapp;"
USERADD_PARAM:${PN} += "-M -U tafusr0;"
USERADD_PARAM:${PN} += "-M -U tafusr1;"
USERADD_PARAM:${PN} += "-M -U tafusr2;"
USERADD_PARAM:${PN} += "-M -U tafusr3;"

GCC_PREFIX = "${@bb.utils.contains('BASEMACHINE', 'sa525m', bb.utils.contains('MULTILIB_VARIANTS', 'lib32', 'arm-oemllib32-linux-gnueabi', 'aarch64-oe-linux', d), '', d)}"
EXTRA_OEMAKE += "'GCC_PREFIX=${GCC_PREFIX}'"
