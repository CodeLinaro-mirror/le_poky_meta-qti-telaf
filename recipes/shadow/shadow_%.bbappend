# append telAf path to system $PATH on console
do_install_append_sa515m() {
    sed -i 's/PATH=\/sbin:\/bin:\/usr\/sbin:\/usr\/bin/&:\/legato\/systems\/current\/bin/' ${D}${sysconfdir}/login.defs
    sed -i 's/PATH=\/bin:\/usr\/bin/&:\/legato\/systems\/current\/bin/' ${D}${sysconfdir}/login.defs
}
