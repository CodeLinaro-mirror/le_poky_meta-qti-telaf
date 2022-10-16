HOMEPAGE = "http://www.boost.org/"
LICENSE = "BSL-1.0"
LIC_FILES_CHKSUM = "file://LICENSE_1_0.txt;md5=e4224ccaecb14d942c71d31bef20d78c"

INSANE_SKIP = "file-rdeps"

# convert the version string to boost_1_76_0
VER = "${@"_".join(d.getVar("PV", True).split("."))}"
DIST = "boost_${VER}"

# don't build additional package
PACKAGECONFIG=""

SRC_URI = "${SOURCEFORGE_MIRROR}/boost/${DIST}.tar.bz2"
SRC_URI[sha256sum] = "f0397ba6e982c4450f27bf32a2a83292aba035b827a5623a14636ea583318c41"
S = "${WORKDIR}/${DIST}"
