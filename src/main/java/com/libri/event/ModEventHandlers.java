package com.libri.event;

import com.libri.enchantment.ModEnchantmentHelper;
import com.libri.enchantment.ModEnchantments;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import java.util.*;

public class ModEventHandlers {
    private static final Map<UUID, Integer> TICKER = new HashMap<>();
    private static final Identifier SWIFT_STRIKE_MODIFIER_ID = Identifier.of("libri", "swift_strike");

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(ModEventHandlers::onServerTick);
    }

    private static void onServerTick(MinecraftServer server) {
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            int ticks = TICKER.getOrDefault(player.getUuid(), 0);
            TICKER.put(player.getUuid(), ticks + 1);

            handlePhoenix(player, ticks);

            if (ticks % 40 == 0) {
                handleWeaponEffects(player);
            }
        }
    }

    private static void handleWeaponEffects(ServerPlayerEntity player) {
        ItemStack weapon = player.getMainHandStack();
        if (weapon.isEmpty()) return;

        var ench = EnchantmentHelper.getEnchantments(weapon);
        int swiftLevel = ModEnchantmentHelper.getLevel(ModEnchantments.SWIFT_STRIKE, ench);
        var attr = player.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_SPEED);
        if (attr != null) {
            attr.removeModifier(SWIFT_STRIKE_MODIFIER_ID);
            if (swiftLevel > 0) {
                attr.addTemporaryModifier(new EntityAttributeModifier(
                    SWIFT_STRIKE_MODIFIER_ID, swiftLevel * 0.5,
                    EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE));
            }
        }
    }

    private static void handlePhoenix(ServerPlayerEntity player, int tick) {
        for (int i = 0; i < 40; i++) {
            ItemStack stack = i < 36 ? player.getInventory().main.get(i) : player.getInventory().armor.get(i - 36);
            if (stack.isEmpty() || !stack.hasEnchantments() || stack.getDamage() == 0) continue;
            int level = ModEnchantmentHelper.getLevel(ModEnchantments.PHOENIX, EnchantmentHelper.getEnchantments(stack));
            if (level > 0 && tick % Math.max(1, 100 / level) == 0) {
                stack.setDamage(Math.max(0, stack.getDamage() - 1));
            }
        }
    }
}
