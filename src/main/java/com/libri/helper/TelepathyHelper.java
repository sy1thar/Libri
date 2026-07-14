package com.libri.helper;

import net.minecraft.server.network.ServerPlayerEntity;

public class TelepathyHelper {
    public static final ThreadLocal<ServerPlayerEntity> TELEKINESIS_ATTACKER = new ThreadLocal<>();
    public static final ThreadLocal<ServerPlayerEntity> BLOCK_MINER = new ThreadLocal<>();
}
