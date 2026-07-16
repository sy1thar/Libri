package com.libri.mixin;

import com.libri.enchantment.ModEnchantmentHelper;
import com.libri.enchantment.ModEnchantments;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.EntityHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PersistentProjectileEntity.class)
public class ParalysisArrowMixin {

    @Inject(method = "onEntityHit", at = @At("TAIL"))
    private void onParalysisHit(EntityHitResult entityHitResult, CallbackInfo ci) {
        PersistentProjectileEntity self = (PersistentProjectileEntity) (Object) this;
        if (!(self.getOwner() instanceof LivingEntity shooter)) return;

        ItemStack bowStack = ItemStack.EMPTY;
        if (shooter.getMainHandStack().getItem() instanceof BowItem) {
            bowStack = shooter.getMainHandStack();
        } else if (shooter.getOffHandStack().getItem() instanceof BowItem) {
            bowStack = shooter.getOffHandStack();
        }

        if (ModEnchantmentHelper.getLevel(ModEnchantments.PARALYSIS, bowStack) == 0) return;

        var hitEntity = entityHitResult.getEntity();
        if (hitEntity instanceof LivingEntity target) {
            target.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 300, 127, false, true));
            target.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 300, 127, false, true));
        }
    }
}
