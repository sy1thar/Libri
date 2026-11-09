package com.libri.mixin;

import com.libri.config.ModConfig;
import net.minecraft.world.item.enchantment.Enchantment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Enchantment.class)
public class EnchantmentLevelMixin {

    @Inject(method = "getMaxLevel", at = @At("RETURN"), cancellable = true)
    private void onGetMaxLevel(CallbackInfoReturnable<Integer> cir) {
        ModConfig config = ModConfig.get();
        if (config.enchantmentMaxLevelMode == ModConfig.EnchantmentMaxLevelMode.VANILLA) return;
        if (cir.getReturnValueI() >= 2) {
            cir.setReturnValue(config.enchantmentCustomMaxLevel);
        }
    }

    @Inject(method = "getMinPower", at = @At("RETURN"), cancellable = true)
    private void onGetMinPower(int level, CallbackInfoReturnable<Integer> cir) {
        ModConfig config = ModConfig.get();
        if (config.enchantmentMaxLevelMode == ModConfig.EnchantmentMaxLevelMode.VANILLA) return;
        if (config.enchantmentCustomMaxLevel <= 1) return;

        int maxLvl = config.enchantmentCustomMaxLevel;
        int maxPower = 30;
        int adjusted = 1 + (level - 1) * (maxPower - 1) / Math.max(1, maxLvl - 1);
        cir.setReturnValue(Math.min(cir.getReturnValue(), adjusted));
    }

    @Inject(method = "getMaxPower", at = @At("RETURN"), cancellable = true)
    private void onGetMaxPower(int level, CallbackInfoReturnable<Integer> cir) {
        ModConfig config = ModConfig.get();
        if (config.enchantmentMaxLevelMode == ModConfig.EnchantmentMaxLevelMode.VANILLA) return;

        cir.setReturnValue(Math.max(cir.getReturnValue(), 50));
    }
}
