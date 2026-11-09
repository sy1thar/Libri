package com.libri.enchantment;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;

public class ModEnchantmentHelper {
    public static int getLevel(ResourceKey<Enchantment> key, ItemStack stack) {
        if (stack.isEmpty()) return 0;
        return getLevel(key, EnchantmentHelper.getEnchantmentsForCrafting(stack));
    }

    public static int getLevel(ResourceKey<Enchantment> key, ItemEnchantments enchantments) {
        for (var entry : enchantments.entrySet()) {
            if (entry.getKey().is(key)) {
                return entry.getIntValue();
            }
        }
        return 0;
    }
}
