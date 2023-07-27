DESCRIPTION = "TalAf Initialization"
HOMEPAGE = "https://www.codeaurora.org/"
LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/files/common-licenses/${LICENSE};md5=550794465ba0ec5312d6919e203a55f9"

SRC_URI = "file://start_telaf.sh"
SRC_URI += "file://telaf.rules"
SRC_URI += "file://telaf.service"
SRC_URI += "file://telaf.env"
SRC_URI += "file://telaf-ubi-mount.sh"
SRC_URI += "file://telaf.mount.service"

FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}:"

do_compile[noexec] = "1"
S = "${WORKDIR}"

INITSCRIPT_NAME = "start_telaf.sh"
INITSCRIPT_PARAMS = "start 99 2 3 4 5 . stop 1 0 1 6 ."

dirs755_append = " /legato /mnt/legato /telaf_app"

do_install() {
    if ${@bb.utils.contains('DISTRO_FEATURES', 'systemd', 'true', 'false', d)}; then
        # Install telaf partition mounting service
        install -d 0644 ${D}${sysconfdir}/initscripts
        install -d 0644 ${D}${systemd_unitdir}/system
        install -d ${D}/${systemd_unitdir}/system/multi-user.target.wants
        install -m 0744 ${WORKDIR}/telaf-ubi-mount.sh ${D}${sysconfdir}/initscripts/telaf-ubi-mount.sh
        install -m 0644 ${WORKDIR}/telaf.mount.service ${D}${systemd_unitdir}/system/telaf.mount.service
        ln -sf ${systemd_unitdir}/system/telaf.mount.service ${D}${systemd_unitdir}/system/multi-user.target.wants/telaf.mount.service

        # Install telaf initscript Service
        install -d ${D}${sysconfdir}/udev/rules.d/
        install -d ${D}${sysconfdir}/tmpfiles.d
        install -m 0644 ${S}/telaf.rules -D ${D}${sysconfdir}/udev/rules.d/telaf.rules
        install -m 0644 ${S}/telaf.service -D ${D}${systemd_unitdir}/system/telaf.service
        ln -sf ${systemd_unitdir}/system/telaf.service ${D}${systemd_unitdir}/system/multi-user.target.wants/telaf.service
        install -m 0644 ${S}/telaf.env -D ${D}${sysconfdir}/telaf.env
        install -m 0755 ${S}/start_telaf.sh -D ${D}${sysconfdir}/init.d/start_telaf.sh

        # create the directories which are used by telaf
        install -m 0755 -d ${D}/legato
        install -m 0755 -d ${D}/mnt/legato
        touch ${D}${sysconfdir}/ld.so.cache

        # create the directories which are used by telaf app installation
        install -m 0755 -d ${D}/app

        # Create directory used by telaf Managed Connectivity Service to store configuration file
        install -m 0777 -d ${D}${userfsdatadir}/ManagedServices

        # create directories with DAC permission for non root users
        install -m 0777 -d ${D}${userfsdatadir}/le_fs
        install -m 0777 -d ${D}${userfsdatadir}/persist/tafKeyStoreSvc
        install -m 0777 -d ${D}${userfsdatadir}/persist/tafKeyStoreSvc/internalKey
    fi

}

FILES_${PN} += "${systemd_unitdir}/system/* /legato /mnt/legato /app ${userfsdatadir}/persist/* ${userfsdatadir}/le_fs ${userfsdatadir}/ManagedServices"

# Add default telaf users
inherit useradd
USERADD_PACKAGES = "${PN}"
USERADD_PARAM_${PN} += "-M -U telaf;"
USERADD_PARAM_${PN} += "-M -U appdefault;"
USERADD_PARAM_${PN} += "-M -g root securityunpack;"

