package com.libri.mixin;

import com.libri.enchantment.ModEnchantmentHelper;
import com.libri.enchantment.ModEnchantments;
import com.libri.helper.TelepathyHelper;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityDropMixin {

    @Inject(method = "drop(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/damage/DamageSource;)V", at = @At("HEAD"))
    private void onDropStart(ServerWorld world, DamageSource source, CallbackInfo ci) {
        TelepathyHelper.TELEKINESIS_ATTACKER.set(null);
        if (source.getAttacker() instanceof ServerPlayerEntity player) {
            ItemStack weapon = player.getMainHandStack();
            if (!weapon.isEmpty() && weapon.hasEnchantments()) {
                var ench = EnchantmentHelper.getEnchantments(weapon);
                if (ModEnchantmentHelper.getLevel(ModEnchantments.TELEKINESIS, ench) > 0
                        || ModEnchantmentHelper.getLevel(ModEnchantments.SACRIFICE, ench) > 0) {
                    TelepathyHelper.TELEKINESIS_ATTACKER.set(player);
                }
            }
        }
    }

    @Inject(method = "drop(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/damage/DamageSource;)V", at = @At("RETURN"))
    private void onDropEnd(ServerWorld world, DamageSource source, CallbackInfo ci) {
        TelepathyHelper.TELEKINESIS_ATTACKER.remove();
    }
}
