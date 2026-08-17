LICENSE += "& BSD-3-Clause & BSD-3-Clause-Clear"

FILESEXTRAPATHS:append := "${THISDIR}:"
SRC_URI += "file://telaf-sepolicy/common/ \
            file://telaf-sepolicy/${BASEMACHINE}/ "

FILESEXTRAPATHS:prepend := "${THISDIR}/files:"
SRC_URI += "file://0070-TelAF-Fix-Argument-list-too-long-issue.patch"

FILESPATH =+ "${WORKSPACE}:"
SRC_URI += "file://telaf/"


do_compile:prepend() {
    set -eu
    export DST_DIR="${S}/policy/modules/device"
    export POLICY_ROOT="${WORKDIR}/telaf/security/selinux/sepolicy"

    install -d "${DST_DIR}"

python3 - << 'PYCODE'
import os
import shutil
import sys

DST = os.environ['DST_DIR']
ROOT = os.environ['POLICY_ROOT']

POLICY_EXTS = ('.te', '.fc', '.if')

# Copy only checked-in TelAF policy sources.  Do not parse .sdef/.sinc and
# do not run policy generators from the production build.
POLICY_DIRS = (
    'sys',
    'services',
    'sample',
    'test',
)


def copy_policy_file(src, copied):
    name = os.path.basename(src)
    dst = os.path.join(DST, name)

    # Refpolicy's device module directory is flat.  Duplicate basenames would
    # silently overwrite each other, so fail early instead.
    if name in copied:
        print(
            'ERROR: duplicate TelAF SELinux policy file basename:\n'
            f'  first : {copied[name]}\n'
            f'  second: {src}',
            file=sys.stderr,
        )
        sys.exit(1)

    shutil.copy2(src, dst)
    copied[name] = src


def copy_policy_dir(src_dir, copied):
    if not os.path.isdir(src_dir):
        return

    for name in sorted(os.listdir(src_dir)):
        src = os.path.join(src_dir, name)
        if not os.path.isfile(src):
            continue
        if not name.endswith(POLICY_EXTS):
            continue
        copy_policy_file(src, copied)


def copy_component_policies(root_dir, copied):
    if not os.path.isdir(root_dir):
        return

    for current_root, dirnames, _filenames in os.walk(root_dir):
        dirnames.sort()
        if os.path.basename(current_root) == 'component':
            copy_policy_dir(current_root, copied)


copied = {}

# Framework-wide TelAF policy.
copy_policy_dir(os.path.join(ROOT, 'sys'), copied)

# App, service, sample, and test component policies.
for policy_dir in POLICY_DIRS:
    if policy_dir == 'sys':
        continue
    copy_component_policies(os.path.join(ROOT, policy_dir), copied)

print('Copied TelAF SELinux policy files:')
for name in sorted(copied):
    print(f'  {name} <- {copied[name]}')
PYCODE
}


POLICY_CUSTOM_BUILDOPT:append = ""qti-nad-telaf sa525m kirkstone""

do_install:append() {
    install -d ${TMPDIR}/work-shared
    cp -rf ${D}/usr/share/selinux ${TMPDIR}/work-shared/
}

