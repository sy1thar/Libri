package com.libri.mixin;

import com.libri.enchantment.ModEnchantmentHelper;
import com.libri.enchantment.ModEnchantments;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerLevel.class)
public class HushMixin {

    @Inject(method = "emitGameEvent(Lnet/minecraft/registry/entry/RegistryEntry;Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/world/event/GameEvent$Emitter;)V",
            at = @At("HEAD"), cancellable = true)
    private void onEmitGameEvent(Holder<GameEvent> event, Vec3 pos, GameEvent.Context emitter, CallbackInfo ci) {
        // Hush only muffles vibrations that sculk sensors pick up (footsteps);
        // ordinary sounds and other events stay visible to everyone.
        if (event != GameEvent.STEP) return;
        if (!(emitter.sourceEntity() instanceof ServerPlayer player)) return;

        ItemStack leggings = player.getItemBySlot(EquipmentSlot.LEGS);
        if (leggings.isEmpty()) return;
        if (ModEnchantmentHelper.getLevel(ModEnchantments.HUSH, leggings) <= 0) return;

        ci.cancel();
    }
}
