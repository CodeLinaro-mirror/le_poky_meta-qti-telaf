SUMMARY = "Platform Adaptor for Telematics Application Framework"
DESCRIPTION = "Platform Adaptor for Telematics Application Framework"
HOMEPAGE = "https://www.codeaurora.org/"
LICENSE = "BSD-3-Clause-Clear"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/BSD-3-Clause-Clear;md5=7a434440b651f4a472ca93716d01033a"

inherit cmake pkgconfig
DEPENDS += "openssl libxml2 xmllib telux telux-lib"

FILESPATH =+ "${WORKSPACE}:"
SRC_URI = "file://telaf-pa/"
SRC_DIR = "${WORKSPACE}/telaf-pa/"

S = "${WORKDIR}/telaf-pa"

# Allow empty main package to avoid packaging errors
ALLOW_EMPTY:${PN} = "1"

# Include all installed shared libraries in the package
# Package layout
FILES:${PN} = "/telaf/target-pa/lib/*.so.*"
FILES:${PN}-dev += "/telaf/target-pa/lib/*.so"
INSANE_SKIP:${PN}-dbg += "buildpaths"



SYSROOT_DIRS:append = " /telaf"

do_install() {
    set -eu
    install -d "${D}/telaf/target-pa/lib"

    if ls "${B}"/*.so* >/dev/null 2>&1; then
        cp -a --no-preserve=ownership "${B}"/*.so* "${D}/telaf/target-pa/lib/"
    else
        bbwarn "No shared libraries (*.so*) produced in ${B}"
    fi
}

RM_WORK_EXCLUDE += "telaf-target-pa-build"
