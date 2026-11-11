package com.libri.mixin;

import com.libri.enchantment.ModEnchantmentHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnchantmentHelper.class)
public class SharpnessMixin {

    @Inject(method = "modifyDamage", at = @At("RETURN"), cancellable = true)
    private static void onGetDamage(ServerLevel world, ItemStack stack, Entity target, DamageSource damageSource, float baseDamage, CallbackInfoReturnable<Float> cir) {
        float damage = cir.getReturnValueF();
        float bonus = damage - baseDamage;
        if (bonus <= 0) return;

        int sharpnessLevel = ModEnchantmentHelper.getLevel(Enchantments.SHARPNESS, stack);
        if (sharpnessLevel <= 5) return;

        float boost = 1.0f + (sharpnessLevel - 5) * 0.4f;
        cir.setReturnValue(baseDamage + bonus * boost);
    }
}
