package com.libri.enchantment;

import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;

public class ParalysisEffect extends StatusEffect {
    public ParalysisEffect() {
        super(StatusEffectCategory.HARMFUL, 0x800080);

        this.addAttributeModifier(
                EntityAttributes.GENERIC_MOVEMENT_SPEED,
                "9f6b4b2a-1d3c-4e5f-8a7b-6c5d4e3f2a10",
                -1.0,
                EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
        );
    }
}
