package com.libri.event;

import com.libri.enchantment.ModEnchantments;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.EnchantRandomlyFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

public class ModLootTables {
    private static final ResourceLocation[] CHEST_TABLES = {
            ResourceLocation.fromNamespaceAndPath("minecraft", "chests/simple_dungeon"),
            ResourceLocation.fromNamespaceAndPath("minecraft", "chests/abandoned_mineshaft"),
            ResourceLocation.fromNamespaceAndPath("minecraft", "chests/stronghold_library"),
            ResourceLocation.fromNamespaceAndPath("minecraft", "chests/stronghold_corridor"),
            ResourceLocation.fromNamespaceAndPath("minecraft", "chests/desert_pyramid"),
            ResourceLocation.fromNamespaceAndPath("minecraft", "chests/jungle_temple"),
            ResourceLocation.fromNamespaceAndPath("minecraft", "chests/woodland_mansion"),
            ResourceLocation.fromNamespaceAndPath("minecraft", "chests/ancient_city"),
            ResourceLocation.fromNamespaceAndPath("minecraft", "chests/end_city_treasure"),
            ResourceLocation.fromNamespaceAndPath("minecraft", "chests/bastion_treasure")
    };

    public static void register() {
        LootTableEvents.MODIFY.register((key, tableBuilder, source, registries) -> {
            if (source.isBuiltin()) {
                if (isChestTable(key)) {
                    tableBuilder.withPool(createTreasurePool(registries, 0.15f));
                }
                if (key.location().equals(ResourceLocation.fromNamespaceAndPath("minecraft", "chests/ancient_city"))) {
                    tableBuilder.withPool(createSoulboundPool(registries, 0.025f));
                }
            }
        });
    }

    private static boolean isChestTable(ResourceKey<LootTable> key) {
        for (ResourceLocation id : CHEST_TABLES) {
            if (key.location().equals(id)) return true;
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    private static LootPool.Builder createTreasurePool(HolderLookup.Provider registries, float chance) {
        LootPool.Builder pool = LootPool.lootPool()
                .setRolls(ConstantValue.exactly(1))
                .when(LootItemRandomChanceCondition.randomChance(chance));

        var builder = EnchantRandomlyFunction.randomApplicableEnchantment(registries);
        HolderLookup<Enchantment> registry = registries.lookupOrThrow(Registries.ENCHANTMENT);
        boolean valid = true;
        for (ResourceKey<Enchantment> key : new ResourceKey[]{
                ModEnchantments.ETERNITY,
                ModEnchantments.TELEKINESIS,
                ModEnchantments.PHOENIX,
                ModEnchantments.SACRIFICE,
                ModEnchantments.SWIFT_STRIKE,
                ModEnchantments.PARALYSIS
        }) {
            try {
                builder.withEnchantment(registry.getOrThrow(key));
            } catch (Exception e) {
                valid = false;
                break;
            }
        }
        if (valid) {
            pool.add(LootItem.lootTableItem(Items.ENCHANTED_BOOK));
            pool.apply(builder.build());
        } else {
            pool.add(LootItem.lootTableItem(Items.BOOK));
        }

        return pool;
    }

    private static LootPool.Builder createSoulboundPool(HolderLookup.Provider registries, float chance) {
        LootPool.Builder pool = LootPool.lootPool()
                .setRolls(ConstantValue.exactly(1))
                .when(LootItemRandomChanceCondition.randomChance(chance));

        var builder = EnchantRandomlyFunction.randomApplicableEnchantment(registries);
        HolderLookup<Enchantment> registry = registries.lookupOrThrow(Registries.ENCHANTMENT);
        try {
            builder.withEnchantment(registry.getOrThrow(ModEnchantments.SOULBOUND));
            pool.add(LootItem.lootTableItem(Items.ENCHANTED_BOOK));
            pool.apply(builder.build());
        } catch (Exception ignored) {
            pool.add(LootItem.lootTableItem(Items.BOOK));
        }

        return pool;
    }
}
