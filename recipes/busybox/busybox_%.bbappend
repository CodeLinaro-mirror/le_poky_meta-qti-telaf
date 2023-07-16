FILESEXTRAPATHS_prepend := "${THISDIR}/files:"

SRC_URI += "\
            file://syslog-startup.conf \
            file://0001-Support-both-buffer-mode-and-file-mode.patch \
"

# enable shared memory for logread, which is used by TelAf logs
do_install_append() {
    install -d ${D}${sysconfdir}/udev/syslog-startup.conf
    install -m 0744 ${WORKDIR}/syslog-startup.conf ${D}${sysconfdir}/syslog-startup.conf
    install -d ${D}${sysconfdir}/initscripts
    install -m 0755 ${WORKDIR}/syslog ${D}${sysconfdir}/initscripts/syslog
    sed -i 's/syslogd -n/syslogd -- -n/' ${D}${sysconfdir}/initscripts/syslog
}
