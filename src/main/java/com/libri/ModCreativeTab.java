package com.libri;

import com.libri.enchantment.ModEnchantments;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.CustomModelDataComponent;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
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
                            iconStack.set(DataComponentTypes.CUSTOM_MODEL_DATA, new CustomModelDataComponent(1));
                            return iconStack;
                        })
                        .displayName(Text.translatable("itemGroup.libri"))
                        .entries((context, entries) -> {
                            addEnchantedBook(entries, ModEnchantments.ETERNITY);
                            addEnchantedBook(entries, ModEnchantments.TELEKINESIS);
                            addEnchantedBook(entries, ModEnchantments.PHOENIX);
                            addEnchantedBook(entries, ModEnchantments.SACRIFICE);
                            addEnchantedBook(entries, ModEnchantments.SWIFT_STRIKE);
                            addEnchantedBook(entries, ModEnchantments.SMELTING_TOUCH);
                            addEnchantedBook(entries, ModEnchantments.HUSH);
                            addEnchantedBook(entries, ModEnchantments.SOULBOUND);
                            addEnchantedBook(entries, ModEnchantments.SIFTING);
                            addEnchantedBook(entries, ModEnchantments.RESONANCE);
                            addEnchantedBook(entries, ModEnchantments.TROPHY);
                            addEnchantedBook(entries, ModEnchantments.PARALYSIS);
                            entries.add(LibriMod.ADVANCED_ANVIL_ITEM);
                        })
                        .build()
        );
    }

    private static void addEnchantedBook(ItemGroup.Entries entries, Enchantment enchantment) {
        int level = enchantment.getMaxLevel();
        ItemStack book = new ItemStack(Items.ENCHANTED_BOOK);
        var builder = new ItemEnchantmentsComponent.Builder(ItemEnchantmentsComponent.DEFAULT);
        builder.add(enchantment, level);
        book.set(DataComponentTypes.STORED_ENCHANTMENTS, builder.build());
        book.set(DataComponentTypes.CUSTOM_MODEL_DATA, new CustomModelDataComponent(1));
        entries.add(book);
    }
}
