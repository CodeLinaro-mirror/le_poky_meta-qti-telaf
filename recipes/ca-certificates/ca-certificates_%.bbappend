do_install:append() {
    sed -i '/DST_Root_CA_X3.crt/d' ${D}${sysconfdir}/ca-certificates.conf
}
