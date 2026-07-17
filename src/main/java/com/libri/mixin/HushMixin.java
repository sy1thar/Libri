package com.libri.mixin;

import com.libri.enchantment.ModEnchantmentHelper;
import com.libri.enchantment.ModEnchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.event.GameEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerWorld.class)
public class HushMixin {

    @Inject(method = "emitGameEvent(Lnet/minecraft/registry/entry/RegistryEntry;Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/world/event/GameEvent$Emitter;)V",
            at = @At("HEAD"), cancellable = true)
    private void onEmitGameEvent(RegistryEntry<GameEvent> event, Vec3d pos, GameEvent.Emitter emitter, CallbackInfo ci) {
        Entity entity = emitter.sourceEntity();
        if (!(entity instanceof ServerPlayerEntity player)) return;

        ItemStack stack = player.getMainHandStack();
        if (stack.isEmpty()) return;
        if (ModEnchantmentHelper.getLevel(ModEnchantments.HUSH, stack) <= 0) return;

        ci.cancel();
    }

    @Inject(method = "syncWorldEvent(Lnet/minecraft/entity/player/PlayerEntity;ILnet/minecraft/util/math/BlockPos;I)V",
            at = @At("HEAD"), cancellable = true)
    private void onSyncWorldEvent(PlayerEntity player, int eventId, BlockPos pos, int data, CallbackInfo ci) {
        if (!(player instanceof ServerPlayerEntity serverPlayer)) return;

        ItemStack stack = serverPlayer.getMainHandStack();
        if (stack.isEmpty()) return;
        if (ModEnchantmentHelper.getLevel(ModEnchantments.HUSH, stack) <= 0) return;

        ci.cancel();
    }
}
