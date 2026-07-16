package com.libri.enchantment;

import com.libri.LibriMod;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.util.Identifier;

public class ParalysisEffect extends StatusEffect {
    public ParalysisEffect() {
        super(StatusEffectCategory.HARMFUL, 0x800080);

        this.addAttributeModifier(
                EntityAttributes.GENERIC_MOVEMENT_SPEED,
                Identifier.of(LibriMod.MOD_ID, "paralysis_slow"),
                -1.0,
                EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
        );
    }
}
