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
S_L = "${WORKDIR}/legato"

PARALLEL_MAKE = ""

do_configure() {
    # Make relative link to access stubs
    cd ${S}
    ln -sf "./stub" "./components/tafSMSSvc/taf_pa_sms"
}

do_compile() {
    oe_runmake distclean
    oe_runmake ${MACHINE}
}

do_install[noexec] = "1"

SYSROOT_PREPROCESS_FUNCS += "telaf_populate_sysroot"
telaf_populate_sysroot() {
    sysroot_stage_dir ${S_L} ${SYSROOT_DESTDIR}/telaf/legato/
    sysroot_stage_dir ${S} ${SYSROOT_DESTDIR}/telaf/telaf/

}

