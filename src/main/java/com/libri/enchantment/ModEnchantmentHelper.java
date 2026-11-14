package com.libri.enchantment;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

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

    public static boolean isModEnchantment(Enchantment enchantment) {
        if (enchantment == null) return false;
        Identifier id = Registries.ENCHANTMENT.getId(enchantment);
        return id != null && id.getNamespace().equals("libri");
    }

    public static boolean hasModEnchantment(ItemStack stack) {
        if (stack.isEmpty()) return false;
        Map<Enchantment, Integer> enchantments = EnchantmentHelper.get(stack);
        for (Enchantment e : enchantments.keySet()) {
            if (isModEnchantment(e)) return true;
        }
        return false;
    }

    public static void applyCustomBookModelIfModEnchanted(ItemStack stack) {
        if (stack.isEmpty() || !stack.isOf(Items.ENCHANTED_BOOK)) return;
        if (hasModEnchantment(stack)) {
            stack.getOrCreateNbt().putInt("CustomModelData", 1);
        }
    }
}
