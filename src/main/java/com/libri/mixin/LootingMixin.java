package com.libri.mixin;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.LivingEntity;
import net.minecraft.registry.entry.RegistryEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnchantmentHelper.class)
public class LootingMixin {

    @Inject(method = "getEquipmentLevel", at = @At("RETURN"), cancellable = true)
    private static void onGetEquipmentLevel(RegistryEntry<Enchantment> enchantment, LivingEntity entity, CallbackInfoReturnable<Integer> cir) {
        int level = cir.getReturnValueI();
        if (level <= 3) return;
        if (!enchantment.matchesKey(Enchantments.LOOTING)) return;
        cir.setReturnValue(level * 3);
    }
}
