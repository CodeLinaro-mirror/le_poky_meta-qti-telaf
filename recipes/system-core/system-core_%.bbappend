# append telAf path to system $PATH on adb
do_install_append_sa515m() {
    sed -i '/export TERM=linux/aexport PATH=\/legato\/systems\/current\/bin:$PATH' ${D}${sysconfdir}/launch_adbd
}
