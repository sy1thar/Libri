package com.libri.client;

import com.libri.enchantment.ModEnchantmentHelper;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.enchantment.Enchantments;

public class LibriClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ItemTooltipCallback.EVENT.register((stack, context, type, lines) -> {
            var enchantments = stack.getEnchantments();
            if (enchantments == null) return;

            for (var entry : enchantments.entrySet()) {
                var key = entry.getKey().unwrapKey().orElse(null);
                if (key != null && key.location().getNamespace().equals("libri")) {
                    String descKey = key.location().toLanguageKey("enchantment") + ".desc";
                    Component desc = Component.translatable(descKey).copy().withStyle(ChatFormatting.DARK_GRAY);
                    lines.add(desc);
                }
            }

            int sharpnessLevel = ModEnchantmentHelper.getLevel(Enchantments.SHARPNESS, stack);
            if (sharpnessLevel <= 0) return;

            float baseDamage = 0f;
            var attrModifiers = stack.getOrDefault(net.minecraft.core.component.DataComponents.ATTRIBUTE_MODIFIERS, net.minecraft.world.item.component.ItemAttributeModifiers.EMPTY);
            for (var modifier : attrModifiers.modifiers()) {
                if (modifier.attribute() == Attributes.ATTACK_DAMAGE
                        && modifier.slot() == EquipmentSlotGroup.MAINHAND) {
                    baseDamage += (float) modifier.modifier().amount();
                }
            }

            float vanillaBonus = 1.0f + (sharpnessLevel - 1) * 0.5f;
            float totalBonus = sharpnessLevel > 5
                    ? vanillaBonus * (1.0f + (sharpnessLevel - 5) * 0.4f)
                    : vanillaBonus;

            float totalDamage = baseDamage + totalBonus;
            lines.add(Component.literal(String.format(" %.1f Attack Damage (enchanted)", totalDamage))
                    .withStyle(ChatFormatting.GRAY));
        });
    }
}
