inherit systemd pkgconfig deploy

DESCRIPTION = "Telematics Test Image & Test Applications"
HOMEPAGE = "https://www.codelinaro.org/"
LICENSE = "BSD-3-Clause-Clear"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/BSD-3-Clause-Clear;md5=7a434440b651f4a472ca93716d01033a"

PR = "r1"
DEBUG_BUILD ?= "1"

PACKAGECONFIG ??= "image testapps"

DEPENDS += "\
    ninja-native \
    cmake-native \
    coreutils-native \
    squashfs-tools-native \
    mtd-utils-native \
    capicxx-core-native \
    capicxx-someip-native \
    boost-native \
"

DEPENDS += "\
    openssl \
    libxml2 \
    xmllib \
    telux \
    telux-lib \
    vsomeip \
    common-api-c++ \
    common-api-c++-someip \
    refpolicy-mls-auto \
    open-avb \
    telaf-build \
"

python __anonymous() {
    if d.getVar('HAS_TELAF_PROP', True) == 'true':
        d.appendVar('DEPENDS', ' telaf-prop-build')
    if d.getVar('HAS_TELAF_NOSHIP', True) == 'true':
        d.appendVar('DEPENDS', ' telaf-noship-build')
}

FILESPATH =+ "${WORKSPACE}:"
SRC_URI += "file://telaf/ file://telaf-adv/ file://legato/ file://telaf-pa/ file://telaf-pa-default/ file://external/wpa_supplicant_8/"
S = "${WORKDIR}/telaf"

PARALLEL_MAKE = ""
RM_WORK_EXCLUDE += "${PN}"

TELAF_TARGET_STAGE_DIR = "${WORKDIR}/legato/legato-af/build/${MACHINE}/_staging_system.${MACHINE}.update_ro"

do_configure[noexec] = "1"

export_common_env() {
    export WORK_ROOT="${WORKDIR}"
    export OECORE_TARGET_SYSROOT="${RECIPE_SYSROOT}"

    if [ -f "${S}/VERSION" ]; then
        export LEGATO_VERSION="$(cat "${S}/VERSION" 2>/dev/null || true)"
    fi

    if ${@bb.utils.contains('MULTILIB_VARIANTS', 'lib32', 'true', 'false', d)} && \
       ${@bb.utils.contains('CFLAGS', '-D_TIME_BITS=64', 'true', 'false', d)}; then
        export MKTOOLS_X_C_FLAGS="-X -D_TIME_BITS=64 -X -D_FILE_OFFSET_BITS=64 -C -D_TIME_BITS=64 -C -D_FILE_OFFSET_BITS=64"
    fi

    export LEGATO_ROOT="${WORKDIR}/legato/legato-af"
    export TELAF_ROOT="${S}"

    export VENDOR_ROOT="${RECIPE_SYSROOT}/telaf/vendor"
    export TELAF_DEFAULT_PA_LIB_DIR=${OECORE_TARGET_SYSROOT}/telaf/default-pa/
    export ENABLE_TEST_APPS="y"
}

build_telaf_image() {
    oe_runmake distclean
    oe_runmake ${MACHINE}
}

build_testapps() {
    build_directory="${S}/testapp_build/build"
    if [ -d "${build_directory}" ]; then
        rm -rf "${build_directory}"
    fi

    export CFLAGS="${BUILD_FLAGS:-} -O"

    S_L="${RECIPE_SYSROOT}/telaf/legato/legato-af"

    cmake -DLEGATO_TARGET=${MACHINE} -DLEGATO_ROOT="${S_L}" -H"${S}/testapp_build/" -B"${S}/testapp_build/build"
    cmake -E env CFLAGS="-O" CC="${CC} -O" CXX="${CXX} -O" cmake --build "${S}/testapp_build/build"
}

do_compile[depends] += "refpolicy-mls-auto:do_install"
do_compile() {
    set -e
    export_common_env

    if ${@bb.utils.contains('PACKAGECONFIG', 'image', 'true', 'false', d)}; then
        build_telaf_image
    fi

    if ${@bb.utils.contains('PACKAGECONFIG', 'testapps', 'true', 'false', d)}; then
        build_testapps
    fi
}

do_install[noexec] = "1"

SYSROOT_PREPROCESS_FUNCS += "telaf_populate_sysroot"
telaf_populate_sysroot() {
    sysroot_stage_dir ${S}                                                         ${SYSROOT_DESTDIR}/telaf/telaf-test/
    sysroot_stage_dir ${S}/build/${MACHINE}/_staging_system.${MACHINE}.update_ro   ${SYSROOT_DESTDIR}/telaf/staging/test/
}

do_deploy() {
    if ${@bb.utils.contains('PACKAGECONFIG', 'testapps', 'true', 'false', d)}; then
        install -d ${DEPLOYDIR}/telaf-images/testapps/Unit_testapp
        install -d ${DEPLOYDIR}/telaf-images/testapps/Integration_testapp
        install -d ${DEPLOYDIR}/telaf-images/testapps/Console_testapp
        cp -rf ${S}/testapp_build/build/TestApps/Unit_testapp/* \
            ${DEPLOYDIR}/telaf-images/testapps/Unit_testapp/
        cp -rf ${S}/testapp_build/build/TestApps/Integration_testapp/* \
            ${DEPLOYDIR}/telaf-images/testapps/Integration_testapp/
        cp -rf ${S}/testapp_build/build/TestApps/Console_testapp/* \
            ${DEPLOYDIR}/telaf-images/testapps/Console_testapp/
    fi
}
addtask deploy after do_compile before do_build

