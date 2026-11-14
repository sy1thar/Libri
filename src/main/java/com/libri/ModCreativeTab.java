package com.libri;

import com.libri.enchantment.ModEnchantmentHelper;
import com.libri.enchantment.ModEnchantments;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ModCreativeTab {
    public static void register() {
        Registry.register(Registries.ITEM_GROUP,
                Identifier.of("libri", "main"),
                FabricItemGroup.builder()
                        .icon(() -> {
                            ItemStack iconStack = new ItemStack(Items.ENCHANTED_BOOK);
                            ModEnchantmentHelper.applyCustomBookModel(iconStack);
                            return iconStack;
                        })
                        .displayName(Text.translatable("itemGroup.libri"))
                        .entries((context, entries) -> {
                            context.lookup().getOptionalWrapper(RegistryKeys.ENCHANTMENT).ifPresent(registry -> {
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
                            entries.add(LibriMod.ADVANCED_ANVIL_ITEM);
                        })
                        .build()
        );
    }

    private static void addEnchantedBook(ItemGroup.Entries entries, RegistryWrapper.Impl<Enchantment> registry,
                                          RegistryKey<Enchantment> key) {
        registry.getOptional(key).ifPresent(entry -> {
            int maxLevel = entry.value().getMaxLevel();
            for (int lvl = 1; lvl <= maxLevel; lvl++) {
                ItemStack book = new ItemStack(Items.ENCHANTED_BOOK);
                var builder = new ItemEnchantmentsComponent.Builder(ItemEnchantmentsComponent.DEFAULT);
                builder.add(entry, lvl);
                book.set(DataComponentTypes.STORED_ENCHANTMENTS, builder.build());
                ModEnchantmentHelper.applyCustomBookModel(book);
                entries.add(book);
            }
        });
    }
}
