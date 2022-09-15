
obj-m := gpioWakeup.o
ccflags-y += -I$(KERNEL_SRC)/drivers/gpio/

all:
	$(MAKE) -C $(KERNEL_SRC) M=$(shell pwd) modules

modules_install:
	$(MAKE) INSTALL_MOD_STRIP=1 -C $(KERNEL_SRC) M=$(shell pwd) modules_install

clean:
	rm -f *.o *.ko *.mod.c *.mod.o Module.symvers *~ .*.cmd
	rm -rf .tmp_versions
