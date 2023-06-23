inherit systemd pkgconfig deploy
DESCRIPTION = "Telematics Test Applications"

HOMEPAGE = "https://www.codelinaro.org/"
LICENSE = "BSD-3-Clause & BSD-3-Clause-Clear"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/BSD-3-Clause;md5=550794465ba0ec5312d6919e203a55f9 \
    file://${COREBASE}/meta-qti-bsp/files/common-licenses/BSD-3-Clause-Clear;md5=48b43ba58d0f8e9ef3704313a46b7a43"

# Host dependencies
DEPENDS += "telaf-build"
DEPENDS += "ninja-native"
DEPENDS += "cmake-native"
DEPENDS += "coreutils-native"

# Target dependencies
DEPENDS += "openssl"
DEPENDS += "libxml2"
DEPENDS += "xmllib"
DEPENDS += "telux"
DEPENDS += "telux-lib"

PR = "r1"
DEBUG_BUILD="1"
FILESPATH =+ "${WORKSPACE}:"


do_configure[noexec] = "1"

S = "${RECIPE_SYSROOT}/telaf/telaf"
S_L = "${RECIPE_SYSROOT}/telaf/legato/legato-af"

do_compile() {
    export TARGET=${MACHINE}
    export LEGATO_ROOT=${RECIPE_SYSROOT}/telaf/legato/legato-af
    export TELAF_ROOT=${RECIPE_SYSROOT}/telaf/telaf
    export OECORE_TARGET_SYSROOT=${RECIPE_SYSROOT}
    cmake -DLEGATO_TARGET=${MACHINE} -DLEGATO_ROOT=${S_L} -H${S}/testapp_build/ -B${S}/testapp_build/build
    cmake -E env CFLAGS=" -O " cmake -E env CC="${CC} -O " cmake -E env CXX="${CXX} -O " cmake --build ${S}/testapp_build/build
}

do_install() {
    mkdir -p ${DEPLOY_DIR_IMAGE}/Testapps/Unit_testapp
    mkdir -p ${DEPLOY_DIR_IMAGE}/Testapps/Integration_testapp
    mkdir -p ${DEPLOY_DIR_IMAGE}/Testapps/Console_testapp
    install -m  0644  ${S}/testapp_build/build/TestApps/Unit_testapp/* -D ${DEPLOY_DIR_IMAGE}/Testapps/Unit_testapp
    install -m  0644  ${S}/testapp_build/build/TestApps/Integration_testapp/* -D ${DEPLOY_DIR_IMAGE}/Testapps/Integration_testapp
    install -m  0644  ${S}/testapp_build/build/TestApps/Console_testapp/* -D ${DEPLOY_DIR_IMAGE}/Testapps/Console_testapp

}


