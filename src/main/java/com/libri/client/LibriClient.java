package com.libri.client;

import com.libri.enchantment.ModEnchantmentHelper;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LibriClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ItemTooltipCallback.EVENT.register((stack, context, type, lines) -> {
            Set<String> added = new HashSet<>();
            addDescriptions(stack.getEnchantments(), lines, added);
            addDescriptions(stack.get(DataComponentTypes.STORED_ENCHANTMENTS), lines, added);

            int sharpnessLevel = ModEnchantmentHelper.getLevel(Enchantments.SHARPNESS, stack);
            if (sharpnessLevel <= 0) return;

            float baseDamage = 0f;
            var attrModifiers = stack.getOrDefault(DataComponentTypes.ATTRIBUTE_MODIFIERS, AttributeModifiersComponent.DEFAULT);
            for (var modifier : attrModifiers.modifiers()) {
                if (modifier.attribute().equals(EntityAttributes.GENERIC_ATTACK_DAMAGE)
                        && modifier.slot() == AttributeModifierSlot.MAINHAND) {
                    baseDamage += (float) modifier.modifier().value();
                }
            }

            float vanillaBonus = 1.0f + (sharpnessLevel - 1) * 0.5f;
            float totalBonus = sharpnessLevel > 5
                    ? vanillaBonus * (1.0f + (sharpnessLevel - 5) * 0.4f)
                    : vanillaBonus;

            float totalDamage = baseDamage + totalBonus;
            lines.add(Text.translatable("tooltip.libri.sharpness_damage", String.format("%.1f", totalDamage))
                    .formatted(Formatting.GRAY));
        });
    }

    private static void addDescriptions(ItemEnchantmentsComponent enchantments, List<Text> lines, Set<String> added) {
        if (enchantments == null || enchantments.isEmpty()) return;
        for (var entry : enchantments.getEnchantmentsMap()) {
            Identifier id = Registries.ENCHANTMENT.getId(entry.getKey().value());
            if (id != null && id.getNamespace().equals("libri")) {
                String descKey = id.toTranslationKey("enchantment") + ".desc";
                if (added.add(descKey)) {
                    lines.add(Text.translatable(descKey).copy().formatted(Formatting.DARK_GRAY));
                }
            }
        }
    }
}
