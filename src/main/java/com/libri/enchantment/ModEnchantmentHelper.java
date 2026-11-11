package com.libri.enchantment;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;

import java.util.Map;

public class ModEnchantmentHelper {
    public static int getLevel(Enchantment enchantment, ItemStack stack) {
        if (stack.isEmpty() || enchantment == null) return 0;
        return EnchantmentHelper.getLevel(enchantment, stack);
    }

    public static int getLevel(Enchantment enchantment, Map<Enchantment, Integer> enchantments) {
        if (enchantments == null || enchantment == null) return 0;
        return enchantments.getOrDefault(enchantment, 0);
    }
}
