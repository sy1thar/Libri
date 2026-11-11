package com.libri.config;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class ModConfigScreen {
    public static Screen create(Screen parent) {
        ModConfig config = ModConfig.get();

        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Text.translatable("config.libri.title"));

        ConfigCategory general = builder.getOrCreateCategory(Text.translatable("config.libri.category.general"));
        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        general.addEntry(entryBuilder
                .startBooleanToggle(Text.translatable("config.libri.remove_anvil_cap"), config.removeAnvilCap)
                .setDefaultValue(true)
                .setTooltip(Text.translatable("config.libri.remove_anvil_cap.tooltip"))
                .setSaveConsumer(val -> config.removeAnvilCap = val)
                .build());

        general.addEntry(entryBuilder
                .startEnumSelector(
                        Text.translatable("config.libri.enchantment_max_level_mode"),
                        ModConfig.EnchantmentMaxLevelMode.class,
                        config.enchantmentMaxLevelMode)
                .setDefaultValue(ModConfig.EnchantmentMaxLevelMode.CUSTOM)
                .setTooltip(Text.translatable("config.libri.enchantment_max_level_mode.tooltip"))
                .setSaveConsumer(val -> config.enchantmentMaxLevelMode = val)
                .build());

        general.addEntry(entryBuilder
                .startIntField(Text.translatable("config.libri.enchantment_custom_max_level"), config.enchantmentCustomMaxLevel)
                .setDefaultValue(10)
                .setMin(1)
                .setMax(255)
                .setTooltip(Text.translatable("config.libri.enchantment_custom_max_level.tooltip"))
                .setSaveConsumer(val -> config.enchantmentCustomMaxLevel = val)
                .build());

        builder.setSavingRunnable(ModConfig::save);

        return builder.build();
    }
}
