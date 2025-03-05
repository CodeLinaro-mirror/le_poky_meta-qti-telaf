DESCRIPTION = "Platform Adaptor for Telematics Application Framework"

HOMEPAGE = "https://www.codeaurora.org/"
LICENSE = "BSD-3-Clause-Clear"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/${LICENSE};md5=7a434440b651f4a472ca93716d01033a"

inherit systemd pkgconfig deploy qprebuilt

DEPENDS += "telaf-build ninja-native cmake-native coreutils-native squashfs-tools-native mtd-utils-native"
DEPENDS += "openssl libxml2 xmllib telux telux-lib"

FILESPATH =+ "${WORKSPACE}:"
SRC_URI += "file://telaf-pa/"
SRC_DIR = "${WORKSPACE}/telaf-pa/"

INSANE_SKIP:${PN} += "installed-vs-shipped"

S = "${WORKDIR}/telaf-pa"
SS = "${RECIPE_SYSROOT}/telaf/telaf/build/${MACHINE}/telaf-pa"

TARGET_CC_ARCH += "${LDFLAGS}"

set_environment_variables() {
    export WORK_ROOT="${WORKDIR}"
    export LEGATO_ROOT="${RECIPE_SYSROOT}/telaf/legato/legato-af"
    export TELAF_ROOT="${RECIPE_SYSROOT}/telaf/telaf"
    export TELAF_PA_DEFAULT="${RECIPE_SYSROOT}/telaf/telaf-pa-default"

    if ${@bb.utils.contains('MULTILIB_VARIANTS', 'lib32', 'true', 'false', d)} && \
       ${@bb.utils.contains('CFLAGS', '-D_TIME_BITS=64', 'true', 'false', d)}; then
        export MKTOOLS_X_C_FLAGS="-X -D_TIME_BITS=64 -X -D_FILE_OFFSET_BITS=64 -C -D_TIME_BITS=64 -C -D_FILE_OFFSET_BITS=64"
    fi
}

do_compile() {
    set_environment_variables

    ${S}/build.sh ${MACHINE}
}

SYSROOT_PREPROCESS_FUNCS += "telaf_populate_sysroot"
telaf_populate_sysroot() {
    MY_DIR=${SS}
    [ ! -d ${MY_DIR} ] && MY_DIR=${S}
    sysroot_stage_dir ${MY_DIR}/ ${SYSROOT_DESTDIR}/telaf/telaf-pa/
}
