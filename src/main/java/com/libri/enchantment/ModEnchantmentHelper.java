package com.libri.enchantment;

import com.libri.LibriMod;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;

import java.util.List;

public class ModEnchantmentHelper {
    public static final CustomModelData CUSTOM_BOOK_MODEL =
            new CustomModelData(List.of(1.0f), List.of(), List.of(), List.of());

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

    public static boolean isModEnchantment(Holder<Enchantment> holder) {
        return holder.unwrapKey()
                .map(key -> key.identifier().getNamespace().equals(LibriMod.MOD_ID))
                .orElse(false);
    }

    public static boolean hasModEnchantment(ItemEnchantments enchantments) {
        if (enchantments == null || enchantments.isEmpty()) return false;
        for (var entry : enchantments.entrySet()) {
            if (isModEnchantment(entry.getKey())) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasModEnchantment(ItemStack stack) {
        if (stack.isEmpty()) return false;
        if (!stack.is(Items.ENCHANTED_BOOK)) return false;

        ItemEnchantments stored = stack.get(DataComponents.STORED_ENCHANTMENTS);
        if (hasModEnchantment(stored)) return true;

        ItemEnchantments regular = stack.get(DataComponents.ENCHANTMENTS);
        return hasModEnchantment(regular);
    }

    public static void applyCustomBookModel(ItemStack stack) {
        stack.set(DataComponents.CUSTOM_MODEL_DATA, CUSTOM_BOOK_MODEL);
    }

    public static void applyCustomBookModelIfModEnchanted(ItemStack stack) {
        if (hasModEnchantment(stack)) {
            applyCustomBookModel(stack);
        }
    }
}
