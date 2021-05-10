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

# Target dependencies
DEPENDS += "openssl"
DEPENDS += "libxml2"
DEPENDS += "xmllib"
DEPENDS += "telux"
DEPENDS += "telux-lib"

PR = "r1"
DEBUG_BUILD="1"
FILESPATH =+ "${WORKSPACE}:"
SRC_URI += "file://telaf/"
SRC_URI += "file://legato/"

S = "${WORKDIR}/telaf"

PARALLEL_MAKE = ""

do_configure[noexec] = "1"

do_compile() {
    oe_runmake distclean
    oe_runmake ${MACHINE}
}

do_install[noexec] = "1"

do_deploy() {
    mkdir -p ${DEPLOY_DIR_IMAGE}
    install ${S}/build/${MACHINE}/telaf_ro.squashfs.ubi ${DEPLOY_DIR_IMAGE}
}
do_deploy[dirs] = "${S} ${DEPLOYDIR}"
addtask deploy before do_build after do_install
