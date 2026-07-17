package com.libri.enchantment;

import com.libri.LibriMod;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

public class ModEnchantments {
    public static final RegistryKey<Enchantment> ETERNITY = of("eternity");
    public static final RegistryKey<Enchantment> TELEKINESIS = of("telekinesis");
    public static final RegistryKey<Enchantment> PHOENIX = of("phoenix");
    public static final RegistryKey<Enchantment> SACRIFICE = of("sacrifice");
    public static final RegistryKey<Enchantment> SWIFT_STRIKE = of("swift_strike");
    public static final RegistryKey<Enchantment> SMELTING_SPEED = of("smelting_speed");
    public static final RegistryKey<Enchantment> FUEL_EFFICIENCY = of("fuel_efficiency");
    public static final RegistryKey<Enchantment> SMELTING_TOUCH = of("smelting_touch");
    public static final RegistryKey<Enchantment> HUSH = of("hush");

    private static RegistryKey<Enchantment> of(String name) {
        return RegistryKey.of(RegistryKeys.ENCHANTMENT, Identifier.of(LibriMod.MOD_ID, name));
    }
}
