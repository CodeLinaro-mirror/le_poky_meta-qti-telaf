FILESEXTRAPATHS_append := "${THISDIR}:"

SRC_URI += "file://telaf-sepolicy/common/ \
            file://telaf-sepolicy/${BASEMACHINE}/ "

do_patch_append() {
    install_device_policy(d, os.path.join("telaf-sepolicy", "common"))
    if os.path.exists(os.path.join(d.getVar("WORKDIR"), "telaf-sepolicy", d.getVar("BASEMACHINE"))):
        install_device_policy(d, os.path.join("telaf-sepolicy", d.getVar("BASEMACHINE")))
}

