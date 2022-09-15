/*
  SPDX-License-Identifier: GPL-2.0-only
  Copyright (c) 2022 Qualcomm Innovation Center, Inc. All rights reserved.
*/
#include <linux/module.h>
#include <linux/kernel.h>
#include <linux/interrupt.h>
#include <linux/gpio.h>
#include <gpiolib.h>

MODULE_ALIAS("platform:TelAF");
MODULE_DESCRIPTION("Kernel module for enabling wakeup irq");
MODULE_LICENSE("GPL v2");

static char * gpioChipName = NULL;
module_param(gpioChipName, charp, 0644);

static int gpioOffset = -1;
module_param(gpioOffset, int, 0644);

static int gpiochip_name_match(struct gpio_chip *chip, void *data)
{
    return !strcmp(chip->label, data);
}

static int __init gpio_wakeup_init(void)
{
    int irq = 0;
    int ret;
    struct gpio_chip *chip;
    struct gpio_desc *desc;

    if ((gpioChipName == NULL) || (gpioOffset == -1))
    {
        pr_err("gpio chip(%s) or offset(%d) is invalid\n", gpioChipName, gpioOffset);
        return -1;
    }

    chip = gpiochip_find(gpioChipName, gpiochip_name_match);
    if (!chip)
    {
        pr_err("cannot found gpiochip for %s\n", gpioChipName);
        return -ENOENT;
    }

    desc = gpiochip_request_own_desc(chip, gpioOffset, "telaf");
    if (!desc)
    {
        pr_err("cannot found gpio desc for pin %d\n", gpioOffset);
        return -ENOENT;
    }

    int irqNum = gpiod_to_irq(desc);
    ret = irq_set_irq_wake(irqNum, 1);
    if (ret)
    {
        pr_err("setting wakeup IRQ attr is failed. ret: %d\n", ret);
        gpiochip_free_own_desc(desc);
        return ret;
    }

    gpiochip_free_own_desc(desc);

    pr_info("finish setting wakeup mode for %s %d\n", gpioChipName, gpioOffset);
    return 0;
}

static void __exit gpio_wakeup_exit(void)
{
    pr_info("exiting module %s()\n", __func__);
}

module_init(gpio_wakeup_init);
module_exit(gpio_wakeup_exit);
