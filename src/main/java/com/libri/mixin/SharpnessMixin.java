package com.libri.mixin;

import com.libri.enchantment.ModEnchantmentHelper;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EntityType;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnchantmentHelper.class)
public class SharpnessMixin {

    @Inject(method = "getAttackDamage(Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/EntityType;)F", at = @At("RETURN"), cancellable = true)
    private static void onGetAttackDamage(ItemStack stack, EntityType<?> target, CallbackInfoReturnable<Float> cir) {
        int sharpnessLevel = ModEnchantmentHelper.getLevel(Enchantments.SHARPNESS, stack);
        if (sharpnessLevel <= 5) return;

        float vanillaSharpness = 1.0f + Math.max(0, sharpnessLevel - 1) * 0.5f;
        float boost = 1.0f + (sharpnessLevel - 5) * 0.4f;
        float extra = vanillaSharpness * (boost - 1.0f);
        cir.setReturnValue(cir.getReturnValueF() + extra);
    }
}
