package com.libri;

import com.libri.config.ModConfig;
import com.libri.enchantment.ModEnchantments;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.component.DataComponentTypes;
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

public class ModCreativeTab {
    public static void register() {
        Registry.register(Registries.ITEM_GROUP,
                Identifier.of("libri", "main"),
                FabricItemGroup.builder()
                        .icon(() -> new ItemStack(Items.ENCHANTED_BOOK))
                        .displayName(Text.translatable("itemGroup.libri"))
                        .entries((context, entries) -> {
                            context.lookup().getOptionalWrapper(RegistryKeys.ENCHANTMENT).ifPresent(registry -> {
                                addEnchantedBook(entries, registry, ModEnchantments.ETERNITY);
                                addEnchantedBook(entries, registry, ModEnchantments.TELEKINESIS);
                                addEnchantedBook(entries, registry, ModEnchantments.PHOENIX);
                                addEnchantedBook(entries, registry, ModEnchantments.SACRIFICE);
                                addEnchantedBook(entries, registry, ModEnchantments.SWIFT_STRIKE);
                                addEnchantedBook(entries, registry, ModEnchantments.SMELTING_TOUCH);
                                addEnchantedBook(entries, registry, ModEnchantments.PARALYSIS);
                            });
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
            entries.add(book);
        });
    }
}
