DESCRIPTION = "Building Image for Telematics Application Framework"
HOMEPAGE = "https://www.codeaurora.org/"

LICENSE = "BSD-3-Clause-Clear"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/BSD-3-Clause-Clear;md5=7a434440b651f4a472ca93716d01033a"

inherit deploy features_check

SRC_URI += "file://mkimg.sh"

DEPENDS += " \
    ninja-native \
    cmake-native \
    coreutils-native \
    squashfs-tools-native \
    mtd-utils-native \
    binutils-native \
    openssl \
    libxml2 \
    xmllib \
    telux \
    telux-lib \
    telaf-pa-target-build \
"

# Only add dependency when it is full variant
DEPENDS:append = " ${@bb.utils.contains('BUILD_VARIANT', 'full', 'telaf-build telaf-test-build telaf-pa-default-build', '', d)}"

# Check if meta-qti-telaf-prop exists and check if BUILD_VARIANT is full
PACKAGECONFIG ??= ""
PACKAGECONFIG:append = " ${@('prop') if (d.getVar('BUILD_VARIANT') == 'full' and 'telaf-prop' in (d.getVar('BBFILE_COLLECTIONS') or '')) else ''}"
DEPENDS:append = " ${@' telaf-prop-build' if (d.getVar('BUILD_VARIANT') == 'full' and 'prop' in (d.getVar('PACKAGECONFIG') or '').split()) else ''}"
DEPENDS:append = " ${@' telaf-noship-build' if (d.getVar('BUILD_VARIANT') == 'full' and 'prop' in (d.getVar('PACKAGECONFIG') or '').split()) else ''}"

S = "${WORKDIR}/src"
B = "${WORKDIR}/build"

TELAF_ROOT                         ?= "${S}/telaf"
TELAF_TEST_ROOT                    ?= "${S}/telaf-test"
TELAF_PROP_DIR                     ?= "${S}/telaf-prop"
TELAF_NOSHIP_DIR                   ?= "${S}/telaf-noship"
TELAF_LEGACY_PA_DIR                ?= "${S}/telaf-pa"
VENDOR_ROOT                        ?= "${S}/vendor"
TELAF_TARGET_PA_DIR                ?= "${S}/target-pa"
TELAF_DEFAULT_PA_DIR               ?= "${S}/default-pa"
TELAF_IMAGE_BASIC_STAGING_PROD     ?= "${S}/staging/prod"
TELAF_IMAGE_BASIC_STAGING_TEST     ?= "${S}/staging/test"

TELAF_IMAGE_OUTPUT_PROD            ?= "${S}/prod_img"
TELAF_IMAGE_OUTPUT_TEST            ?= "${S}/test_img"
TELAF_IMAGE_COMBINED_PROD          ?= "${TELAF_IMAGE_OUTPUT_PROD}/staging_combined"
TELAF_IMAGE_COMBINED_TEST          ?= "${TELAF_IMAGE_OUTPUT_TEST}/staging_combined"

TELAF_SELINUX_FILE_CONTEXTS_PROD ?= "${TELAF_ROOT}/security/selinux/sepolicy/files/file_contexts"
TELAF_SELINUX_FILE_CONTEXTS_TEST ?= "${TELAF_TEST_ROOT}/security/selinux/sepolicy/files/file_contexts"

# Only pass default-PA to mkimg in FULL variant. In MINIMAL (PA-only), do not pass -d to skip strong/weak check.
DEFAULT_PA_OPT = "${@('-d \"%s\"' % d.getVar('TELAF_DEFAULT_PA_DIR')) \
                   if d.getVar('BUILD_VARIANT') == 'full' else ''}"

RM_WORK_EXCLUDE += "telaf-image"

do_deploy[depends]    += "attr-native:do_populate_sysroot"

do_deploy[cleandirs]  = "${DEPLOYDIR}/telaf-images"

python do_configure () {
    import os, shutil, subprocess

    sdir    = d.getVar("S")
    sysroot = d.getVar("RECIPE_SYSROOT")

    telaf_sysroot = os.path.join(sysroot, "telaf")
    if not os.path.isdir(telaf_sysroot):
        bb.fatal("Expected TELAF content at: %s (did telaf-build populate sysroot?)" % telaf_sysroot)

    if os.path.isdir(sdir):
        shutil.rmtree(sdir)
    os.makedirs(sdir, exist_ok=True)

    cmd = "set -eu; cp -a --no-preserve=ownership '{}'/* '{}'".format(telaf_sysroot, sdir)
    subprocess.check_call(["/bin/sh", "-c", cmd])
}

do_compile () {
    set -eu

    env OBJCOPY="${OBJCOPY}" STRIP="${STRIP}" \
        "${WORKDIR}/mkimg.sh" \
            -t "${MACHINE}" \
            -o "${TELAF_IMAGE_OUTPUT_PROD}" \
            -r "${TELAF_ROOT}" \
            -s "${TELAF_IMAGE_BASIC_STAGING_PROD}" \
            -a "${TELAF_LEGACY_PA_DIR}" \
            -p "${TELAF_PROP_DIR}" \
            -n "${TELAF_NOSHIP_DIR}" \
            -w "${TELAF_TARGET_PA_DIR}" \
            ${DEFAULT_PA_OPT} \
            -v "${VENDOR_ROOT}"

    env OBJCOPY="${OBJCOPY}" STRIP="${STRIP}" \
        "${WORKDIR}/mkimg.sh" \
            -t "${MACHINE}" \
            -o "${TELAF_IMAGE_OUTPUT_TEST}" \
            -r "${TELAF_TEST_ROOT}" \
            -s "${TELAF_IMAGE_BASIC_STAGING_TEST}" \
            -a "${TELAF_LEGACY_PA_DIR}" \
            -p "${TELAF_PROP_DIR}" \
            -n "${TELAF_NOSHIP_DIR}" \
            -w "${TELAF_TARGET_PA_DIR}" \
            ${DEFAULT_PA_OPT} \
            -v "${VENDOR_ROOT}"

    if [ -x "${TELAF_ROOT}/bin/createsdk" ]; then
        "${TELAF_ROOT}/bin/createsdk" "${MACHINE}" "${S}"
    fi
}

do_install[noexec] = "1"

do_deploy () {
    set -eu

    install -d "${DEPLOYDIR}/telaf-images"
    install -d "${DEPLOYDIR}/telaf-images/security/selinux/sepolicy/files"
    install -d "${DEPLOYDIR}/telaf-images/debug_files"
    install -d "${DEPLOYDIR}/telaf-images/telaf_ro"

    if [ -d "${TELAF_IMAGE_COMBINED_PROD}" ]; then
        cp -a --no-preserve=ownership "${TELAF_IMAGE_COMBINED_PROD}" "${DEPLOYDIR}/telaf-images/telaf_ro/prod/"
    else
        bbwarn "TELAF_IMAGE_COMBINED_PROD not found: ${TELAF_IMAGE_COMBINED_PROD}"
    fi

    if [ -d "${TELAF_IMAGE_COMBINED_TEST}" ]; then
        cp -a --no-preserve=ownership "${TELAF_IMAGE_COMBINED_TEST}" "${DEPLOYDIR}/telaf-images/telaf_ro/test/"
    else
        bbnote "TELAF_IMAGE_COMBINED_TEST not found: ${TELAF_IMAGE_COMBINED_TEST}"
    fi

    if ${@bb.utils.contains('DISTRO_FEATURES', 'selinux', 'true', 'false', d)}; then
        if [ -f "${TELAF_SELINUX_FILE_CONTEXTS_PROD}" ]; then
            install -m 0644 "${TELAF_SELINUX_FILE_CONTEXTS_PROD}" \
                "${DEPLOYDIR}/telaf-images/security/selinux/sepolicy/files/file_contexts_prod"
        else
            bbwarn "SELinux file_contexts not found: ${TELAF_SELINUX_FILE_CONTEXTS_PROD}, creating empty fallback"
            touch "${DEPLOYDIR}/telaf-images/security/selinux/sepolicy/files/file_contexts_prod"
        fi

        if [ -f "${TELAF_SELINUX_FILE_CONTEXTS_TEST}" ]; then
            install -m 0644 "${TELAF_SELINUX_FILE_CONTEXTS_TEST}" \
                "${DEPLOYDIR}/telaf-images/security/selinux/sepolicy/files/file_contexts_test"
        else
            bbwarn "SELinux file_contexts not found: ${TELAF_SELINUX_FILE_CONTEXTS_TEST}, creating empty fallback"
            touch "${DEPLOYDIR}/telaf-images/security/selinux/sepolicy/files/file_contexts_test"
        fi

    else
        bbnote "DISTRO_FEATURES lacks selinux; skipping SELinux contexts deployment."
    fi

    SDK_GLOB="${S}/telaf/build/${MACHINE}/telaf-sdk*"
    set +e
    ls ${SDK_GLOB} >/dev/null 2>&1
    found=$?
    set -e
    if [ $found -eq 0 ]; then
        install -m 0644 ${SDK_GLOB} "${DEPLOYDIR}/"
    else
        bbwarn "No SDK bundle matched: ${SDK_GLOB}"
    fi

    install -d "${DEPLOYDIR}/telaf-images/debug_files/build-id"
    declare -A BUILD_ID_DIRS=(
        ["SERVICE_BUILD_ID_DIR"]="${S}/telaf/build/${MACHINE}/debug/.build-id"
        ["TARGET_PA_BUILD_ID_DIR"]="${S}/target-pa/.build-id"
        ["DEFAULT_PA_BUILD_ID_DIR"]="${S}/default-pa/.build-id"
        ["NOSHIP_PA_BUILD_ID_DIR"]="${S}/telaf-noship/.build-id"
        ["PROP_PA_BUILD_ID_DIR"]="${S}/telaf-prop/.build-id"
    )

    for label in "${!BUILD_ID_DIRS[@]}"; do
        src_dir="${BUILD_ID_DIRS[$label]}"
        if [ -d "${src_dir}" ]; then
            cp -a --no-preserve=ownership "${src_dir}/." \
                "${DEPLOYDIR}/telaf-images/debug_files/build-id/"
            bbnote "Copied build-id debug symbols from ${src_dir} to ${DEPLOYDIR}/telaf-images/debug_files/build-id/ (dir is ${label})"
        else
            bbwarn "Build-id directory not found: ${src_dir} (dir is ${label})"
        fi
    done

    build_id_root="${DEPLOYDIR}/telaf-images/debug_files"
    build_id_dir="${build_id_root}/build-id"
    if [ -d "${build_id_dir}" ]; then
        if command -v zstd >/dev/null 2>&1; then
            tar -C "${build_id_root}" \
                --sort=name --mtime='@0' --owner=0 --group=0 --numeric-owner \
                -I 'zstd -19 -T0' \
                -cf "${build_id_root}/build-id.tar.zst" build-id
            bbnote "Compressed build-id directory to ${build_id_root}/build-id.tar.zst"
        elif command -v pigz >/dev/null 2>&1; then
            tar -C "${build_id_root}" \
                --sort=name --mtime='@0' --owner=0 --group=0 --numeric-owner \
                -I pigz \
                -cf "${build_id_root}/build-id.tar.gz" build-id
            bbnote "Compressed build-id directory to ${build_id_root}/build-id.tar.gz (pigz)"
        else
            tar -C "${build_id_root}" \
                --sort=name --mtime='@0' --owner=0 --group=0 --numeric-owner \
                -czf "${build_id_root}/build-id.tar.gz" build-id
            bbnote "Compressed build-id directory to ${build_id_root}/build-id.tar.gz (gzip)"
        fi
    else
        bbwarn "Skip compression: build-id directory not found at ${build_id_dir}"
    fi
}

addtask deploy after do_compile before do_build
