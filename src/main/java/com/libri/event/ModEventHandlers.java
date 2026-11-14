package com.libri.event;

import com.libri.enchantment.ModEnchantmentHelper;
import com.libri.enchantment.ModEnchantments;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
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
    private static final Identifier SWIFT_STRIKE_MODIFIER_ID = Identifier.fromNamespaceAndPath("libri", "swift_strike");
    private static final ThreadLocal<Boolean> IS_RESONATING = ThreadLocal.withInitial(() -> false);
    private static final ThreadLocal<Boolean> IS_CUSTOM_BREAKING = ThreadLocal.withInitial(() -> false);

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(ModEventHandlers::onServerTick);

        ServerPlayerEvents.COPY_FROM.register((oldPlayer, newPlayer, alive) -> {
            if (!alive) {
                for (int i = 0; i < oldPlayer.getInventory().getContainerSize(); i++) {
                    ItemStack stack = oldPlayer.getInventory().getItem(i);
                    if (!stack.isEmpty() && ModEnchantmentHelper.getLevel(ModEnchantments.SOULBOUND, stack) > 0) {
                        newPlayer.getInventory().setItem(i, stack.copy());
                    }
                }
            }
        });

        PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, blockEntity) -> {
            if (world.isClientSide() || IS_RESONATING.get() || IS_CUSTOM_BREAKING.get()) return true;

            ItemStack tool = player.getMainHandItem();
            if (tool.isEmpty()) return true;

            var ench = EnchantmentHelper.getEnchantmentsForCrafting(tool);

            // 1. Resonance (Ore veins)
            int resonance = ModEnchantmentHelper.getLevel(ModEnchantments.RESONANCE, ench);
            if (resonance > 0) {
                String blockName = BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath();
                boolean isOre = blockName.endsWith("_ore") || blockName.equals("ancient_debris") || state.tags().anyMatch(t -> t.location().getPath().endsWith("ores"));

                if (isOre) {
                    return handleResonance(world, (ServerPlayer) player, pos, state, blockName);
                }
            }

            // 2. Lumberjack (Fell whole trees with leaf verification)
            int lumberjack = ModEnchantmentHelper.getLevel(ModEnchantments.LUMBERJACK, ench);
            if (lumberjack > 0 && tool.is(ItemTags.AXES) && state.is(BlockTags.LOGS)) {
                boolean handled = handleLumberjack((ServerLevel) world, (ServerPlayer) player, pos, state, tool);
                if (handled) {
                    return false;
                }
            }

            // 3. Crusher (3x3 area mining with per-tool block filtering)
            int crusher = ModEnchantmentHelper.getLevel(ModEnchantments.CRUSHER, ench);
            if (crusher > 0) {
                boolean handled = handleCrusher((ServerLevel) world, (ServerPlayer) player, pos, state, tool);
                if (handled) {
                    return false;
                }
            }

            return true;
        });

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) ->
                SWIFT_LEVEL_CACHE.remove(handler.getPlayer().getUUID()));
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) ->
                SWIFT_LEVEL_CACHE.remove(oldPlayer.getUUID()));
    }

    private static boolean handleResonance(net.minecraft.world.level.Level world, ServerPlayer serverPlayer, BlockPos pos, BlockState state, String blockName) {
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

            if (!serverPlayer.gameMode.destroyBlock(pos)) return false;
            int broken = 1;

            while (!queue.isEmpty() && broken < maxBlocks) {
                BlockPos current = queue.poll();

                for (int x = -1; x <= 1; x++) {
                    for (int y = -1; y <= 1; y++) {
                        for (int z = -1; z <= 1; z++) {
                            if (x == 0 && y == 0 && z == 0) continue;
                            BlockPos neighbor = current.offset(x, y, z);
                            if (!visited.contains(neighbor)) {
                                visited.add(neighbor);
                                String neighborName = BuiltInRegistries.BLOCK.getKey(world.getBlockState(neighbor).getBlock()).getPath();
                                if (neighborName.replace("deepslate_", "").replace("nether_", "").replace("end_", "").equals(targetOreName)) {
                                    if (!serverPlayer.gameMode.destroyBlock(neighbor)) continue;
                                    queue.add(neighbor);
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

            java.util.List<net.minecraft.world.item.ItemStack> drops = com.libri.helper.TelepathyHelper.RESONANCE_DROPS.get();
            if (drops != null && !drops.isEmpty()) {
                java.util.List<net.minecraft.world.item.ItemStack> compressed = new java.util.ArrayList<>();
                for (net.minecraft.world.item.ItemStack stack : drops) {
                    if (stack.isEmpty()) continue;
                    for (net.minecraft.world.item.ItemStack c : compressed) {
                        if (net.minecraft.world.item.ItemStack.isSameItemSameComponents(c, stack)) {
                            int space = c.getMaxStackSize() - c.getCount();
                            if (space > 0) {
                                int toAdd = Math.min(space, stack.getCount());
                                c.grow(toAdd);
                                stack.shrink(toAdd);
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

                double d = pos.getX() + 0.5 + world.getRandom().nextDouble() * 0.5 - 0.25;
                double e = pos.getY() + 0.5 + world.getRandom().nextDouble() * 0.5 - 0.25;
                double f = pos.getZ() + 0.5 + world.getRandom().nextDouble() * 0.5 - 0.25;
                double vx = world.getRandom().nextDouble() * 0.2 - 0.1;
                double vy = 0.2;
                double vz = world.getRandom().nextDouble() * 0.2 - 0.1;

                for (net.minecraft.world.item.ItemStack c : compressed) {
                    net.minecraft.world.entity.item.ItemEntity itemEntity = new net.minecraft.world.entity.item.ItemEntity(world, d, e, f, c);
                    itemEntity.setDeltaMovement(vx, vy, vz);
                    itemEntity.setDefaultPickUpDelay();
                    world.addFreshEntity(itemEntity);
                }
            }
        } finally {
            IS_RESONATING.set(false);
            com.libri.helper.TelepathyHelper.RESONANCE_ORIGIN.remove();
            com.libri.helper.TelepathyHelper.RESONANCE_DROPS.remove();
        }

        return false;
    }

    private static boolean handleLumberjack(ServerLevel world, ServerPlayer player, BlockPos pos, BlockState state, ItemStack tool) {
        if (player.isShiftKeyDown()) return false; // Sneak breaks single block

        // Scan connected logs
        Queue<BlockPos> queue = new LinkedList<>();
        Set<BlockPos> logs = new LinkedHashSet<>();
        queue.add(pos);
        logs.add(pos);

        int leavesFound = 0;
        int maxLogs = 512;

        while (!queue.isEmpty() && logs.size() < maxLogs) {
            BlockPos current = queue.poll();

            // Check surrounding area for leaves
            for (int lx = -2; lx <= 2; lx++) {
                for (int ly = -2; ly <= 2; ly++) {
                    for (int lz = -2; lz <= 2; lz++) {
                        BlockPos leafPos = current.offset(lx, ly, lz);
                        if (world.getBlockState(leafPos).is(BlockTags.LEAVES)) {
                            leavesFound++;
                        }
                    }
                }
            }

            // Find connected logs (including diagonals)
            for (int x = -1; x <= 1; x++) {
                for (int y = -1; y <= 1; y++) {
                    for (int z = -1; z <= 1; z++) {
                        if (x == 0 && y == 0 && z == 0) continue;
                        BlockPos neighbor = current.offset(x, y, z);
                        if (!logs.contains(neighbor)) {
                            BlockState neighborState = world.getBlockState(neighbor);
                            if (neighborState.is(BlockTags.LOGS)) {
                                logs.add(neighbor);
                                queue.add(neighbor);
                                if (logs.size() >= maxLogs) break;
                            }
                        }
                    }
                    if (logs.size() >= maxLogs) break;
                }
                if (logs.size() >= maxLogs) break;
            }
        }

        // If there are less than 3 leaves near the canopy, it's considered a wooden pillar or player building!
        // Break only the single clicked block.
        if (leavesFound < 3) {
            return false;
        }

        // It's a genuine tree! Fell all connected logs.
        IS_CUSTOM_BREAKING.set(true);
        try {
            for (BlockPos logPos : logs) {
                if (tool.isEmpty()) break;
                player.gameMode.destroyBlock(logPos);
            }
        } finally {
            IS_CUSTOM_BREAKING.set(false);
        }

        return true;
    }

    private static boolean handleCrusher(ServerLevel world, ServerPlayer player, BlockPos pos, BlockState state, ItemStack tool) {
        if (player.isShiftKeyDown()) return false; // Sneak breaks single block

        HitResult hit = player.pick(6.0, 0, false);
        Direction face = Direction.UP;
        if (hit instanceof BlockHitResult bhr) {
            face = bhr.getDirection();
        } else {
            face = player.getDirection().getOpposite();
        }

        boolean isPickaxe = tool.is(ItemTags.PICKAXES);
        boolean isShovel = tool.is(ItemTags.SHOVELS);
        boolean isAxe = tool.is(ItemTags.AXES);

        List<BlockPos> targetPositions = new ArrayList<>();
        targetPositions.add(pos);

        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                if (i == 0 && j == 0) continue;
                BlockPos target;
                if (face.getAxis() == Direction.Axis.Y) {
                    target = pos.offset(i, 0, j);
                } else if (face.getAxis() == Direction.Axis.X) {
                    target = pos.offset(0, i, j);
                } else {
                    target = pos.offset(i, j, 0);
                }

                BlockState targetState = world.getBlockState(target);
                if (targetState.isAir()) continue;
                if (targetState.getDestroySpeed(world, target) < 0) continue; // Unbreakable blocks (bedrock, portals)

                if (isPickaxe) {
                    // Pickaxe breaks ANY block in the 3x3 area!
                    targetPositions.add(target);
                } else if (isShovel) {
                    // Shovel only breaks shovel-mineable blocks!
                    if (targetState.is(BlockTags.MINEABLE_WITH_SHOVEL) || tool.isCorrectToolForDrops(targetState)) {
                        targetPositions.add(target);
                    }
                } else if (isAxe) {
                    // Axe only breaks axe-mineable blocks!
                    if (targetState.is(BlockTags.MINEABLE_WITH_AXE) || tool.isCorrectToolForDrops(targetState)) {
                        targetPositions.add(target);
                    }
                }
            }
        }

        IS_CUSTOM_BREAKING.set(true);
        try {
            for (BlockPos targetPos : targetPositions) {
                if (tool.isEmpty()) break;
                player.gameMode.destroyBlock(targetPos);
            }
        } finally {
            IS_CUSTOM_BREAKING.set(false);
        }

        return true;
    }

    private static void onServerTick(MinecraftServer server) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            handleWeaponEffects(player);
            handlePhoenix(player);
        }
    }

    private static void handleWeaponEffects(ServerPlayer player) {
        ItemStack weapon = player.getMainHandItem();
        int swiftLevel = weapon.isEmpty()
                ? 0
                : ModEnchantmentHelper.getLevel(ModEnchantments.SWIFT_STRIKE, EnchantmentHelper.getEnchantmentsForCrafting(weapon));

        Integer cached = SWIFT_LEVEL_CACHE.get(player.getUUID());
        if (cached != null && cached == swiftLevel) return;
        SWIFT_LEVEL_CACHE.put(player.getUUID(), swiftLevel);

        var attr = player.getAttribute(Attributes.ATTACK_SPEED);
        if (attr == null) return;

        attr.removeModifier(SWIFT_STRIKE_MODIFIER_ID);
        if (swiftLevel > 0) {
            attr.addTransientModifier(new AttributeModifier(
                SWIFT_STRIKE_MODIFIER_ID, swiftLevel * 0.5,
                AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
        }
    }

    private static void handlePhoenix(ServerPlayer player) {
        for (int i = 0; i < 41; i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.isEmpty() || !stack.isEnchanted() || stack.getDamageValue() == 0) continue;
            int level = ModEnchantmentHelper.getLevel(ModEnchantments.PHOENIX, stack);
            if (level > 0 && player.tickCount % Math.max(1, 100 / level) == 0) {
                stack.setDamageValue(Math.max(0, stack.getDamageValue() - 1));
            }
        }
    }
}
