package com.libri.enchantment;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.CustomModelDataComponent;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

public class ModEnchantmentHelper {
    public static int getLevel(Enchantment enchantment, ItemStack stack) {
        if (stack.isEmpty() || enchantment == null) return 0;
        return EnchantmentHelper.getLevel(enchantment, stack);
    }

    public static int getLevel(Enchantment enchantment, ItemEnchantmentsComponent enchantments) {
        if (enchantments == null || enchantment == null) return 0;
        return enchantments.getLevel(enchantment);
    }

    public static boolean isModEnchantment(Enchantment enchantment) {
        if (enchantment == null) return false;
        Identifier id = Registries.ENCHANTMENT.getId(enchantment);
        return id != null && id.getNamespace().equals("libri");
    }

    public static boolean hasModEnchantment(ItemStack stack) {
        if (stack.isEmpty()) return false;
        ItemEnchantmentsComponent enchants = stack.get(DataComponentTypes.ENCHANTMENTS);
        if (enchants != null) {
            for (RegistryEntry<Enchantment> e : enchants.getEnchantments()) {
                if (isModEnchantment(e.value())) return true;
            }
        }
        ItemEnchantmentsComponent stored = stack.get(DataComponentTypes.STORED_ENCHANTMENTS);
        if (stored != null) {
            for (RegistryEntry<Enchantment> e : stored.getEnchantments()) {
                if (isModEnchantment(e.value())) return true;
            }
        }
        return false;
    }

    public static void applyCustomBookModelIfModEnchanted(ItemStack stack) {
        if (stack.isEmpty() || !stack.isOf(Items.ENCHANTED_BOOK)) return;
        if (hasModEnchantment(stack)) {
            stack.set(DataComponentTypes.CUSTOM_MODEL_DATA, new CustomModelDataComponent(1));
        }
    }
}
