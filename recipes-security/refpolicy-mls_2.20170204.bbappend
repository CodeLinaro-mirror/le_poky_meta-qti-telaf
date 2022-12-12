LICENSE += "& BSD-3-Clause & BSD-3-Clause-Clear"

FILESEXTRAPATHS_append := "${THISDIR}:"
SRC_URI += "file://telaf-sepolicy/common/ \
            file://telaf-sepolicy/${BASEMACHINE}/ "

FILESPATH =+ "${WORKSPACE}:"
SRC_URI += "file://telaf/"

do_patch_append() {
    # import common TelAF framework policies
    install_device_policy(d, os.path.join("telaf-sepolicy", "common"))

    # import sa515m specified TelAF framework policies
    if os.path.exists(os.path.join(d.getVar("WORKDIR"), "telaf-sepolicy", d.getVar("BASEMACHINE"))):
        install_device_policy(d, os.path.join("telaf-sepolicy", d.getVar("BASEMACHINE")))
}

do_compile_prepend() {
    DST_DIR=${S}/policy/modules/device
    POLICY_ROOT=${WORKDIR}/telaf/security/selinux/sepolicy
    SDEF_FILE=${WORKDIR}/telaf/modules/TelSdk/${MACHINE}.sdef
    APPS=$(cat ${SDEF_FILE} | awk '{print $1}' | grep "^\$TELAF_ROOT" | awk -F "/" '{print $NF}')
    for APP in ${APPS}; do
        APP_DIR=`find ${POLICY_ROOT} -type d -name ${APP} | tail -n 1`
        if [ "$(ls -A ${APP_DIR}/component/*.te)" ]; then
            cp -fr ${APP_DIR}/component/* ${DST_DIR}/
        fi
    done
}

POLICY_CUSTOM_BUILDOPT_append = ""nad-telaf ""
