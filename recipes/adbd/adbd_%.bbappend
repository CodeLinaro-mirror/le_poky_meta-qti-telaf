# append telAf path to system $PATH on adb
do_install:append() {
    sed -i '/export TERM=linux/aexport PATH=\/legato\/systems\/current\/bin:$PATH' ${D}${base_sbindir}/launch_adbd
}
