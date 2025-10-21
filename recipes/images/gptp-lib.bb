SUMMARY = "gptp lib and header files"
LICENSE = "BSD-3-Clause"

FILESPATH =+ "${WORKSPACE}:"
SRC_URI = "file://external/open-avb/"

S = "${WORKDIR}/external/open-avb/"

DEPENDS += "glib-2.0 libpcap pciutils cmake-native"
EXTRA_OEMAKE += 'CC="${CC}" CFLAGS="${CFLAGS}" LDFLAGS="${LDFLAGS} -shared"'

do_compile() {
    oe_runmake libgptp
}

do_install() {
    install -d ${D}${libdir}
    install -m 0755 ${S}/lib/libgptp/*.so ${D}${libdir}
    install -d ${D}/${includedir}
    install -m 0644 ${S}/lib/libgptp/gptp_helper.h ${D}${includedir}
    bbnote "Installed GPTP libraries to SDK sysroot"
}

RPROVIDES:${PN} += "gptp-lib"
FILES:SOLIBSDEV = ""
FILES:${PN}-dev = "${includedir}"
FILES:${PN} += "${libdir}/libgptp.so"

BBCLASSEXTEND += "native nativesdk"