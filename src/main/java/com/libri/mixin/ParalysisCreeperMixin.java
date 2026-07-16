package com.libri.mixin;

import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.CreeperEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CreeperEntity.class)
public class ParalysisCreeperMixin {

    @Inject(method = "getSwelling", at = @At("HEAD"), cancellable = true)
    private void preventParalyzedCreeperExplosion(boolean ticksSinceIgnited, CallbackInfoReturnable<Integer> cir) {
        CreeperEntity self = (CreeperEntity) (Object) this;
        StatusEffectInstance slowness = self.getStatusEffect(StatusEffects.SLOWNESS);
        if (slowness != null && slowness.getAmplifier() >= 127) {
            cir.setReturnValue(0);
        }
    }
}
