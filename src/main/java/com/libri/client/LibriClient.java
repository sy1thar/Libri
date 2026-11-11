package com.libri.client;

import com.libri.enchantment.ModEnchantmentHelper;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LibriClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ItemTooltipCallback.EVENT.register((stack, context, type, lines) -> {
            Set<String> added = new HashSet<>();
            addDescriptions(stack.getEnchantments(), lines, added);
            addDescriptions(stack.get(DataComponents.STORED_ENCHANTMENTS), lines, added);

            int sharpnessLevel = ModEnchantmentHelper.getLevel(Enchantments.SHARPNESS, stack);
            if (sharpnessLevel <= 0) return;

            float baseDamage = 0f;
            var attrModifiers = stack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
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
            lines.add(Component.translatable("tooltip.libri.sharpness_damage", String.format("%.1f", totalDamage))
                    .withStyle(ChatFormatting.GRAY));
        });
    }

    private static void addDescriptions(ItemEnchantments enchantments, List<Component> lines, Set<String> added) {
        if (enchantments == null || enchantments.isEmpty()) return;
        for (var entry : enchantments.entrySet()) {
            var key = entry.getKey().unwrapKey().orElse(null);
            if (key != null && key.identifier().getNamespace().equals("libri")) {
                String descKey = key.identifier().toLanguageKey("enchantment") + ".desc";
                if (added.add(descKey)) {
                    lines.add(Component.translatable(descKey).withStyle(ChatFormatting.DARK_GRAY));
                }
            }
        }
    }
}
