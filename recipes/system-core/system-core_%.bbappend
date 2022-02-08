# append telAf path to system $PATH on adb
do_install_append() {
    sed -i '/export TERM=linux/aexport PATH=\/legato\/systems\/current\/bin:$PATH' ${D}${sysconfdir}/launch_adbd
}
