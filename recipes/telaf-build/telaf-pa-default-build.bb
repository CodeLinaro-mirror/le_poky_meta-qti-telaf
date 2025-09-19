SUMMARY = "Platform Adaptor for Telematics Application Framework"
DESCRIPTION = "Platform Adaptor for Telematics Application Framework"
HOMEPAGE = "https://www.codelinaro.org/"
LICENSE = "BSD-3-Clause-Clear"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/BSD-3-Clause-Clear;md5=7a434440b651f4a472ca93716d01033a"

inherit cmake pkgconfig

FILESPATH =+ "${WORKSPACE}:"
SRC_URI += "file://telaf-pa-default/"
SRC_DIR = "${WORKSPACE}/telaf-pa-default/"

S = "${WORKDIR}/telaf-pa-default"

# Allow empty main package to avoid packaging errors
ALLOW_EMPTY:${PN} = "1"

# Include all installed shared libraries in the package
# Package layout
FILES:${PN} = "/telaf/default-pa/lib/*.so.*"
FILES:${PN}-dev += "/telaf/default-pa/lib/*.so"
INSANE_SKIP:${PN}-dbg += "buildpaths"

SYSROOT_DIRS:append = " /telaf"

do_install() {
    set -eu
    install -d "${D}/telaf/default-pa/lib"

    if ls "${B}"/*.so* >/dev/null 2>&1; then
        cp -a --no-preserve=ownership "${B}"/*.so* "${D}/telaf/default-pa/lib/"
    else
        bbwarn "No shared libraries (*.so*) produced in ${B}"
    fi
}

RM_WORK_EXCLUDE += "telaf-default-pa-build"
