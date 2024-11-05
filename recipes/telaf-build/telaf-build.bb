inherit systemd pkgconfig deploy
DESCRIPTION = "Telematics Application Framework"

HOMEPAGE = "https://www.codeaurora.org/"
LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/files/common-licenses/${LICENSE};md5=550794465ba0ec5312d6919e203a55f9"

# Host dependencies
DEPENDS += "ninja-native"
DEPENDS += "cmake-native"
DEPENDS += "coreutils-native"
DEPENDS += "squashfs-tools-native"
DEPENDS += "mtd-utils-native"
DEPENDS += "capicxx-core-native"
DEPENDS += "capicxx-someip-native"

# Target dependencies
DEPENDS += "openssl"
DEPENDS += "libxml2"
DEPENDS += "xmllib"
DEPENDS += "telux"
DEPENDS += "telux-lib"

DEPENDS += "vsomeip"
DEPENDS += "common-api-c++"
DEPENDS += "common-api-c++-someip"

DEPENDS += "refpolicy-mls-auto"

FILESPATH =+ "${WORKSPACE}:"
SRC_URI += "file://telaf/"
SRC_URI += "file://telaf-adv/"
SRC_URI += "file://legato/"
SRC_URI += "file://external/wpa_supplicant_8/"

S = "${WORKDIR}/telaf"
S_L = "${WORKDIR}/legato"
S_V = "${WORKDIR}/telaf-adv"

PARALLEL_MAKE = ""

RM_WORK_EXCLUDE += "telaf-build"

do_compile() {
    export WORK_ROOT=${WORKDIR}
    export OECORE_TARGET_SYSROOT=${RECIPE_SYSROOT}
    if [ -f ${S}/VERSION ]; then
        export LEGATO_VERSION=`cat ${S}/VERSION 2>/dev/null`
    fi

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

do_install:append() {
    install -d ${D}/${libdir}/pkgconfig

    # Replace "{MACHINE}" with machine type
    TELAF_PC_FILE="${S}/telaf.pc"
    TELAF_PC_CONTENT=$(cat "${TELAF_PC_FILE}")
    TELAF_PC_CONTENT=${TELAF_PC_CONTENT//\$\{MACHINE\}/${MACHINE}}
    echo "${TELAF_PC_CONTENT}" > "${D}/${libdir}/pkgconfig/telaf.pc"
}

SYSROOT_PREPROCESS_FUNCS += "telaf_populate_sysroot"
telaf_populate_sysroot() {
    sysroot_stage_dir ${S_L} ${SYSROOT_DESTDIR}/telaf/legato/
    sysroot_stage_dir ${S} ${SYSROOT_DESTDIR}/telaf/telaf/
    sysroot_stage_dir ${S_V} ${SYSROOT_DESTDIR}/telaf/telaf-adv/
}

GCC_PREFIX = "${@bb.utils.contains('BASEMACHINE', 'sa525m', bb.utils.contains('MULTILIB_VARIANTS', 'lib32', 'arm-oemllib32-linux-gnueabi', 'aarch64-oe-linux', d), '', d)}"
EXTRA_OEMAKE += "'GCC_PREFIX=${GCC_PREFIX}'"
