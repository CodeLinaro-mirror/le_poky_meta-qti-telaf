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

PR = "r1"

S = "${WORKDIR}/telaf-image/stage"

do_configure() {
    cp -rf ${RECIPE_SYSROOT}/telaf/* ${S}
}

do_compile() {
    export LEGATO_ROOT=${S}/legato/legato-af
    export TELAF_ROOT=${S}/telaf
    export TELAF_PROP=${S}/telaf-prop
    export TELAF_NOSHIP=${S}/telaf-noship
    export WORK_ROOT=${WORKDIR}
    ${TELAF_ROOT}/mkimg.sh ${MACHINE} ${S}
}

do_deploy() {
    mkdir -p ${DEPLOY_DIR_IMAGE}
    install ${S}/telaf_ro.squashfs.ubi ${DEPLOY_DIR_IMAGE}/
    install ${S}/telaf_ro.squashfs ${DEPLOY_DIR_IMAGE}/
}
do_deploy[dirs] = "${S} ${DEPLOYDIR}"
addtask deploy before do_build after do_install
