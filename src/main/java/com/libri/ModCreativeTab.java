package com.libri;

import com.libri.enchantment.ModEnchantmentHelper;
import com.libri.enchantment.ModEnchantments;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.ItemEnchantments;

public class ModCreativeTab {
    public static void register() {
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB,
                Identifier.fromNamespaceAndPath("libri", "main"),
                FabricCreativeModeTab.builder()
                        .icon(() -> {
                            ItemStack iconStack = new ItemStack(Items.ENCHANTED_BOOK);
                            ModEnchantmentHelper.applyCustomBookModel(iconStack);
                            return iconStack;
                        })
                        .title(Component.translatable("itemGroup.libri"))
                        .displayItems((context, entries) -> {
                            context.holders().lookup(Registries.ENCHANTMENT).ifPresent(registry -> {
                                addEnchantedBook(entries, registry, ModEnchantments.ETERNITY);
                                addEnchantedBook(entries, registry, ModEnchantments.TELEKINESIS);
                                addEnchantedBook(entries, registry, ModEnchantments.PHOENIX);
                                addEnchantedBook(entries, registry, ModEnchantments.SACRIFICE);
                                addEnchantedBook(entries, registry, ModEnchantments.SWIFT_STRIKE);
                                addEnchantedBook(entries, registry, ModEnchantments.SMELTING_SPEED);
                                addEnchantedBook(entries, registry, ModEnchantments.FUEL_EFFICIENCY);
                                addEnchantedBook(entries, registry, ModEnchantments.SMELTING_TOUCH);
                                addEnchantedBook(entries, registry, ModEnchantments.HUSH);
                                addEnchantedBook(entries, registry, ModEnchantments.SOULBOUND);
                                addEnchantedBook(entries, registry, ModEnchantments.SIFTING);
                                addEnchantedBook(entries, registry, ModEnchantments.RESONANCE);
                                addEnchantedBook(entries, registry, ModEnchantments.TROPHY);
                                addEnchantedBook(entries, registry, ModEnchantments.PARALYSIS);
                                addEnchantedBook(entries, registry, ModEnchantments.CRUSHER);
                                addEnchantedBook(entries, registry, ModEnchantments.LUMBERJACK);
                            });
                            entries.accept(LibriMod.ADVANCED_ANVIL_ITEM);
                        })
                        .build()
        );
    }

    private static void addEnchantedBook(CreativeModeTab.Output entries, HolderLookup.RegistryLookup<net.minecraft.world.item.enchantment.Enchantment> registry,
                                          net.minecraft.resources.ResourceKey<net.minecraft.world.item.enchantment.Enchantment> key) {
        registry.get(key).ifPresent(entry -> {
            int maxLevel = entry.value().getMaxLevel();
            for (int level = 1; level <= maxLevel; level++) {
                ItemStack book = new ItemStack(Items.ENCHANTED_BOOK);
                var builder = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
                builder.upgrade(entry, level);
                book.set(DataComponents.STORED_ENCHANTMENTS, builder.toImmutable());
                ModEnchantmentHelper.applyCustomBookModel(book);
                entries.accept(book);
            }
        });
    }
}
