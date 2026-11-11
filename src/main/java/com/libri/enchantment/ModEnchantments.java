package com.libri.enchantment;

import com.libri.LibriMod;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModEnchantments {
    public static final Enchantment ETERNITY = new LibriEnchantment(
            Enchantment.Rarity.RARE, EnchantmentTarget.BREAKABLE, EquipmentSlot.values(),
            1, 30, 8, 50, 8, true, true, true);

    public static final Enchantment TELEKINESIS = new LibriEnchantment(
            Enchantment.Rarity.RARE, EnchantmentTarget.DIGGER, new EquipmentSlot[]{EquipmentSlot.MAINHAND},
            1, 20, 5, 40, 5, false, true, true);

    public static final Enchantment PHOENIX = new LibriEnchantment(
            Enchantment.Rarity.RARE, EnchantmentTarget.BREAKABLE, EquipmentSlot.values(),
            10, 25, 7, 50, 7, false, true, true);

    public static final Enchantment SACRIFICE = new LibriEnchantment(
            Enchantment.Rarity.RARE, EnchantmentTarget.WEAPON, new EquipmentSlot[]{EquipmentSlot.MAINHAND},
            2, 20, 8, 50, 8, false, true, true);

    public static final Enchantment SWIFT_STRIKE = new LibriEnchantment(
            Enchantment.Rarity.UNCOMMON, EnchantmentTarget.WEAPON, new EquipmentSlot[]{EquipmentSlot.MAINHAND},
            2, 8, 4, 20, 4, false, true, true);

    public static final Enchantment SMELTING_SPEED = new LibriEnchantment(
            Enchantment.Rarity.RARE, EnchantmentTarget.BREAKABLE, new EquipmentSlot[]{EquipmentSlot.MAINHAND},
            10, 10, 5, 50, 5, false, true, true);

    public static final Enchantment FUEL_EFFICIENCY = new LibriEnchantment(
            Enchantment.Rarity.RARE, EnchantmentTarget.BREAKABLE, new EquipmentSlot[]{EquipmentSlot.MAINHAND},
            10, 10, 5, 50, 5, false, true, true);

    public static final Enchantment SMELTING_TOUCH = new LibriEnchantment(
            Enchantment.Rarity.RARE, EnchantmentTarget.DIGGER, new EquipmentSlot[]{EquipmentSlot.MAINHAND},
            1, 25, 10, 50, 10, true, true, true);

    public static final Enchantment HUSH = new LibriEnchantment(
            Enchantment.Rarity.RARE, EnchantmentTarget.ARMOR_LEGS, new EquipmentSlot[]{EquipmentSlot.LEGS},
            1, 25, 10, 50, 10, true, true, true);

    public static final Enchantment SOULBOUND = new LibriEnchantment(
            Enchantment.Rarity.RARE, EnchantmentTarget.VANISHABLE, EquipmentSlot.values(),
            1, 15, 0, 65, 0, false, true, true);

    public static final Enchantment SIFTING = new LibriEnchantment(
            Enchantment.Rarity.RARE, EnchantmentTarget.DIGGER, new EquipmentSlot[]{EquipmentSlot.MAINHAND},
            1, 15, 0, 65, 0, false, true, true);

    public static final Enchantment RESONANCE = new LibriEnchantment(
            Enchantment.Rarity.RARE, EnchantmentTarget.DIGGER, new EquipmentSlot[]{EquipmentSlot.MAINHAND},
            1, 20, 0, 70, 0, false, true, true);

    public static final Enchantment TROPHY = new LibriEnchantment(
            Enchantment.Rarity.RARE, EnchantmentTarget.WEAPON, new EquipmentSlot[]{EquipmentSlot.MAINHAND},
            1, 15, 15, 65, 20, false, true, true);

    public static final Enchantment PARALYSIS = new LibriEnchantment(
            Enchantment.Rarity.RARE, EnchantmentTarget.BOW, new EquipmentSlot[]{EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND},
            1, 15, 0, 65, 0, true, false, false);

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
    }

    private static void registerOne(String name, Enchantment enchantment) {
        Registry.register(Registries.ENCHANTMENT, new Identifier(LibriMod.MOD_ID, name), enchantment);
    }

    public static final class LibriEnchantment extends Enchantment {
        private final int maxLevel;
        private final int minBaseCost;
        private final int minCostPerLevel;
        private final int maxBaseCost;
        private final int maxCostPerLevel;
        private final boolean treasure;
        private final boolean tradeable;
        private final boolean randomSelection;

        public LibriEnchantment(Rarity rarity, EnchantmentTarget target, EquipmentSlot[] slots,
                                int maxLevel, int minBaseCost, int minCostPerLevel,
                                int maxBaseCost, int maxCostPerLevel,
                                boolean treasure, boolean tradeable, boolean randomSelection) {
            super(rarity, target, slots);
            this.maxLevel = maxLevel;
            this.minBaseCost = minBaseCost;
            this.minCostPerLevel = minCostPerLevel;
            this.maxBaseCost = maxBaseCost;
            this.maxCostPerLevel = maxCostPerLevel;
            this.treasure = treasure;
            this.tradeable = tradeable;
            this.randomSelection = randomSelection;
        }

        @Override
        public int getMaxLevel() {
            return maxLevel;
        }

        @Override
        public int getMinPower(int level) {
            return minBaseCost + (level - 1) * minCostPerLevel;
        }

        @Override
        public int getMaxPower(int level) {
            return maxBaseCost + (level - 1) * maxCostPerLevel;
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
