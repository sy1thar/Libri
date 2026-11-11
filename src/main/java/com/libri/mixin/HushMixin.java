package com.libri.mixin;

import com.libri.enchantment.ModEnchantmentHelper;
import com.libri.enchantment.ModEnchantments;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.event.GameEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerWorld.class)
public class HushMixin {

    @Unique
    private static boolean libri$hasHush(ServerPlayerEntity player, boolean checkMainHand) {
        ItemStack leggings = player.getEquippedStack(EquipmentSlot.LEGS);
        if (!leggings.isEmpty() && ModEnchantmentHelper.getLevel(ModEnchantments.HUSH, leggings) > 0) {
            return true;
        }
        if (checkMainHand) {
            ItemStack mainHand = player.getMainHandStack();
            return !mainHand.isEmpty() && ModEnchantmentHelper.getLevel(ModEnchantments.HUSH, mainHand) > 0;
        }
        return false;
    }

    @Inject(method = "emitGameEvent(Lnet/minecraft/world/event/GameEvent;Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/world/event/GameEvent$Emitter;)V",
            at = @At("HEAD"), cancellable = true)
    private void onEmitGameEvent(GameEvent event, Vec3d pos, GameEvent.Emitter emitter, CallbackInfo ci) {
        if (!(emitter.sourceEntity() instanceof ServerPlayerEntity player)) return;

        boolean isStep = event == GameEvent.STEP || event == GameEvent.HIT_GROUND;
        boolean isBlockBreak = event == GameEvent.BLOCK_DESTROY;
        if (!isStep && !isBlockBreak) return;

        if (libri$hasHush(player, isBlockBreak)) {
            ci.cancel();
        }
    }

    @Inject(method = "syncWorldEvent(Lnet/minecraft/entity/player/PlayerEntity;ILnet/minecraft/util/math/BlockPos;I)V",
            at = @At("HEAD"), cancellable = true)
    private void onSyncWorldEvent(PlayerEntity player, int eventId, BlockPos pos, int data, CallbackInfo ci) {
        if (eventId == 2001 && player instanceof ServerPlayerEntity serverPlayer && libri$hasHush(serverPlayer, true)) {
            ci.cancel();
        }
    }
}
