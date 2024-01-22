SUMMARY = "build the vsomeip package"
LICENSE = "MPL-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=9741c346eef56131163e13b9db1241b3"

inherit cmake
DEPENDS = "boost"

# fetch the version 3.4.9-r1 from github
SRCREV = "91805d8a24e3f3c63ce72d6eee09a6ee703eff7d"
SRC_URI = "git://github.com/COVESA/vsomeip.git;protocol=https \
           file://0001-Fix-bug-to-support-multiple-routing-managers-in-sing.patch \
           "
S = "${WORKDIR}/git"

# enable multiple routing managers
EXTRA_OECMAKE += "-DENABLE_MULTIPLE_ROUTING_MANAGERS=1"

BBCLASSEXTEND = "nativesdk"
