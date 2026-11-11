package com.libri;

import com.libri.block.AdvancedAnvilBlock;
import com.libri.config.ModConfig;
import com.libri.enchantment.ModEnchantments;
import com.libri.enchantment.ParalysisEffect;
import com.libri.event.ModEventHandlers;
import com.libri.event.ModLootTables;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LibriMod implements ModInitializer {
    public static final String MOD_ID = "libri";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final Block ADVANCED_ANVIL = new AdvancedAnvilBlock(AbstractBlock.Settings.copy(Blocks.ANVIL));
    public static final Item ADVANCED_ANVIL_ITEM = new BlockItem(ADVANCED_ANVIL, new Item.Settings());

    public static RegistryEntry<StatusEffect> PARALYSIS_EFFECT_ENTRY;

    @Override
    public void onInitialize() {
        PARALYSIS_EFFECT_ENTRY = Registry.registerReference(Registries.STATUS_EFFECT, new Identifier(MOD_ID, "paralysis"), new ParalysisEffect());

        ModEnchantments.register();
        ModConfig.load();
        ModEventHandlers.register();
        ModLootTables.register();
        ModCreativeTab.register();
        registerParalysisEvent();

        Registry.register(Registries.BLOCK, new Identifier(MOD_ID, "advanced_anvil"), ADVANCED_ANVIL);
        Registry.register(Registries.ITEM, new Identifier(MOD_ID, "advanced_anvil"), ADVANCED_ANVIL_ITEM);

        LOGGER.info("Libri mod initialized with 14 enchantments");
    }

    private void registerParalysisEvent() {
        ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
            Entity attacker = source.getAttacker();
            Entity directSource = source.getSource();

            if (attacker instanceof LivingEntity livingAttacker && PARALYSIS_EFFECT_ENTRY != null) {
                if (livingAttacker.hasStatusEffect(PARALYSIS_EFFECT_ENTRY)) {
                    return false;
                }
            }

            if (directSource instanceof PersistentProjectileEntity && attacker instanceof LivingEntity shooter && PARALYSIS_EFFECT_ENTRY != null) {
                int mainHandLvl = EnchantmentHelper.getLevel(ModEnchantments.PARALYSIS, shooter.getMainHandStack());
                int offHandLvl = EnchantmentHelper.getLevel(ModEnchantments.PARALYSIS, shooter.getOffHandStack());

                if (mainHandLvl > 0 || offHandLvl > 0) {
                    entity.addStatusEffect(new StatusEffectInstance(PARALYSIS_EFFECT_ENTRY, 600, 0));
                }
            }

            return true;
        });
    }
}
