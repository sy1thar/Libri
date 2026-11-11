package com.libri.mixin;

import net.minecraft.core.Holder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.enchantment.Enchantment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Enchantment.class)
public class EnchantmentMixin {

    @Inject(method = "areCompatible(Lnet/minecraft/core/Holder;Lnet/minecraft/core/Holder;)Z", at = @At("RETURN"), cancellable = true)
    private static void onCanBeCombined(Holder<Enchantment> first, Holder<Enchantment> second, CallbackInfoReturnable<Boolean> cir) {
        // Vanilla forbids combinations inside an exclusive set (e.g. Protection vs Fire Protection).
        // We override that: enchantments are compatible whenever they apply to the same equipment
        // slots (armor with armor, weapon with weapon, tools with tools), regardless of exclusivity.
        if (libri$slotsOverlap(first.value(), second.value())) {
            cir.setReturnValue(true);
        }
    }

    @Unique
    private static boolean libri$slotsOverlap(Enchantment first, Enchantment second) {
        for (EquipmentSlot equipmentSlot : EquipmentSlot.values()) {
            if (libri$matchesSlot(first, equipmentSlot) && libri$matchesSlot(second, equipmentSlot)) {
                return true;
            }
        }
        return false;
    }

    @Unique
    private static boolean libri$matchesSlot(Enchantment enchantment, EquipmentSlot slot) {
        for (EquipmentSlotGroup modifierSlot : enchantment.definition().slots()) {
            if (modifierSlot.test(slot)) {
                return true;
            }
        }
        return false;
    }
}
