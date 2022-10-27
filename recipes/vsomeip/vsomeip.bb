SUMMARY = "build the vsomeip package"
LICENSE = "MPL-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=815ca599c9df247a0c7f619bab123dad"

inherit cmake
DEPENDS = "boost"

# fetch the version 3.2.20.3 from github
SRCREV = "17cc55f24d1c56f6a5dcca6065a227ca91d01c90"
SRC_URI = "git://github.com/COVESA/vsomeip.git;protocol=https"
S = "${WORKDIR}/git"

BBCLASSEXTEND = "nativesdk"