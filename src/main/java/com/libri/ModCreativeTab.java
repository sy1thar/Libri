package com.libri;

import com.libri.enchantment.ModEnchantments;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ModCreativeTab {
    public static void register() {
        Registry.register(Registries.ITEM_GROUP,
                new Identifier("libri", "main"),
                FabricItemGroup.builder()
                        .icon(() -> {
                            ItemStack iconStack = new ItemStack(Items.ENCHANTED_BOOK);
                            iconStack.getOrCreateNbt().putInt("CustomModelData", 1);
                            return iconStack;
                        })
                        .displayName(Text.translatable("itemGroup.libri"))
                        .entries((context, entries) -> {
                            addEnchantedBook(entries, ModEnchantments.ETERNITY);
                            addEnchantedBook(entries, ModEnchantments.TELEKINESIS);
                            addEnchantedBook(entries, ModEnchantments.PHOENIX);
                            addEnchantedBook(entries, ModEnchantments.SACRIFICE);
                            addEnchantedBook(entries, ModEnchantments.SWIFT_STRIKE);
                            addEnchantedBook(entries, ModEnchantments.SMELTING_SPEED);
                            addEnchantedBook(entries, ModEnchantments.FUEL_EFFICIENCY);
                            addEnchantedBook(entries, ModEnchantments.SMELTING_TOUCH);
                            addEnchantedBook(entries, ModEnchantments.HUSH);
                            addEnchantedBook(entries, ModEnchantments.SOULBOUND);
                            addEnchantedBook(entries, ModEnchantments.SIFTING);
                            addEnchantedBook(entries, ModEnchantments.RESONANCE);
                            addEnchantedBook(entries, ModEnchantments.TROPHY);
                            addEnchantedBook(entries, ModEnchantments.PARALYSIS);
                            addEnchantedBook(entries, ModEnchantments.CRUSHER);
                            addEnchantedBook(entries, ModEnchantments.LUMBERJACK);
                            entries.add(LibriMod.ADVANCED_ANVIL_ITEM);
                        })
                        .build()
        );
    }

    private static void addEnchantedBook(ItemGroup.Entries entries, Enchantment enchantment) {
        int maxLevel = enchantment.getMaxLevel();
        for (int l = 1; l <= maxLevel; l++) {
            ItemStack book = EnchantedBookItem.forEnchantment(new EnchantmentLevelEntry(enchantment, l));
            book.getOrCreateNbt().putInt("CustomModelData", 1);
            entries.add(book);
        }
    }
}
