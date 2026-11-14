package com.libri.enchantment;

import com.libri.LibriMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.enchantment.Enchantment;

public class ModEnchantments {
    public static final ResourceKey<Enchantment> ETERNITY = of("eternity");
    public static final ResourceKey<Enchantment> TELEKINESIS = of("telekinesis");
    public static final ResourceKey<Enchantment> PHOENIX = of("phoenix");
    public static final ResourceKey<Enchantment> SACRIFICE = of("sacrifice");
    public static final ResourceKey<Enchantment> SWIFT_STRIKE = of("swift_strike");
    public static final ResourceKey<Enchantment> SMELTING_SPEED = of("smelting_speed");
    public static final ResourceKey<Enchantment> FUEL_EFFICIENCY = of("fuel_efficiency");
    public static final ResourceKey<Enchantment> SMELTING_TOUCH = of("smelting_touch");
    public static final ResourceKey<Enchantment> HUSH = of("hush");
    public static final ResourceKey<Enchantment> SOULBOUND = of("soulbound");
    public static final ResourceKey<Enchantment> SIFTING = of("sifting");
    public static final ResourceKey<Enchantment> RESONANCE = of("resonance");
    public static final ResourceKey<Enchantment> TROPHY = of("trophy");
    public static final ResourceKey<Enchantment> PARALYSIS = of("paralysis");
    public static final ResourceKey<Enchantment> CRUSHER = of("crusher");
    public static final ResourceKey<Enchantment> LUMBERJACK = of("lumberjack");

    private static ResourceKey<Enchantment> of(String name) {
        return ResourceKey.create(Registries.ENCHANTMENT, Identifier.fromNamespaceAndPath(LibriMod.MOD_ID, name));
    }
}
