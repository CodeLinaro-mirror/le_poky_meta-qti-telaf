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
FILES:${PN}-dbg += "/telaf/default-pa/.build-id"
INSANE_SKIP:${PN}-dbg += "buildpaths"

# The CMake build (split_debug_symbols_strip_binaries) already strips these
# libraries and stores debug info under .build-id/, so skip bitbake's own
# split/strip QA for the main package.
INSANE_SKIP:${PN} += "already-stripped"
INHIBIT_PACKAGE_STRIP = "1"
INHIBIT_PACKAGE_DEBUG_SPLIT = "1"

SYSROOT_DIRS:append = " /telaf"

do_compile:prepend() {
    # Clean up old build directory if it exists
    if [ -d "${S}/build" ]; then
        rm -rf "${S}/build"
    fi
}

do_install() {
    cmake --install "${B}" --prefix "${S}/build/staging"
    set -eu
    install -d "${D}/telaf/default-pa/lib"

    if ls "${S}/build/staging/lib/"*.so* >/dev/null 2>&1; then
        cp -a --no-preserve=ownership "${S}/build/staging/lib/"*.so* "${D}/telaf/default-pa/lib/"
    else
        bbwarn "No shared libraries (*.so*) produced in ${S}/build/staging/lib"
    fi

    if [ -d "${S}/build/staging/.build-id" ]; then
        install -d "${D}/telaf/default-pa/.build-id"
        cp -a --no-preserve=ownership "${S}/build/staging/.build-id/." "${D}/telaf/default-pa/.build-id/"
    else
        bbwarn "No .build-id directory found in ${S}/build/staging"
    fi
}

RM_WORK_EXCLUDE += "telaf-pa-default-build"
