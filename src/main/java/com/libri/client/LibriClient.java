package com.libri.client;

import com.libri.enchantment.ModEnchantmentHelper;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class LibriClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ItemTooltipCallback.EVENT.register((stack, context, type, lines) -> {
            var enchantments = stack.getEnchantments();
            if (enchantments == null) return;

            for (var entry : enchantments.getEnchantmentEntries()) {
                var key = entry.getKey().getKey().orElse(null);
                if (key != null && key.getValue().getNamespace().equals("libri")) {
                    String descKey = key.getValue().toTranslationKey("enchantment") + ".desc";
                    Text desc = Text.translatable(descKey).copy().formatted(Formatting.DARK_GRAY);
                    lines.add(desc);
                }
            }

            int sharpnessLevel = ModEnchantmentHelper.getLevel(Enchantments.SHARPNESS, stack);
            if (sharpnessLevel <= 0) return;

            float baseDamage = 0f;
            var attrModifiers = stack.getOrDefault(net.minecraft.component.DataComponentTypes.ATTRIBUTE_MODIFIERS, net.minecraft.component.type.AttributeModifiersComponent.DEFAULT);
            for (var modifier : attrModifiers.modifiers()) {
                if (modifier.attribute() == EntityAttributes.GENERIC_ATTACK_DAMAGE
                        && modifier.slot() == AttributeModifierSlot.MAINHAND) {
                    baseDamage += (float) modifier.modifier().value();
                }
            }

            float vanillaBonus = 1.0f + (sharpnessLevel - 1) * 0.5f;
            float totalBonus = sharpnessLevel > 5
                    ? vanillaBonus * (1.0f + (sharpnessLevel - 5) * 0.4f)
                    : vanillaBonus;

            float totalDamage = baseDamage + totalBonus;
            lines.add(Text.literal(String.format(" %.1f Attack Damage (enchanted)", totalDamage))
                    .formatted(Formatting.GRAY));
        });
    }
}
