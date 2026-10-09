package com.libri.event;

import com.libri.enchantment.ModEnchantments;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.Items;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.condition.RandomChanceLootCondition;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.function.EnchantRandomlyLootFunction;
import net.minecraft.loot.provider.number.ConstantLootNumberProvider;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;

public class ModLootTables {
    private static final Identifier[] CHEST_TABLES = {
            Identifier.of("minecraft", "chests/simple_dungeon"),
            Identifier.of("minecraft", "chests/abandoned_mineshaft"),
            Identifier.of("minecraft", "chests/stronghold_library"),
            Identifier.of("minecraft", "chests/stronghold_corridor"),
            Identifier.of("minecraft", "chests/desert_pyramid"),
            Identifier.of("minecraft", "chests/jungle_temple"),
            Identifier.of("minecraft", "chests/woodland_mansion"),
            Identifier.of("minecraft", "chests/ancient_city"),
            Identifier.of("minecraft", "chests/end_city_treasure"),
            Identifier.of("minecraft", "chests/bastion_treasure")
    };

    public static void register() {
        LootTableEvents.MODIFY.register((key, tableBuilder, source, registries) -> {
            if (source.isBuiltin()) {
                if (isChestTable(key)) {
                    tableBuilder.pool(createTreasurePool(registries, 0.15f));
                }
                if (key.getValue().equals(Identifier.of("minecraft", "chests/ancient_city"))) {
                    tableBuilder.pool(createSoulboundPool(registries, 0.025f));
                }
            }
        });
    }

    private static boolean isChestTable(RegistryKey<LootTable> key) {
        for (Identifier id : CHEST_TABLES) {
            if (key.getValue().equals(id)) return true;
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    private static LootPool.Builder createTreasurePool(RegistryWrapper.WrapperLookup registries, float chance) {
        LootPool.Builder pool = LootPool.builder()
                .rolls(ConstantLootNumberProvider.create(1))
                .conditionally(RandomChanceLootCondition.builder(chance));

        var builder = EnchantRandomlyLootFunction.builder(registries);
        RegistryWrapper<Enchantment> registry = registries.getWrapperOrThrow(RegistryKeys.ENCHANTMENT);
        boolean valid = true;
        for (RegistryKey<Enchantment> key : new RegistryKey[]{
                ModEnchantments.ETERNITY,
                ModEnchantments.TELEKINESIS,
                ModEnchantments.PHOENIX,
                ModEnchantments.SACRIFICE,
                ModEnchantments.SWIFT_STRIKE,
                ModEnchantments.PARALYSIS
        }) {
            try {
                builder.option(registry.getOrThrow(key));
            } catch (Exception e) {
                valid = false;
                break;
            }
        }
        if (valid) {
            pool.with(ItemEntry.builder(Items.ENCHANTED_BOOK));
            pool.apply(builder.build());
        } else {
            pool.with(ItemEntry.builder(Items.BOOK));
        }

        return pool;
    }

    private static LootPool.Builder createSoulboundPool(RegistryWrapper.WrapperLookup registries, float chance) {
        LootPool.Builder pool = LootPool.builder()
                .rolls(ConstantLootNumberProvider.create(1))
                .conditionally(RandomChanceLootCondition.builder(chance));

        var builder = EnchantRandomlyLootFunction.builder(registries);
        RegistryWrapper<Enchantment> registry = registries.getWrapperOrThrow(RegistryKeys.ENCHANTMENT);
        try {
            builder.option(registry.getOrThrow(ModEnchantments.SOULBOUND));
            pool.with(ItemEntry.builder(Items.ENCHANTED_BOOK));
            pool.apply(builder.build());
        } catch (Exception ignored) {
            pool.with(ItemEntry.builder(Items.BOOK));
        }

        return pool;
    }
}
