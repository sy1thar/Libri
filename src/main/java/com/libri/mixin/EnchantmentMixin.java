package com.libri.mixin;

import com.libri.enchantment.EnchantmentSlots;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EquipmentSlot;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Enchantment.class)
public abstract class EnchantmentMixin implements EnchantmentSlots {

    @Shadow @Final
    private Enchantment.Properties properties;

    @Override
    public EquipmentSlot[] libri$getSlots() {
        return this.properties.slots();
    }

    @Inject(method = "canCombine(Lnet/minecraft/enchantment/Enchantment;)Z", at = @At("RETURN"), cancellable = true)
    private void onCanCombine(Enchantment other, CallbackInfoReturnable<Boolean> cir) {
        Enchantment self = (Enchantment) (Object) this;
        if (self == other) return;
        if (libri$slotsOverlap(self, other)) {
            cir.setReturnValue(true);
        }
    }

    @Unique
    private static boolean libri$slotsOverlap(Enchantment first, Enchantment second) {
        EquipmentSlot[] slotsA = ((EnchantmentSlots) first).libri$getSlots();
        EquipmentSlot[] slotsB = ((EnchantmentSlots) second).libri$getSlots();
        for (EquipmentSlot a : slotsA) {
            for (EquipmentSlot b : slotsB) {
                if (a == b) return true;
            }
        }
        return false;
    }
}
