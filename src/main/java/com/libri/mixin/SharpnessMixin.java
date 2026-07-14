package com.libri.mixin;

import com.libri.enchantment.ModEnchantmentHelper;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnchantmentHelper.class)
public class SharpnessMixin {

    @Inject(method = "getDamage", at = @At("RETURN"), cancellable = true)
    private static void onGetDamage(ServerWorld world, ItemStack stack, Entity target, DamageSource damageSource, float baseDamage, CallbackInfoReturnable<Float> cir) {
        float damage = cir.getReturnValueF();
        float bonus = damage - baseDamage;
        if (bonus <= 0) return;

        int sharpnessLevel = ModEnchantmentHelper.getLevel(Enchantments.SHARPNESS, stack);
        if (sharpnessLevel <= 5) return;

        float boost = 1.0f + (sharpnessLevel - 5) * 0.4f;
        cir.setReturnValue(baseDamage + bonus * boost);
    }
}
