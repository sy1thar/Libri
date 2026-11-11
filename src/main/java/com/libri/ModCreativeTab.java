package com.libri;

import com.libri.enchantment.ModEnchantments;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.CustomModelDataComponent;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.List;

public class ModCreativeTab {
    public static void register() {
        Registry.register(Registries.ITEM_GROUP,
                Identifier.of("libri", "main"),
                FabricItemGroup.builder()
                        .icon(() -> {
                            ItemStack iconStack = new ItemStack(Items.ENCHANTED_BOOK);
                            iconStack.set(DataComponentTypes.CUSTOM_MODEL_DATA, new CustomModelDataComponent(List.of(1.0f), List.of(), List.of(), List.of()));
                            return iconStack;
                        })
                        .displayName(Text.translatable("itemGroup.libri"))
                        .entries((context, entries) -> {
                            context.lookup().getOptional(RegistryKeys.ENCHANTMENT).ifPresent(registry -> {
                                addEnchantedBook(entries, registry, ModEnchantments.ETERNITY);
                                addEnchantedBook(entries, registry, ModEnchantments.TELEKINESIS);
                                addEnchantedBook(entries, registry, ModEnchantments.PHOENIX);
                                addEnchantedBook(entries, registry, ModEnchantments.SACRIFICE);
                                addEnchantedBook(entries, registry, ModEnchantments.SWIFT_STRIKE);
                                addEnchantedBook(entries, registry, ModEnchantments.SMELTING_TOUCH);
                                addEnchantedBook(entries, registry, ModEnchantments.HUSH);
                                addEnchantedBook(entries, registry, ModEnchantments.SOULBOUND);
                                addEnchantedBook(entries, registry, ModEnchantments.SIFTING);
                                addEnchantedBook(entries, registry, ModEnchantments.RESONANCE);
                                addEnchantedBook(entries, registry, ModEnchantments.TROPHY);
                                addEnchantedBook(entries, registry, ModEnchantments.PARALYSIS);
                            });
                            entries.add(LibriMod.ADVANCED_ANVIL_ITEM);
                        })
                        .build()
        );
    }

    private static void addEnchantedBook(ItemGroup.Entries entries, RegistryWrapper.Impl<net.minecraft.enchantment.Enchantment> registry,
                                          net.minecraft.registry.RegistryKey<net.minecraft.enchantment.Enchantment> key) {
        registry.getOptional(key).ifPresent(entry -> {
            int level = entry.value().getMaxLevel();
            ItemStack book = new ItemStack(Items.ENCHANTED_BOOK);
            var builder = new ItemEnchantmentsComponent.Builder(ItemEnchantmentsComponent.DEFAULT);
            builder.add(entry, level);
            book.set(DataComponentTypes.STORED_ENCHANTMENTS, builder.build());
            book.set(DataComponentTypes.CUSTOM_MODEL_DATA, new CustomModelDataComponent(List.of(1.0f), List.of(), List.of(), List.of()));
            entries.add(book);
        });
    }
}
