package com.libri.mixin;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnchantmentHelper.class)
public class LootingMixin {

    @Inject(method = "getLooting(Lnet/minecraft/entity/LivingEntity;)I", at = @At("RETURN"), cancellable = true)
    private static void onGetLooting(LivingEntity entity, CallbackInfoReturnable<Integer> cir) {
        int level = cir.getReturnValueI();
        if (level <= 3) return;
        cir.setReturnValue(level * 3);
    }
}
