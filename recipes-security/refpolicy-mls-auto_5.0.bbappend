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
    export SDEF_FILE="${WORKDIR}/telaf/modules/${MACHINE}.sdef"
    export TEST_APPS_SINC="${WORKDIR}/telaf/modules/testApps.sinc"

    install -d "${DST_DIR}"
    if [ -d "${POLICY_ROOT}/sys" ] && ls -1 "${POLICY_ROOT}/sys"/* >/dev/null 2>&1; then
        cp -fr "${POLICY_ROOT}/sys/"* "${DST_DIR}/"
    fi

python3 - << 'PYCODE'
import os
import glob
DST  = os.environ['DST_DIR']
ROOT = os.environ['POLICY_ROOT']
SDEF = os.environ['SDEF_FILE']
SINC = os.environ['TEST_APPS_SINC']

def read_apps(path):
    if not path or not os.path.isfile(path):
        return []
    apps, in_block = [], False
    with open(path, 'r', encoding='utf-8', errors='ignore') as f:
        for raw in f:
            line = raw.strip()
            if line.startswith('apps:'):
                in_block = True
                continue
            if in_block and line.startswith('}'):
                in_block = False
                continue
            if not in_block:
                continue
            if not line or line.startswith('//') or line.startswith('$LEGATO_ROOT'):
                continue
            if not line.startswith('$TELAF_ROOT/apps/'):
                continue
            token = line.split('#', 1)[0].strip()
            last = token.split('/')[-1]
            if last.endswith('.adef'):
                last = last[:-5]
            if last:
                apps.append(last)
    return apps

apps = set(read_apps(SDEF) + read_apps(SINC))
if not apps:
    raise SystemExit(0)

def copy_component_under_root(app_name):
    for r, dnames, fnames in os.walk(ROOT):
        if os.path.basename(r) != app_name:
            continue
        comp = os.path.join(r, 'component')
        if os.path.isdir(comp) and glob.glob(os.path.join(comp, '*.te')):
            for src in glob.glob(os.path.join(comp, '*')):
                os.system(f'cp -fr "{src}" "{DST}/"')
            return True
    return False

for app in sorted(apps):
    copy_component_under_root(app)
PYCODE
}


POLICY_CUSTOM_BUILDOPT:append = ""qti-nad-telaf sa525m kirkstone""

do_install:append() {
    install -d ${TMPDIR}/work-shared
    cp -rf ${D}/usr/share/selinux ${TMPDIR}/work-shared/
}

