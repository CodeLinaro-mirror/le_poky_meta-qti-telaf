FILESEXTRAPATHS_append := "${THISDIR}:"
SRC_URI += "file://telaf-sepolicy/common/ \
            file://telaf-sepolicy/${BASEMACHINE}/ "

DEPENDS += "telaf-build"

do_patch_append() {
    # import common TelAF framework policies
    install_device_policy(d, os.path.join("telaf-sepolicy", "common"))

    # import sa515m specified TelAF framework policies
    if os.path.exists(os.path.join(d.getVar("WORKDIR"), "telaf-sepolicy", d.getVar("BASEMACHINE"))):
        install_device_policy(d, os.path.join("telaf-sepolicy", d.getVar("BASEMACHINE")))
}

do_compile_prepend() {
    DST_DIR=${S}/policy/modules/device
    TELAF_ROOT=${RECIPE_SYSROOT}/telaf/telaf
    SDEF_FILE=${RECIPE_SYSROOT}/telaf/telaf/modules/TelSdk/${MACHINE}.sdef
    COMPS=$(cat ${SDEF_FILE} | awk '{print $1}' | grep "^\$TELAF_ROOT" | awk -F "/" '{print $NF}')
    for COMP in ${COMPS}; do
        SRC_DIR=${TELAF_ROOT}/components/${COMP}/selinux
        if [ -d ${SRC_DIR} ]; then
            cp -fr ${SRC_DIR}/* ${DST_DIR}/
        fi
    done
}