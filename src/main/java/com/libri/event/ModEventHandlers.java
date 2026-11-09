package com.libri.event;

import com.libri.enchantment.ModEnchantmentHelper;
import com.libri.enchantment.ModEnchantments;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.HashSet;

public class ModEventHandlers {
    private static final Map<UUID, Integer> SWIFT_LEVEL_CACHE = new HashMap<>();
    private static final ResourceLocation SWIFT_STRIKE_MODIFIER_ID = ResourceLocation.fromNamespaceAndPath("libri", "swift_strike");
    private static final ThreadLocal<Boolean> IS_RESONATING = ThreadLocal.withInitial(() -> false);

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
            if (world.isClientSide || IS_RESONATING.get()) return true;

            ItemStack tool = player.getMainHandItem();
            if (tool.isEmpty()) return true;

            var ench = EnchantmentHelper.getEnchantmentsForCrafting(tool);
            int resonance = ModEnchantmentHelper.getLevel(ModEnchantments.RESONANCE, ench);
            if (resonance <= 0) return true;

            String blockName = BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath();
            boolean isOre = blockName.endsWith("_ore") || blockName.equals("ancient_debris") || state.getTags().anyMatch(t -> t.location().getPath().endsWith("ores"));

            if (!isOre) return true;

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

                ServerPlayer serverPlayer = (ServerPlayer) player;
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
                    
                    double d = pos.getX() + 0.5 + world.random.nextDouble() * 0.5 - 0.25;
                    double e = pos.getY() + 0.5 + world.random.nextDouble() * 0.5 - 0.25;
                    double f = pos.getZ() + 0.5 + world.random.nextDouble() * 0.5 - 0.25;
                    double vx = world.random.nextDouble() * 0.2 - 0.1;
                    double vy = 0.2;
                    double vz = world.random.nextDouble() * 0.2 - 0.1;

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
        });
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) ->
                SWIFT_LEVEL_CACHE.remove(handler.getPlayer().getUUID()));
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) ->
                SWIFT_LEVEL_CACHE.remove(oldPlayer.getUUID()));
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

        // Only touch the attribute when the level actually changed, so slot switching
        // applies on the very next tick instead of a fixed polling interval.
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
            ItemStack stack = i < 36 ? player.getInventory().items.get(i) : (i < 40 ? player.getInventory().armor.get(i - 36) : player.getInventory().offhand.get(0));
            if (stack.isEmpty() || !stack.isEnchanted() || stack.getDamageValue() == 0) continue;
            int level = ModEnchantmentHelper.getLevel(ModEnchantments.PHOENIX, stack);
            if (level > 0 && player.tickCount % Math.max(1, 100 / level) == 0) {
                stack.setDamageValue(Math.max(0, stack.getDamageValue() - 1));
            }
        }
    }
}
