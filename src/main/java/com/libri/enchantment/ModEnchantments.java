package com.libri.enchantment;

import com.libri.LibriMod;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

public class ModEnchantments {
    public static final Enchantment ETERNITY = create(
            ItemTags.DURABILITY_ENCHANTABLE, ItemTags.DURABILITY_ENCHANTABLE,
            2, 1, Enchantment.leveledCost(30, 8), Enchantment.leveledCost(50, 8), 8,
            true, true, true, EquipmentSlot.values());

    public static final Enchantment TELEKINESIS = create(
            tag("telekinesis"), tag("telekinesis"),
            2, 1, Enchantment.leveledCost(20, 5), Enchantment.leveledCost(40, 5), 4,
            false, true, true, EquipmentSlot.MAINHAND);

    public static final Enchantment PHOENIX = create(
            ItemTags.DURABILITY_ENCHANTABLE, ItemTags.DURABILITY_ENCHANTABLE,
            2, 10, Enchantment.leveledCost(25, 7), Enchantment.leveledCost(50, 7), 6,
            false, true, true, EquipmentSlot.values());

    public static final Enchantment SACRIFICE = create(
            ItemTags.WEAPON_ENCHANTABLE, ItemTags.WEAPON_ENCHANTABLE,
            2, 2, Enchantment.leveledCost(20, 8), Enchantment.leveledCost(50, 8), 8,
            false, true, true, EquipmentSlot.MAINHAND);

    public static final Enchantment SWIFT_STRIKE = create(
            ItemTags.SHARP_WEAPON_ENCHANTABLE, ItemTags.SHARP_WEAPON_ENCHANTABLE,
            4, 2, Enchantment.leveledCost(8, 4), Enchantment.leveledCost(20, 4), 3,
            false, true, true, EquipmentSlot.MAINHAND);

    public static final Enchantment SMELTING_SPEED = create(
            tag("furnace"), tag("furnace"),
            2, 10, Enchantment.leveledCost(10, 5), Enchantment.leveledCost(50, 5), 4,
            false, true, true, EquipmentSlot.MAINHAND);

    public static final Enchantment FUEL_EFFICIENCY = create(
            tag("furnace"), tag("furnace"),
            2, 10, Enchantment.leveledCost(10, 5), Enchantment.leveledCost(50, 5), 4,
            false, true, true, EquipmentSlot.MAINHAND);

    public static final Enchantment SMELTING_TOUCH = create(
            tag("smelting_touch"), tag("smelting_touch"),
            2, 1, Enchantment.leveledCost(25, 10), Enchantment.leveledCost(50, 10), 6,
            true, true, true, EquipmentSlot.MAINHAND);

    public static final Enchantment HUSH = create(
            tag("hush"), tag("hush"),
            2, 1, Enchantment.leveledCost(25, 10), Enchantment.leveledCost(50, 10), 6,
            true, true, true, EquipmentSlot.LEGS);

    public static final Enchantment SOULBOUND = create(
            ItemTags.VANISHING_ENCHANTABLE, ItemTags.VANISHING_ENCHANTABLE,
            2, 1, Enchantment.leveledCost(15, 0), Enchantment.leveledCost(65, 0), 4,
            false, true, true, EquipmentSlot.values());

    public static final Enchantment SIFTING = create(
            tag("sifting"), tag("sifting"),
            2, 1, Enchantment.leveledCost(15, 0), Enchantment.leveledCost(65, 0), 4,
            false, true, true, EquipmentSlot.MAINHAND);

    public static final Enchantment RESONANCE = create(
            tag("resonance"), tag("resonance"),
            2, 1, Enchantment.leveledCost(20, 0), Enchantment.leveledCost(70, 0), 4,
            false, true, true, EquipmentSlot.MAINHAND);

    public static final Enchantment TROPHY = create(
            tag("trophy"), tag("trophy"),
            2, 1, Enchantment.leveledCost(15, 15), Enchantment.leveledCost(65, 20), 4,
            false, true, true, EquipmentSlot.MAINHAND);

    public static final Enchantment PARALYSIS = create(
            tag("paralysis"), tag("paralysis"),
            2, 1, Enchantment.leveledCost(15, 0), Enchantment.leveledCost(65, 0), 4,
            true, false, false, EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND);

    public static final Enchantment CRUSHER = create(
            tag("crusher"), tag("crusher"),
            2, 1, Enchantment.leveledCost(15, 0), Enchantment.leveledCost(65, 0), 4,
            false, true, true, EquipmentSlot.MAINHAND);

    public static final Enchantment LUMBERJACK = create(
            tag("lumberjack"), tag("lumberjack"),
            2, 1, Enchantment.leveledCost(15, 0), Enchantment.leveledCost(65, 0), 4,
            false, true, true, EquipmentSlot.MAINHAND);

    private static TagKey<Item> tag(String name) {
        return TagKey.of(RegistryKeys.ITEM, new Identifier(LibriMod.MOD_ID, "enchantable/" + name));
    }

    private static Enchantment create(TagKey<Item> supported, TagKey<Item> primary,
                                      int weight, int maxLevel,
                                      Enchantment.Cost minCost, Enchantment.Cost maxCost, int anvilCost,
                                      boolean treasure, boolean tradeable, boolean randomSelection,
                                      EquipmentSlot... slots) {
        return new LibriEnchantment(
                Enchantment.properties(supported, primary, weight, maxLevel, minCost, maxCost, anvilCost, slots),
                treasure, tradeable, randomSelection);
    }

    public static void register() {
        registerOne("eternity", ETERNITY);
        registerOne("telekinesis", TELEKINESIS);
        registerOne("phoenix", PHOENIX);
        registerOne("sacrifice", SACRIFICE);
        registerOne("swift_strike", SWIFT_STRIKE);
        registerOne("smelting_speed", SMELTING_SPEED);
        registerOne("fuel_efficiency", FUEL_EFFICIENCY);
        registerOne("smelting_touch", SMELTING_TOUCH);
        registerOne("hush", HUSH);
        registerOne("soulbound", SOULBOUND);
        registerOne("sifting", SIFTING);
        registerOne("resonance", RESONANCE);
        registerOne("trophy", TROPHY);
        registerOne("paralysis", PARALYSIS);
        registerOne("crusher", CRUSHER);
        registerOne("lumberjack", LUMBERJACK);
    }

    private static void registerOne(String name, Enchantment enchantment) {
        Registry.register(Registries.ENCHANTMENT, new Identifier(LibriMod.MOD_ID, name), enchantment);
    }

    public static final class LibriEnchantment extends Enchantment {
        private final boolean treasure;
        private final boolean tradeable;
        private final boolean randomSelection;

        public LibriEnchantment(Properties properties, boolean treasure, boolean tradeable, boolean randomSelection) {
            super(properties);
            this.treasure = treasure;
            this.tradeable = tradeable;
            this.randomSelection = randomSelection;
        }

        @Override
        public boolean isTreasure() {
            return treasure;
        }

        @Override
        public boolean isAvailableForEnchantedBookOffer() {
            return tradeable;
        }

        @Override
        public boolean isAvailableForRandomSelection() {
            return randomSelection;
        }
    }
}
