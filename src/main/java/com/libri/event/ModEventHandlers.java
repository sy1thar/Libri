package com.libri.event;

import com.libri.enchantment.ModEnchantmentHelper;
import com.libri.enchantment.ModEnchantments;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.block.BlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;

public class ModEventHandlers {
    private static final Map<UUID, Integer> SWIFT_LEVEL_CACHE = new HashMap<>();
    private static final UUID SWIFT_STRIKE_MODIFIER_UUID = UUID.fromString("4a1b2c3d-5e6f-4789-9abc-def012345678");
    private static final ThreadLocal<Boolean> IS_RESONATING = ThreadLocal.withInitial(() -> false);
    private static final ThreadLocal<Boolean> IS_CUSTOM_BREAKING = ThreadLocal.withInitial(() -> false);

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(ModEventHandlers::onServerTick);

        ServerPlayerEvents.COPY_FROM.register((oldPlayer, newPlayer, alive) -> {
            if (!alive) {
                for (int i = 0; i < 41; i++) {
                    ItemStack stack = i < 36 ? oldPlayer.getInventory().main.get(i) : (i < 40 ? oldPlayer.getInventory().armor.get(i - 36) : oldPlayer.getInventory().offHand.get(0));
                    if (!stack.isEmpty() && ModEnchantmentHelper.getLevel(ModEnchantments.SOULBOUND, stack) > 0) {
                        if (i < 36) newPlayer.getInventory().main.set(i, stack.copy());
                        else if (i < 40) newPlayer.getInventory().armor.set(i - 36, stack.copy());
                        else newPlayer.getInventory().offHand.set(0, stack.copy());
                    }
                }
            }
        });

        PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, blockEntity) -> {
            if (world.isClient || IS_RESONATING.get() || IS_CUSTOM_BREAKING.get()) return true;

            ItemStack tool = player.getMainHandStack();
            if (tool.isEmpty()) return true;

            var ench = EnchantmentHelper.get(tool);

            // 1. Resonance (Ore veins)
            int resonance = ModEnchantmentHelper.getLevel(ModEnchantments.RESONANCE, ench);
            if (resonance > 0) {
                String blockName = Registries.BLOCK.getId(state.getBlock()).getPath();
                boolean isOre = blockName.endsWith("_ore") || blockName.equals("ancient_debris") || state.streamTags().anyMatch(t -> t.id().getPath().endsWith("ores"));

                if (isOre) {
                    return handleResonance(world, (ServerPlayerEntity) player, pos, state, blockName);
                }
            }

            // 2. Lumberjack (Fell whole trees with leaf verification)
            int lumberjack = ModEnchantmentHelper.getLevel(ModEnchantments.LUMBERJACK, ench);
            if (lumberjack > 0 && tool.isIn(ItemTags.AXES) && state.isIn(BlockTags.LOGS)) {
                boolean handled = handleLumberjack((ServerWorld) world, (ServerPlayerEntity) player, pos, state, tool);
                if (handled) {
                    return false;
                }
            }

            // 3. Crusher (3x3 area mining with per-tool block filtering)
            int crusher = ModEnchantmentHelper.getLevel(ModEnchantments.CRUSHER, ench);
            if (crusher > 0) {
                boolean handled = handleCrusher((ServerWorld) world, (ServerPlayerEntity) player, pos, state, tool);
                if (handled) {
                    return false;
                }
            }

            return true;
        });

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) ->
                SWIFT_LEVEL_CACHE.remove(handler.getPlayer().getUuid()));
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) ->
                SWIFT_LEVEL_CACHE.remove(oldPlayer.getUuid()));
    }

    private static boolean handleResonance(net.minecraft.world.World world, ServerPlayerEntity player, BlockPos pos, BlockState state, String blockName) {
        IS_RESONATING.set(true);
        com.libri.helper.TelepathyHelper.RESONANCE_ORIGIN.set(pos);
        com.libri.helper.TelepathyHelper.RESONANCE_DROPS.set(new java.util.ArrayList<>());
        try {
            int maxBlocks = com.libri.config.ModConfig.get().resonanceMaxBlocks;
            String targetOreName = blockName.replace("deepslate_", "").replace("nether_", "").replace("end_", "");
            Queue<BlockPos> queue = new LinkedList<>();
            Set<BlockPos> visited = new HashSet<>();

            queue.add(pos);
            visited.add(pos);

            player.interactionManager.tryBreakBlock(pos);
            int broken = 1;

            while (!queue.isEmpty() && broken < maxBlocks) {
                BlockPos current = queue.poll();

                for (int x = -1; x <= 1; x++) {
                    for (int y = -1; y <= 1; y++) {
                        for (int z = -1; z <= 1; z++) {
                            if (x == 0 && y == 0 && z == 0) continue;
                            BlockPos neighbor = current.add(x, y, z);
                            if (!visited.contains(neighbor)) {
                                visited.add(neighbor);
                                String neighborName = Registries.BLOCK.getId(world.getBlockState(neighbor).getBlock()).getPath();
                                if (neighborName.replace("deepslate_", "").replace("nether_", "").replace("end_", "").equals(targetOreName)) {
                                    queue.add(neighbor);
                                    player.interactionManager.tryBreakBlock(neighbor);
                                    broken++;
                                    if (broken >= maxBlocks) break;
                                }
                            }
                        }
                        if (broken >= maxBlocks) break;
                    }
                    if (broken >= maxBlocks) break;
                }
            }

            java.util.List<net.minecraft.item.ItemStack> drops = com.libri.helper.TelepathyHelper.RESONANCE_DROPS.get();
            if (drops != null && !drops.isEmpty()) {
                java.util.List<net.minecraft.item.ItemStack> compressed = new java.util.ArrayList<>();
                for (net.minecraft.item.ItemStack stack : drops) {
                    if (stack.isEmpty()) continue;
                    for (net.minecraft.item.ItemStack c : compressed) {
                        if (net.minecraft.item.ItemStack.canCombine(c, stack)) {
                            int space = c.getMaxCount() - c.getCount();
                            if (space > 0) {
                                int toAdd = Math.min(space, stack.getCount());
                                c.increment(toAdd);
                                stack.decrement(toAdd);
                                if (stack.isEmpty()) {
                                    break;
                                }
                            }
                        }
                    }
                    if (!stack.isEmpty()) {
                        compressed.add(stack);
                    }
                }

                double d = pos.getX() + 0.5 + world.random.nextDouble() * 0.5 - 0.25;
                double e = pos.getY() + 0.5 + world.random.nextDouble() * 0.5 - 0.25;
                double f = pos.getZ() + 0.5 + world.random.nextDouble() * 0.5 - 0.25;
                double vx = world.random.nextDouble() * 0.2 - 0.1;
                double vy = 0.2;
                double vz = world.random.nextDouble() * 0.2 - 0.1;

                for (net.minecraft.item.ItemStack c : compressed) {
                    net.minecraft.entity.ItemEntity itemEntity = new net.minecraft.entity.ItemEntity(world, d, e, f, c);
                    itemEntity.setVelocity(vx, vy, vz);
                    itemEntity.setToDefaultPickupDelay();
                    world.spawnEntity(itemEntity);
                }
            }
        } finally {
            IS_RESONATING.set(false);
            com.libri.helper.TelepathyHelper.RESONANCE_ORIGIN.remove();
            com.libri.helper.TelepathyHelper.RESONANCE_DROPS.remove();
        }

        return false;
    }

    private static boolean handleLumberjack(ServerWorld world, ServerPlayerEntity player, BlockPos pos, BlockState state, ItemStack tool) {
        if (player.isSneaking()) return false;

        int maxLogs = 512;
        Set<BlockPos> logs = new LinkedHashSet<>();
        Queue<BlockPos> queue = new LinkedList<>();

        logs.add(pos);
        queue.add(pos);

        int leavesFound = 0;

        while (!queue.isEmpty() && logs.size() < maxLogs) {
            BlockPos current = queue.poll();

            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        if (dx == 0 && dy == 0 && dz == 0) continue;
                        BlockPos neighbor = current.add(dx, dy, dz);

                        if (world.getBlockState(neighbor).isIn(BlockTags.LEAVES)) {
                            leavesFound++;
                        }

                        if (!logs.contains(neighbor) && world.getBlockState(neighbor).isIn(BlockTags.LOGS)) {
                            logs.add(neighbor);
                            queue.add(neighbor);
                            if (logs.size() >= maxLogs) break;
                        }
                    }
                    if (logs.size() >= maxLogs) break;
                }
                if (logs.size() >= maxLogs) break;
            }
        }

        if (leavesFound < 3) {
            return false;
        }

        IS_CUSTOM_BREAKING.set(true);
        try {
            for (BlockPos logPos : logs) {
                if (tool.isEmpty()) break;
                player.interactionManager.tryBreakBlock(logPos);
            }
        } finally {
            IS_CUSTOM_BREAKING.set(false);
        }

        return true;
    }

    private static boolean handleCrusher(ServerWorld world, ServerPlayerEntity player, BlockPos pos, BlockState state, ItemStack tool) {
        if (player.isSneaking()) return false;

        HitResult hit = player.raycast(6.0, 0, false);
        Direction face = Direction.UP;
        if (hit instanceof BlockHitResult bhr) {
            face = bhr.getSide();
        } else {
            face = player.getHorizontalFacing().getOpposite();
        }

        boolean isPickaxe = tool.isIn(ItemTags.PICKAXES);
        boolean isShovel = tool.isIn(ItemTags.SHOVELS);
        boolean isAxe = tool.isIn(ItemTags.AXES);

        List<BlockPos> targetPositions = new ArrayList<>();
        targetPositions.add(pos);

        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                if (i == 0 && j == 0) continue;
                BlockPos target;
                if (face.getAxis() == Direction.Axis.Y) {
                    target = pos.add(i, 0, j);
                } else if (face.getAxis() == Direction.Axis.X) {
                    target = pos.add(0, i, j);
                } else {
                    target = pos.add(i, j, 0);
                }

                BlockState targetState = world.getBlockState(target);
                if (targetState.isAir()) continue;
                if (targetState.getHardness(world, target) < 0) continue;

                if (isPickaxe) {
                    targetPositions.add(target);
                } else if (isShovel) {
                    if (targetState.isIn(BlockTags.SHOVEL_MINEABLE) || tool.isSuitableFor(targetState)) {
                        targetPositions.add(target);
                    }
                } else if (isAxe) {
                    if (targetState.isIn(BlockTags.AXE_MINEABLE) || tool.isSuitableFor(targetState)) {
                        targetPositions.add(target);
                    }
                }
            }
        }

        IS_CUSTOM_BREAKING.set(true);
        try {
            for (BlockPos targetPos : targetPositions) {
                if (tool.isEmpty()) break;
                player.interactionManager.tryBreakBlock(targetPos);
            }
        } finally {
            IS_CUSTOM_BREAKING.set(false);
        }

        return true;
    }

    private static void onServerTick(MinecraftServer server) {
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            handleWeaponEffects(player);
            handlePhoenix(player);
        }
    }

    private static void handleWeaponEffects(ServerPlayerEntity player) {
        ItemStack weapon = player.getMainHandStack();
        int swiftLevel = weapon.isEmpty()
                ? 0
                : ModEnchantmentHelper.getLevel(ModEnchantments.SWIFT_STRIKE, EnchantmentHelper.get(weapon));

        Integer cached = SWIFT_LEVEL_CACHE.get(player.getUuid());
        if (cached != null && cached == swiftLevel) return;
        SWIFT_LEVEL_CACHE.put(player.getUuid(), swiftLevel);

        var attr = player.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_SPEED);
        if (attr == null) return;

        attr.removeModifier(SWIFT_STRIKE_MODIFIER_UUID);
        if (swiftLevel > 0) {
            attr.addTemporaryModifier(new EntityAttributeModifier(
                SWIFT_STRIKE_MODIFIER_UUID, "libri:swift_strike", swiftLevel * 0.5,
                EntityAttributeModifier.Operation.MULTIPLY_BASE));
        }
    }

    private static void handlePhoenix(ServerPlayerEntity player) {
        for (int i = 0; i < 41; i++) {
            ItemStack stack = i < 36 ? player.getInventory().main.get(i) : (i < 40 ? player.getInventory().armor.get(i - 36) : player.getInventory().offHand.get(0));
            if (stack.isEmpty() || !stack.hasEnchantments() || !stack.isDamageable() || stack.getDamage() == 0) continue;
            int level = ModEnchantmentHelper.getLevel(ModEnchantments.PHOENIX, stack);
            if (level > 0 && player.age % Math.max(1, 100 / level) == 0) {
                stack.setDamage(Math.max(0, stack.getDamage() - 1));
            }
        }
    }
}
