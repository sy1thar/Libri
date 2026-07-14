package com.libri;

import com.libri.config.ModConfig;
import com.libri.event.ModEventHandlers;
import com.libri.event.ModLootTables;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LibriMod implements ModInitializer {
    public static final String MOD_ID = "libri";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        ModConfig.load();
        ModEventHandlers.register();
        ModLootTables.register();
        LOGGER.info("Libri mod initialized with 7 enchantments");
    }
}
