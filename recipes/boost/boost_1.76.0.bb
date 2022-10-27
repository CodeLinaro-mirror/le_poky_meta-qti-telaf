require boost-${PV}.inc
require ${COREBASE}/meta/recipes-support/boost/boost.inc

BOOST_LIBS = "\
    atomic \
    chrono \
    filesystem \
    log \
    regex \
    system \
    thread \
    "
