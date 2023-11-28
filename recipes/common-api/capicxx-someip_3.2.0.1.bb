SUMMARY = "Common API C++ SOME/IP generator"

LAUNCHER_BASE = "commonapi-someip-generator"
LAUNCHER_LINK = "capicxx-someip-gen"

require capicxx-native.inc

SRC_URI = "https://github.com/GENIVI/${_BPN}-tools/releases/download/${PV}/commonapi_someip_generator.zip"
SRC_URI[md5sum] = "ac3eb5d83d35bb57e93c8f03cac2c989"
SRC_URI[sha256sum] = "6e83a48db2e81fcdf774d7aa7472877bbcc18c52964cd125b08e5690d634fe4f"
