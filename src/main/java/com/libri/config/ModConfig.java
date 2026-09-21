package com.libri.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

public class ModConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("libri.json");
    private static ModConfig INSTANCE = new ModConfig();

    public boolean removeAnvilCap = true;
    public EnchantmentMaxLevelMode enchantmentMaxLevelMode = EnchantmentMaxLevelMode.CUSTOM;
    public int enchantmentCustomMaxLevel = 10;
    public List<String> siftingJunkBlocks = Arrays.asList(
            "minecraft:cobblestone",
            "minecraft:cobbled_deepslate",
            "minecraft:deepslate",
            "minecraft:diorite",
            "minecraft:andesite",
            "minecraft:granite",
            "minecraft:tuff",
            "minecraft:gravel"
    );

    public enum EnchantmentMaxLevelMode {
        VANILLA,
        CUSTOM
    }

    public static ModConfig get() {
        return INSTANCE;
    }

    public static void load() {
        try {
            if (Files.exists(PATH)) {
                String json = Files.readString(PATH);
                INSTANCE = GSON.fromJson(json, ModConfig.class);
            }
        } catch (Exception e) {
            INSTANCE = new ModConfig();
        }
    }

    public static void save() {
        try {
            Files.createDirectories(PATH.getParent());
            Files.writeString(PATH, GSON.toJson(INSTANCE));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
