package com.libri.scratch;

import net.minecraft.world.item.ItemStack;

public class Scratch {
    public static void test(ItemStack a, ItemStack b) {
        ItemStack.isSameItemSameComponents(a, b);
    }
}
