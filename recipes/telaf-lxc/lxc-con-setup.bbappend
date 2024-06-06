DESCRIPTION = "This recipe is required for copying the files to container to start"

LICENSE = "BSD-3-Clause-Clear"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta-qti-bsp/files/common-licenses/BSD-3-Clause-Clear;md5=3771d4920bd6cdb8cbdf1e8344489ee0"

FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

SRC_URI:append += "file://lxc_telaf_init.sh"

do_install:append() {
   # Add TelAF required directries to LXC container
   install -m 0755 -d ${D}/app
   install -m 0755 -d ${D}/data
   install -m 0755 -d ${D}/persist
   install -m 0755 -d ${D}/mnt/legato
   install -m 0755 -d ${D}/legato

   # Register a init script to rcS for telaf
   install -m 0755 -d ${D}/${sysconfdir}/init.d
   install -m 0755 ${WORKDIR}/lxc_telaf_init.sh ${D}/${sysconfdir}/init.d/S10_lxc_telaf_init.sh
}

FILES:${PN} += "${sysconfdir}/*"
FILES:${PN} += "/app"
FILES:${PN} += "/data"
FILES:${PN} += "/persist"
FILES:${PN} += "/mnt/legato"
FILES:${PN} += "/legato"
