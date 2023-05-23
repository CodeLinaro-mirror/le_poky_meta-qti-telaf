inherit systemd pkgconfig deploy
DESCRIPTION = "Telematics Application Framework"

HOMEPAGE = "https://www.codeaurora.org/"
LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/files/common-licenses/${LICENSE};md5=550794465ba0ec5312d6919e203a55f9"

DEPENDS += "ninja-native"
DEPENDS += "cmake-native"
DEPENDS += "coreutils-native"
DEPENDS += "squashfs-tools-native"
DEPENDS += "mtd-utils-native"

DEPENDS += "telaf-build"
def get_depends_prop(d):
    if d.getVar('HAS_TELAF_PROP', True) == 'true':
        return "telaf-prop-build"
    else:
        return ""
DEPENDS += "${@get_depends_prop(d)}"
def get_depends_noship(d):
    if d.getVar('HAS_TELAF_NOSHIP', True) == 'true':
        return "telaf-noship-build"
    else:
        return ""
DEPENDS += "${@get_depends_noship(d)}"

DEPENDS += "openssl"
DEPENDS += "libxml2"
DEPENDS += "xmllib"
DEPENDS += "telux"
DEPENDS += "telux-lib"


S = "${WORKDIR}/telaf-image/stage"
LEGATO_ROOT = "${S}/legato/legato-af"
TELAF_ROOT = "${S}/telaf"
TELAF_TARGET_STAGE_DIR = "${LEGATO_ROOT}/build/${MACHINE}/_staging_system.${MACHINE}.update_ro"
TELAF_SELINUX_FILE_CONTEXTS = "${TELAF_ROOT}/security/selinux/sepolicy/files/file_contexts"

do_configure() {
    if [ -d "${S}" ]; then
        rm -fr ${S}/*
    fi
    cp -rf ${RECIPE_SYSROOT}/telaf/* ${S}
}

do_compile() {
    export LEGATO_ROOT=${LEGATO_ROOT}
    export TELAF_ROOT=${TELAF_ROOT}
    export TELAF_PROP=${S}/telaf-prop
    export TELAF_NOSHIP=${S}/telaf-noship
    export WORK_ROOT=${WORKDIR}
    ${TELAF_ROOT}/mkimg.sh ${MACHINE} ${S}
    ${TELAF_ROOT}/bin/createsdk ${MACHINE} ${S}
}

do_deploy() {
    rm -rf   ${DEPLOY_DIR_IMAGE}/telaf-images
    mkdir -p ${DEPLOY_DIR_IMAGE}/telaf-images/security/selinux/sepolicy/files
    cp -rf   ${TELAF_TARGET_STAGE_DIR} ${DEPLOY_DIR_IMAGE}/telaf-images/telaf_ro
    cp -rf   ${TELAF_SELINUX_FILE_CONTEXTS} ${DEPLOY_DIR_IMAGE}/telaf-images/security/selinux/sepolicy/files/

    # Deploy the telaf-sdk-[telaf-version].tar.bz2 to $DEPLOY_DIR_IMAGE directory
    install ${S}/telaf/build/${MACHINE}/telaf-sdk* ${DEPLOY_DIR_IMAGE}/
}
do_deploy[dirs] = "${S} ${DEPLOYDIR}"
addtask deploy before do_build after do_install
