package com.libri.mixin;

import com.libri.enchantment.ModEnchantmentHelper;
import com.libri.enchantment.ModEnchantments;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerLevel.class)
public class HushMixin {

    @Unique
    private static boolean libri$hasHush(ServerPlayer player, boolean checkMainHand) {
        ItemStack leggings = player.getItemBySlot(EquipmentSlot.LEGS);
        if (!leggings.isEmpty() && ModEnchantmentHelper.getLevel(ModEnchantments.HUSH, leggings) > 0) {
            return true;
        }
        if (checkMainHand) {
            ItemStack mainHand = player.getMainHandItem();
            return !mainHand.isEmpty() && ModEnchantmentHelper.getLevel(ModEnchantments.HUSH, mainHand) > 0;
        }
        return false;
    }

    @Inject(method = "gameEvent(Lnet/minecraft/core/Holder;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/level/gameevent/GameEvent$Context;)V",
            at = @At("HEAD"), cancellable = true)
    private void onEmitGameEvent(Holder<GameEvent> event, Vec3 pos, GameEvent.Context emitter, CallbackInfo ci) {
        if (!(emitter.sourceEntity() instanceof ServerPlayer player)) return;

        boolean isStep = event.is(GameEvent.STEP) || event.is(GameEvent.HIT_GROUND);
        boolean isBlockBreak = event.is(GameEvent.BLOCK_DESTROY);
        if (!isStep && !isBlockBreak) return;

        if (libri$hasHush(player, isBlockBreak)) {
            ci.cancel();
        }
    }

    @Inject(method = "levelEvent(Lnet/minecraft/world/entity/Entity;ILnet/minecraft/core/BlockPos;I)V",
            at = @At("HEAD"), cancellable = true)
    private void onLevelEvent(Entity entity, int type, BlockPos pos, int data, CallbackInfo ci) {
        if (type == 2001 && entity instanceof ServerPlayer player && libri$hasHush(player, true)) {
            ci.cancel();
        }
    }
}
