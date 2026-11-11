package com.libri.enchantment;

import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;

public class ModEnchantmentHelper {
    public static int getLevel(Enchantment enchantment, ItemStack stack) {
        if (stack.isEmpty() || enchantment == null) return 0;
        return EnchantmentHelper.getLevel(enchantment, stack);
    }

    public static int getLevel(Enchantment enchantment, ItemEnchantmentsComponent enchantments) {
        if (enchantments == null || enchantment == null) return 0;
        return enchantments.getLevel(enchantment);
    }
}
