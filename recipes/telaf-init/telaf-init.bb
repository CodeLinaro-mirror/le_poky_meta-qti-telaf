DESCRIPTION = "TalAf Initialization"
HOMEPAGE = "https://www.codeaurora.org/"
LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/files/common-licenses/${LICENSE};md5=550794465ba0ec5312d6919e203a55f9"

SRC_URI = "file://start_telaf.sh"
SRC_URI += "file://telaf.rules"
SRC_URI += "file://telaf.service"
SRC_URI += "file://telaf.app.service"
SRC_URI += "file://telaf.env"
SRC_URI += "file://telaf-ubi-mount.sh"
SRC_URI += "file://telaf.mount.service"
SRC_URI += "file://overlay_selinuxrw-workdir.sh"
SRC_URI += "file://overlay_selinuxrw-workdir.service"

FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

do_compile[noexec] = "1"
S = "${WORKDIR}"

INITSCRIPT_NAME = "start_telaf.sh"
INITSCRIPT_PARAMS = "start 99 2 3 4 5 . stop 1 0 1 6 ."

dirs755:append = " /legato /mnt/legato /telaf_app"

do_install() {
    if ${@bb.utils.contains('DISTRO_FEATURES', 'systemd', 'true', 'false', d)}; then
        # Install telaf partition mounting service
        install -d 0644 ${D}${sysconfdir}/initscripts
        install -d 0644 ${D}${systemd_unitdir}/system
        install -d ${D}/${systemd_unitdir}/system/multi-user.target.wants
        install -m 0555 ${WORKDIR}/telaf-ubi-mount.sh ${D}${sysconfdir}/initscripts/telaf-ubi-mount.sh
        install -m 0644 ${WORKDIR}/telaf.mount.service ${D}${systemd_unitdir}/system/telaf.mount.service
        ln -sf ${systemd_unitdir}/system/telaf.mount.service ${D}${systemd_unitdir}/system/multi-user.target.wants/telaf.mount.service

        # Install telaf initscript Service
        install -d ${D}${sysconfdir}/udev/rules.d/
        install -d ${D}${sysconfdir}/tmpfiles.d
        install -m 0644 ${S}/telaf.rules -D ${D}${sysconfdir}/udev/rules.d/telaf.rules
        install -m 0644 ${S}/telaf.service -D ${D}${systemd_unitdir}/system/telaf.service
        ln -sf ${systemd_unitdir}/system/telaf.service ${D}${systemd_unitdir}/system/multi-user.target.wants/telaf.service

        install -m 0644 ${S}/telaf.app.service -D ${D}${systemd_unitdir}/system/telaf.app.service
        ln -sf ${systemd_unitdir}/system/telaf.app.service ${D}${systemd_unitdir}/system/multi-user.target.wants/telaf.app.service
        install -m 0644 ${S}/telaf.env -D ${D}${sysconfdir}/telaf.env
        install -m 0555 ${S}/start_telaf.sh -D ${D}${sysconfdir}/init.d/start_telaf.sh

        # create the directories which are used by telaf
        install -m 0755 -d ${D}/legato
        install -m 0755 -d ${D}/mnt/legato
        touch ${D}${sysconfdir}/ld.so.cache

        # create the directories which are used by telaf app installation
        install -m 0755 -d ${D}/app

        # Create directory used by telaf Managed Connectivity Service to store configuration file
        install -m 0755 -d ${D}${userfsdatadir}/ManagedServices

        # create directories with DAC permission for non root users
        install -m 0775 -d ${D}${userfsdatadir}/le_fs
        chown -h telaf.telaf ${D}${userfsdatadir}/le_fs
        install -m 0777 -d ${D}${userfsdatadir}/persist/tafKeyStoreSvc
        install -m 0777 -d ${D}${userfsdatadir}/persist/tafKeyStoreSvc/internalKey

        # Install SELinux overlay service
        install -m 0640 ${WORKDIR}/overlay_selinuxrw-workdir.service ${D}${systemd_unitdir}/system/overlay_selinuxrw-workdir.service
        install -m 0555 ${WORKDIR}/overlay_selinuxrw-workdir.sh ${D}${sysconfdir}/initscripts/overlay_selinuxrw-workdir.sh
        ln -sf ${systemd_unitdir}/system/overlay_selinuxrw-workdir.service ${D}${systemd_unitdir}/system/multi-user.target.wants/overlay_selinuxrw-workdir.service
    fi

}

FILES:${PN} += "${systemd_unitdir}/system/* /legato /mnt/legato /app ${userfsdatadir}/persist/* ${userfsdatadir}/le_fs ${userfsdatadir}/ManagedServices"

# Add default telaf users
inherit useradd
USERADD_PACKAGES = "${PN}"
USERADD_PARAM:${PN} += "-M -U telaf;"
USERADD_PARAM:${PN} += "-M -U appdefault;"
USERADD_PARAM:${PN} += "-M -g root securityunpack;"
# Default service users
USERADD_PARAM:${PN} += "-M -U tafaudiosvc;"
USERADD_PARAM:${PN} += "-M -U tafcansvc;"
USERADD_PARAM:${PN} += "-M -U tafdatacallsvc;"
USERADD_PARAM:${PN} += "-M -U tafdevinfosvc;"
USERADD_PARAM:${PN} += "-M -U tafdiagsvc;"
USERADD_PARAM:${PN} += "-M -U tafhmsvc;"
USERADD_PARAM:${PN} += "-M -U tafivssinfosvc;"
USERADD_PARAM:${PN} += "-M -U tafivssradiosvc;"
USERADD_PARAM:${PN} += "-M -U tafivsssimsvc;"
USERADD_PARAM:${PN} += "-M -U tafkeystoresvc;"
USERADD_PARAM:${PN} += "-M -U taflocationsvc;"
USERADD_PARAM:${PN} += "-M -U taflxcinitapp;"
USERADD_PARAM:${PN} += "-M -U tafmrcsvc;"
USERADD_PARAM:${PN} += "-M -U tafmngdaudiosvc;"
USERADD_PARAM:${PN} += "-M -U tafmngdconnsvc;"
USERADD_PARAM:${PN} += "-M -U tafmngdpmsvc;"
USERADD_PARAM:${PN} += "-M -U tafnetsvc;"
USERADD_PARAM:${PN} += "-M -U tafpmsvc;"
USERADD_PARAM:${PN} += "-M -U tafradiosvc;"
USERADD_PARAM:${PN} += "-M -U tafremotesimsvc;"
USERADD_PARAM:${PN} += "-M -U tafrpcproxy;"
USERADD_PARAM:${PN} += "-M -U tafsmssvc;"
USERADD_PARAM:${PN} += "-M -U tafsimcardsvc;"
USERADD_PARAM:${PN} += "-M -U tafsomeipgwsvc;"
USERADD_PARAM:${PN} += "-M -U tafthermsvc;"
USERADD_PARAM:${PN} += "-M -U taftimesvc;"
USERADD_PARAM:${PN} += "-M -U tafupdatesvc;"
USERADD_PARAM:${PN} += "-M -U tafvoicecallsvc;"
# TelAF Reserved Users
USERADD_PARAM:${PN} += "-M -U taftestapp;"
USERADD_PARAM:${PN} += "-M -U tafsampleapp;"
USERADD_PARAM:${PN} += "-M -U tafusr0;"
USERADD_PARAM:${PN} += "-M -U tafusr1;"
USERADD_PARAM:${PN} += "-M -U tafusr2;"
USERADD_PARAM:${PN} += "-M -U tafusr3;"
