DESCRIPTION = "TelAF Vendor Build"
HOMEPAGE = "https://git.codelinaro.org"
LICENSE = "BSD-3-Clause-Clear"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/${LICENSE};md5=7a434440b651f4a472ca93716d01033a"

inherit systemd pkgconfig deploy qprebuilt

DEPENDS += "telaf-build"
DEPENDS += "ninja-native"

FILESPATH = "${WORKSPACE}:"
SRC_DIR = "${WORKSPACE}/"
SRC_URI = "file://telaf"

S = "${WORKDIR}"

SRC="${S}/telaf/apps/sample/plugin/configStorage"

INSANE_SKIP:${PN} += "installed-vs-shipped"

do_compile() {
    export LEGATO_ROOT=${RECIPE_SYSROOT}/telaf/legato/legato-af
    export TELAF_ROOT=${RECIPE_SYSROOT}/telaf/telaf

    cd ${LEGATO_ROOT} && source ${LEGATO_ROOT}/build/${MACHINE}/config.sh && \
    source ${LEGATO_ROOT}/bin/configlegatoenv && cd ${SRC}
    mkcomp -t ${MACHINE} -X -O2 -C -O2 -X "-fPIC" -X "-shared" -X "-std=c++11" -i ${TELAF_ROOT}/interfaces -o ${SRC}/tafPiCfgStor.so -s ${SRC} ./
}

do_install() {
    install -d ${D}/vendor
    install -m 0755 -D ${SRC}/*.so -D ${D}/vendor/
}

SYSROOT_PREPROCESS_FUNCS += "telaf_populate_sysroot"
telaf_populate_sysroot() {
    install -d ${SYSROOT_DESTDIR}/telaf/vendor/
    install -m 0755 ${D}/vendor/*.so ${SYSROOT_DESTDIR}/telaf/vendor/
}
