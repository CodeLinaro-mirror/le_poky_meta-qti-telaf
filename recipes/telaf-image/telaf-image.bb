inherit systemd pkgconfig deploy

DESCRIPTION = "Telematics Application Framework"
HOMEPAGE = "https://www.codeaurora.org/"
LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/files/common-licenses/${LICENSE};md5=550794465ba0ec5312d6919e203a55f9"

DEPENDS += "ninja-native cmake-native coreutils-native squashfs-tools-native mtd-utils-native telaf-build openssl libxml2 xmllib telux telux-lib telaf-pa-build"

python __anonymous() {
    if d.getVar('HAS_TELAF_PROP', True) == 'true':
        d.appendVar('DEPENDS', ' telaf-prop-build')
    if d.getVar('HAS_TELAF_NOSHIP', True) == 'true':
        d.appendVar('DEPENDS', ' telaf-noship-build')
}

# If telaf-vendor-build is needed, uncomment the following line
# DEPENDS += "telaf-vendor-build"

RM_WORK_EXCLUDE += "telaf-image"

S = "${WORKDIR}/telaf-image/stage"
TELAF_TARGET_STAGE_DIR = "${S}/legato/legato-af/build/${MACHINE}/_staging_system.${MACHINE}.update_ro"
TELAF_SELINUX_FILE_CONTEXTS = "${S}/telaf/security/selinux/sepolicy/files/file_contexts"

do_configure() {
    if [ -d "${S}" ]; then
        rm -fr ${S}/*
    fi
    cp -rf ${RECIPE_SYSROOT}/telaf/* ${S}
}

set_environment_variables() {
    export LEGATO_ROOT="${S}/legato/legato-af"
    export TELAF_ROOT="${S}/telaf"
    export TELAF_PROP="${S}/telaf-prop"
    export TELAF_NOSHIP="${S}/telaf-noship"
    export TELAF_PA="${S}/telaf-pa"
    export WORK_ROOT="${WORKDIR}"
    export VENDOR_ROOT="${S}/vendor"
}

do_compile() {
    set_environment_variables

    ${TELAF_ROOT}/mkimg.sh ${MACHINE} ${S}
    ${TELAF_ROOT}/bin/createsdk ${MACHINE} ${S}
}

do_deploy() {
    rm -rf ${DEPLOYDIR}/telaf-images
    mkdir -p ${DEPLOYDIR}/telaf-images/security/selinux/sepolicy/files
    cp -r -d --preserve=mode,xattr,links ${TELAF_TARGET_STAGE_DIR} ${DEPLOYDIR}/telaf-images/telaf_ro
    cp -rf ${TELAF_SELINUX_FILE_CONTEXTS} ${DEPLOYDIR}/telaf-images/security/selinux/sepolicy/files/

    # Deploy the telaf-sdk-[telaf-version].tar.bz2 to $DEPLOYDIR directory
    install ${S}/telaf/build/${MACHINE}/telaf-sdk* ${DEPLOYDIR}/
}
do_deploy[dirs] = "${S} ${DEPLOYDIR}"
addtask deploy before do_build after do_install
