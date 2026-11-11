package com.libri.event;

import com.libri.enchantment.ModEnchantments;
import net.fabricmc.fabric.api.loot.v2.LootTableEvents;
import net.minecraft.item.Items;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.condition.RandomChanceLootCondition;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.function.EnchantRandomlyLootFunction;
import net.minecraft.loot.provider.number.ConstantLootNumberProvider;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;

public class ModLootTables {
    private static final Identifier[] CHEST_TABLES = {
            new Identifier("minecraft", "chests/simple_dungeon"),
            new Identifier("minecraft", "chests/abandoned_mineshaft"),
            new Identifier("minecraft", "chests/stronghold_library"),
            new Identifier("minecraft", "chests/stronghold_corridor"),
            new Identifier("minecraft", "chests/desert_pyramid"),
            new Identifier("minecraft", "chests/jungle_temple"),
            new Identifier("minecraft", "chests/woodland_mansion"),
            new Identifier("minecraft", "chests/ancient_city"),
            new Identifier("minecraft", "chests/end_city_treasure"),
            new Identifier("minecraft", "chests/bastion_treasure")
    };

    public static void register() {
        LootTableEvents.MODIFY.register((key, tableBuilder, source) -> {
            if (source.isBuiltin()) {
                if (isChestTable(key)) {
                    tableBuilder.pool(createTreasurePool(0.15f));
                }
                if (key.getValue().equals(new Identifier("minecraft", "chests/ancient_city"))) {
                    tableBuilder.pool(createSoulboundPool(0.025f));
                }
            }
        });
    }

    private static boolean isChestTable(RegistryKey<LootTable> key) {
        Identifier tableId = key.getValue();
        for (Identifier id : CHEST_TABLES) {
            if (tableId.equals(id)) return true;
        }
        return false;
    }

    private static LootPool.Builder createTreasurePool(float chance) {
        LootPool.Builder pool = LootPool.builder()
                .rolls(ConstantLootNumberProvider.create(1))
                .conditionally(RandomChanceLootCondition.builder(chance));

        var builder = new EnchantRandomlyLootFunction.Builder()
                .add(ModEnchantments.ETERNITY)
                .add(ModEnchantments.TELEKINESIS)
                .add(ModEnchantments.PHOENIX)
                .add(ModEnchantments.SACRIFICE)
                .add(ModEnchantments.SWIFT_STRIKE)
                .add(ModEnchantments.PARALYSIS);

        pool.with(ItemEntry.builder(Items.ENCHANTED_BOOK));
        pool.apply(builder.build());
        return pool;
    }

    private static LootPool.Builder createSoulboundPool(float chance) {
        LootPool.Builder pool = LootPool.builder()
                .rolls(ConstantLootNumberProvider.create(1))
                .conditionally(RandomChanceLootCondition.builder(chance));

        var builder = new EnchantRandomlyLootFunction.Builder()
                .add(ModEnchantments.SOULBOUND);

        pool.with(ItemEntry.builder(Items.ENCHANTED_BOOK));
        pool.apply(builder.build());
        return pool;
    }
}
