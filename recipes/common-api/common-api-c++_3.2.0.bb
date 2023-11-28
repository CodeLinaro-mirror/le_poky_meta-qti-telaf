SUMMARY = "CommonAPI"
SECTION = "libs"
LICENSE = "MPLv2"
LIC_FILES_CHKSUM = "file://LICENSE;md5=815ca599c9df247a0c7f619bab123dad"

PROVIDES = "commonapi3"
PR = "r0"

inherit cmake lib_package pkgconfig

SRCREV = "89720d3c63bbd22cbccc80cdc92c2f2dd20193ba"
SRC_URI = "git://git.codelinaro.org/clo/la/platform/external/capicxx-core-runtime.git;protocol=https;branch=github/master \
    "
S = "${WORKDIR}/git"

EXTRA_OECMAKE += "-DCMAKE_INSTALL_PREFIX=/usr \
    -DINSTALL_LIB_DIR:PATH=${baselib} \
    -DINSTALL_CMAKE_DIR:PATH=${baselib}/cmake/CommonAPI \
    "

