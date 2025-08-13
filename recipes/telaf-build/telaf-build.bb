inherit systemd pkgconfig deploy

DESCRIPTION = "Telematics Application Framework"
HOMEPAGE = "https://www.codeaurora.org/"
LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/files/common-licenses/${LICENSE};md5=550794465ba0ec5312d6919e203a55f9"

# Host dependencies
DEPENDS += "ninja-native cmake-native coreutils-native squashfs-tools-native mtd-utils-native capicxx-core-native capicxx-someip-native telaf-pa-default-build"

# Target dependencies
DEPENDS += "openssl libxml2 xmllib telux telux-lib vsomeip common-api-c++ common-api-c++-someip refpolicy-mls-auto open-avb"

FILESPATH =+ "${WORKSPACE}:"
SRC_URI += "file://telaf/ file://telaf-adv/ file://legato/ file://telaf-pa/ file://telaf-pa-default/ file://external/wpa_supplicant_8/"

S = "${WORKDIR}/telaf"
S_L = "${WORKDIR}/legato"
S_V = "${WORKDIR}/telaf-adv"
S_PA_DEF = "${WORKDIR}/telaf-pa-default"

PARALLEL_MAKE = ""

RM_WORK_EXCLUDE += "telaf-build"

set_environment_variables() {
    export WORK_ROOT=${WORKDIR}
    export OECORE_TARGET_SYSROOT=${RECIPE_SYSROOT}
    if [ -f ${S}/VERSION ]; then
        export LEGATO_VERSION=$(cat ${S}/VERSION 2>/dev/null)
    fi

    if ${@bb.utils.contains('MULTILIB_VARIANTS', 'lib32', 'true', 'false', d)} && \
       ${@bb.utils.contains('CFLAGS', '-D_TIME_BITS=64', 'true', 'false', d)}; then
        export MKTOOLS_X_C_FLAGS="-X -D_TIME_BITS=64 -X -D_FILE_OFFSET_BITS=64 -C -D_TIME_BITS=64 -C -D_FILE_OFFSET_BITS=64"
    fi
    export TELAF_DEFAULT_PA_LIB_DIR=${OECORE_TARGET_SYSROOT}/telaf/default-pa/
}

do_compile() {
    set_environment_variables

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
    sysroot_stage_dir ${S_PA_DEF} ${SYSROOT_DESTDIR}/telaf/telaf-pa-default/
}

GCC_PREFIX = "${@bb.utils.contains('BASEMACHINE', 'sa525m', bb.utils.contains('MULTILIB_VARIANTS', 'lib32', 'arm-oemllib32-linux-gnueabi', 'aarch64-oe-linux', d), '', d)}"
EXTRA_OEMAKE += "'GCC_PREFIX=${GCC_PREFIX}'"
