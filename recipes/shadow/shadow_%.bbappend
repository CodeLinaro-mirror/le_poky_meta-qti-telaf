# append telAf path to system $PATH on console
do_install:append() {
    sed -i 's/PATH=\/sbin:\/bin:\/usr\/sbin:\/usr\/bin/&:\/legato\/systems\/current\/bin/' ${D}${sysconfdir}/login.defs
    sed -i 's/PATH=\/bin:\/usr\/bin/&:\/legato\/systems\/current\/bin/' ${D}${sysconfdir}/login.defs
}
