package com.libri.mixin;

import com.libri.enchantment.ModEnchantmentHelper;
import com.libri.enchantment.ModEnchantments;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
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
        // Hush only muffles vibrations that sculk sensors pick up (footsteps);
        // ordinary sounds and other events stay visible to everyone.
        if (event != GameEvent.STEP) return;
        if (!(emitter.sourceEntity() instanceof ServerPlayerEntity player)) return;

        ItemStack leggings = player.getEquippedStack(EquipmentSlot.LEGS);
        if (leggings.isEmpty()) return;
        if (ModEnchantmentHelper.getLevel(ModEnchantments.HUSH, leggings) <= 0) return;

        ci.cancel();
    }
}
