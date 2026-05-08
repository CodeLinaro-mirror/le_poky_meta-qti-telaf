SUMMARY = "build the vsomeip package"
LICENSE = "MPL-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=9741c346eef56131163e13b9db1241b3"

inherit cmake
DEPENDS = "boost"

# fetch the version 3.4.10 from github
SRCREV = "02c199dff8aba814beebe3ca417fd991058fe90c"
SRC_URI = "git://github.com/COVESA/vsomeip.git;protocol=https \
           file://0001-for-3.4.10.patch \
           file://0002-for-3.4.10.patch \
           file://0003-Fix-the-loss-of-availability-event.patch \
           file://0004-add-request_response_delay.patch \
           file://0005-Fix-the-event-loss-if-payload-unchange.patch \
           file://0006-Create-new-train-to-void-duplicate-messages.patch \
           file://0007-Improve-req-resp-delay.patch \
           file://0008-Fix-Multi-Routing-Managers-in-one-Process.patch \
           file://0009-Fix-ECONNRESET-case-state-not-set-to-DEREGISTERED.patch \
          "
S = "${WORKDIR}/git"

# enable multiple routing managers
EXTRA_OECMAKE += "-DENABLE_MULTIPLE_ROUTING_MANAGERS=1"

BBCLASSEXTEND = "nativesdk"
FILES:${PN} = "/usr/"
