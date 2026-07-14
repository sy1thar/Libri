package com.libri.mixin;

import com.libri.enchantment.ModEnchantmentHelper;
import com.libri.enchantment.ModEnchantments;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class ParalysisMixin {

    @Inject(method = "damage", at = @At("HEAD"))
    private void onDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (((LivingEntity)(Object)this).getWorld().isClient) return;

        if (source.getSource() instanceof PersistentProjectileEntity arrow
                && source.getAttacker() instanceof ServerPlayerEntity player) {
            ItemStack weapon = player.getMainHandStack();
            int level = ModEnchantmentHelper.getLevel(ModEnchantments.PARALYSIS, weapon);
            if (level <= 0) return;

            float chance = switch (level) { case 1 -> 0.3f; case 2 -> 0.5f; case 3 -> 0.7f; default -> 0f; };
            if (((LivingEntity)(Object)this).getRandom().nextFloat() >= chance) return;

            int duration = switch (level) { case 1 -> 140; case 2 -> 240; case 3 -> 340; default -> 0; };
            ((LivingEntity)(Object)this).addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, duration, 4, false, false, true));
        }
    }
}
