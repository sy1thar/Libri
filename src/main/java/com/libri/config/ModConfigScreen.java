package com.libri.config;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ModConfigScreen {
    public static Screen create(Screen parent) {
        ModConfig config = ModConfig.get();

        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Component.translatable("config.libri.title"));

        ConfigCategory general = builder.getOrCreateCategory(Component.translatable("config.libri.category.general"));
        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        general.addEntry(entryBuilder
                .startBooleanToggle(Component.translatable("config.libri.remove_anvil_cap"), config.removeAnvilCap)
                .setDefaultValue(true)
                .setTooltip(Component.translatable("config.libri.remove_anvil_cap.tooltip"))
                .setSaveConsumer(val -> config.removeAnvilCap = val)
                .build());

        general.addEntry(entryBuilder
                .startEnumSelector(
                        Component.translatable("config.libri.enchantment_max_level_mode"),
                        ModConfig.EnchantmentMaxLevelMode.class,
                        config.enchantmentMaxLevelMode)
                .setDefaultValue(ModConfig.EnchantmentMaxLevelMode.CUSTOM)
                .setTooltip(Component.translatable("config.libri.enchantment_max_level_mode.tooltip"))
                .setSaveConsumer(val -> config.enchantmentMaxLevelMode = val)
                .build());

        general.addEntry(entryBuilder
                .startIntField(Component.translatable("config.libri.enchantment_custom_max_level"), config.enchantmentCustomMaxLevel)
                .setDefaultValue(10)
                .setMin(1)
                .setMax(255)
                .setTooltip(Component.translatable("config.libri.enchantment_custom_max_level.tooltip"))
                .setSaveConsumer(val -> config.enchantmentCustomMaxLevel = val)
                .build());

        builder.setSavingRunnable(ModConfig::save);

        return builder.build();
    }
}
