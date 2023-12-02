FILESEXTRAPATHS_prepend := "${THISDIR}/files:"

SRC_URI_append += "file://lxc_telaf.conf"
SRC_URI_append += "file://telaf_lxc_env.sh"
SRC_URI_append += "file://telaf_lxc.sh"

do_install_append() {
   # Replace the default lxc configuration file
   install -m 0644 ${WORKDIR}/lxc_telaf.conf ${D}${sysconfdir}/lxc/lxc_telaf.conf
   install -m 0755 ${WORKDIR}/telaf_lxc_env.sh ${D}${sysconfdir}/lxc/telaf_lxc_env.sh
   install -m 0755 ${WORKDIR}/telaf_lxc.sh ${D}${sysconfdir}/lxc/telaf_lxc.sh
}