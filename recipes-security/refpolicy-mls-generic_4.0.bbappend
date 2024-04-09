LICENSE += "& BSD-3-Clause & BSD-3-Clause-Clear"

FILESEXTRAPATHS:append := "${THISDIR}:"
SRC_URI += "file://telaf-sepolicy/common/ \
            file://telaf-sepolicy/${BASEMACHINE}/ "

FILESPATH =+ "${WORKSPACE}:"
SRC_URI += "file://telaf/"

do_compile:prepend() {
    DST_DIR=${S}/policy/modules/device
    POLICY_ROOT=${WORKDIR}/telaf/security/selinux/sepolicy

    # import TelAF framework policy
    cp -fr ${POLICY_ROOT}/sys/* ${DST_DIR}/

    # import TelAF application policy
    SDEF_FILE=${WORKDIR}/telaf/modules/TelSdk/${MACHINE}.sdef
    APPS=$(cat ${SDEF_FILE} | awk '{print $1}' | grep "^\$TELAF_ROOT" | awk -F "/" '{print $NF}')
    for APP in ${APPS}; do
        APP_DIR=`find ${POLICY_ROOT} -type d -name ${APP} | tail -n 1`
        if [ "$(ls -A ${APP_DIR}/component/*.te)" ]; then
            cp -fr ${APP_DIR}/component/* ${DST_DIR}/
        fi
    done
}

POLICY_CUSTOM_BUILDOPT:append = ""qti-nad-telaf sa525m kirkstone""

do_install:append() {
    install -d ${TMPDIR}/work-shared
    cp -rf ${D}/usr/share/selinux ${TMPDIR}/work-shared/
}

