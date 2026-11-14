package com.libri.enchantment;

import com.libri.LibriMod;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.CustomModelDataComponent;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

public class ModEnchantmentHelper {
    public static final CustomModelDataComponent CUSTOM_BOOK_MODEL =
            new CustomModelDataComponent(1);
    public static final Identifier BOOK_MODEL_ID = Identifier.of(LibriMod.MOD_ID, "book");

    public static int getLevel(RegistryKey<Enchantment> key, ItemStack stack) {
        if (stack.isEmpty()) return 0;
        return getLevel(key, EnchantmentHelper.getEnchantments(stack));
    }

    public static int getLevel(RegistryKey<Enchantment> key, ItemEnchantmentsComponent enchantments) {
        for (var entry : enchantments.getEnchantmentEntries()) {
            if (entry.getKey().matchesKey(key)) {
                return entry.getIntValue();
            }
        }
        return 0;
    }

    public static boolean isModEnchantment(RegistryEntry<Enchantment> entry) {
        if (entry == null) return false;
        return entry.getKey()
                .map(k -> k.getValue().getNamespace().equals(LibriMod.MOD_ID))
                .orElse(false);
    }

    public static boolean hasModEnchantment(ItemEnchantmentsComponent enchantments) {
        if (enchantments == null || enchantments.isEmpty()) return false;
        for (var entry : enchantments.getEnchantmentEntries()) {
            if (isModEnchantment(entry.getKey())) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasModEnchantment(ItemStack stack) {
        if (stack.isEmpty() || !stack.isOf(Items.ENCHANTED_BOOK)) return false;

        ItemEnchantmentsComponent stored = stack.get(DataComponentTypes.STORED_ENCHANTMENTS);
        if (hasModEnchantment(stored)) return true;

        ItemEnchantmentsComponent regular = stack.get(DataComponentTypes.ENCHANTMENTS);
        return hasModEnchantment(regular);
    }

    public static void applyCustomBookModel(ItemStack stack) {
        stack.set(DataComponentTypes.CUSTOM_MODEL_DATA, CUSTOM_BOOK_MODEL);
        stack.set(DataComponentTypes.ITEM_MODEL, BOOK_MODEL_ID);
    }

    public static void applyCustomBookModelIfModEnchanted(ItemStack stack) {
        if (hasModEnchantment(stack)) {
            applyCustomBookModel(stack);
        }
    }
}
