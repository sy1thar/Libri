package com.libri;

import com.libri.config.ModConfig;
import com.libri.enchantment.ParalysisEffect;
import com.libri.event.ModEventHandlers;
import com.libri.event.ModLootTables;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.AbstractBlock;
import net.minecraft.item.Item;
import net.minecraft.item.BlockItem;
import com.libri.block.AdvancedAnvilBlock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class LibriMod implements ModInitializer {
    public static final String MOD_ID = "libri";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final Block ADVANCED_ANVIL = new AdvancedAnvilBlock(AbstractBlock.Settings.copy(Blocks.ANVIL));
    public static final Item ADVANCED_ANVIL_ITEM = new BlockItem(ADVANCED_ANVIL, new Item.Settings());

    public static StatusEffect PARALYSIS_EFFECT;

    @Override
    public void onInitialize() {
        PARALYSIS_EFFECT = new ParalysisEffect();
        Registry.register(Registries.STATUS_EFFECT, Identifier.of(MOD_ID, "paralysis"), PARALYSIS_EFFECT);

        ModConfig.load();
        ModEventHandlers.register();
        ModLootTables.register();
        ModCreativeTab.register();
        registerParalysisEvent();

        Registry.register(Registries.BLOCK, Identifier.of(MOD_ID, "advanced_anvil"), ADVANCED_ANVIL);
        Registry.register(Registries.ITEM, Identifier.of(MOD_ID, "advanced_anvil"), ADVANCED_ANVIL_ITEM);

        LOGGER.info("Libri mod initialized with 9 enchantments");
    }

    private void registerParalysisEvent() {
        ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
            Entity attacker = source.getAttacker();
            Entity directSource = source.getSource();

            if (attacker instanceof LivingEntity livingAttacker) {
                Optional<RegistryEntry.Reference<StatusEffect>> effectEntry = Registries.STATUS_EFFECT.getEntry(Registries.STATUS_EFFECT.getRawId(PARALYSIS_EFFECT));
                if (effectEntry.isPresent() && livingAttacker.hasStatusEffect(effectEntry.get())) {
                    return false;
                }
            }

            if (directSource instanceof PersistentProjectileEntity) {
                if (attacker instanceof LivingEntity shooter) {
                    var registryManager = entity.getWorld().getRegistryManager();
                    var enchantmentRegistry = registryManager.getWrapperOrThrow(RegistryKeys.ENCHANTMENT);
                    Optional<RegistryEntry.Reference<Enchantment>> paralysisEnch = enchantmentRegistry.getOptional(
                            RegistryKey.of(RegistryKeys.ENCHANTMENT, Identifier.of(MOD_ID, "paralysis")));

                    if (paralysisEnch.isPresent()) {
                        int mainHandLvl = EnchantmentHelper.getLevel(paralysisEnch.get(), shooter.getMainHandStack());
                        int offHandLvl = EnchantmentHelper.getLevel(paralysisEnch.get(), shooter.getOffHandStack());

                        if (mainHandLvl > 0 || offHandLvl > 0) {
                            Optional<RegistryEntry.Reference<StatusEffect>> effectEntry = Registries.STATUS_EFFECT.getEntry(Registries.STATUS_EFFECT.getRawId(PARALYSIS_EFFECT));
                            effectEntry.ifPresent(entry -> entity.addStatusEffect(new StatusEffectInstance(entry, 600, 0)));
                        }
                    }
                }
            }

            return true;
        });
    }
}
