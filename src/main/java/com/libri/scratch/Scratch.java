package com.libri.scratch;

import net.minecraft.item.ItemStack;

public class Scratch {
    public static void test(ItemStack a, ItemStack b) {
        boolean eq = ItemStack.areItemsAndComponentsEqual(a, b);
    }
}
