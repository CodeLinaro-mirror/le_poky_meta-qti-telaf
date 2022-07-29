# Append telaf related images to rootfs
IMAGE_INSTALL += "${@bb.utils.contains('DISTRO_FEATURES', 'nad-telaf', 'telaf-build', '', d)}"
IMAGE_INSTALL += "${@bb.utils.contains('DISTRO_FEATURES', 'nad-telaf', 'telaf-init', '', d)}"
IMAGE_INSTALL += "${@bb.utils.contains('DISTRO_FEATURES', 'nad-telaf', 'telaf-image', '', d)}"
IMAGE_INSTALL += "${@bb.utils.contains('DISTRO_FEATURES', 'nad-telaf', 'telaf-test-build', '', d)}"

