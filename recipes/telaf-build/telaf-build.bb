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

DEPENDS += "vsomeip"

DEPENDS += "refpolicy-mls"

FILESPATH =+ "${WORKSPACE}:"
SRC_URI += "file://telaf/"
SRC_URI += "file://legato/"

S = "${WORKDIR}/telaf"
S_L = "${WORKDIR}/legato"

PARALLEL_MAKE = ""

do_compile() {
    export WORK_ROOT=${WORKDIR}
    export OECORE_TARGET_SYSROOT=${RECIPE_SYSROOT}
    if [ -f ${S}/VERSION ]; then
        export LEGATO_VERSION=`cat ${S}/VERSION 2>/dev/null`
    fi
    oe_runmake distclean
    oe_runmake ${MACHINE}
}

do_install_append() {
    install -d ${D}/${libdir}/pkgconfig

    # Replace "{MACHINE}" with machine type
    #TELAF_PC_FILE="${S}/telaf.pc"
    #TELAF_PC_CONTENT=$(cat "${TELAF_PC_FILE}")
    #TELAF_PC_CONTENT=${TELAF_PC_CONTENT//\$\{MACHINE\}/${MACHINE}}
    #echo "${TELAF_PC_CONTENT}" > "${D}/${libdir}/pkgconfig/telaf.pc"
}

SYSROOT_PREPROCESS_FUNCS += "telaf_populate_sysroot"
telaf_populate_sysroot() {
    sysroot_stage_dir ${S_L} ${SYSROOT_DESTDIR}/telaf/legato/
    sysroot_stage_dir ${S} ${SYSROOT_DESTDIR}/telaf/telaf/
}

