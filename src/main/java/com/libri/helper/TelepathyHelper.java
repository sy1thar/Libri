package com.libri.helper;

import net.minecraft.server.level.ServerPlayer;

public class TelepathyHelper {
    public static final ThreadLocal<ServerPlayer> TELEKINESIS_ATTACKER = new ThreadLocal<>();
    public static final ThreadLocal<ServerPlayer> BLOCK_MINER = new ThreadLocal<>();
    public static final ThreadLocal<net.minecraft.core.BlockPos> RESONANCE_ORIGIN = new ThreadLocal<>();
    public static final ThreadLocal<java.util.List<net.minecraft.world.item.ItemStack>> RESONANCE_DROPS = new ThreadLocal<>();
}
