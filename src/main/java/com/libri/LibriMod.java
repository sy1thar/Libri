package com.libri;

import com.libri.config.ModConfig;
import com.libri.enchantment.ParalysisEffect;
import com.libri.event.ModEventHandlers;
import com.libri.event.ModLootTables;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import com.libri.block.AdvancedAnvilBlock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class LibriMod implements ModInitializer {
    public static final String MOD_ID = "libri";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final ResourceKey<Block> ADVANCED_ANVIL_BLOCK_KEY = ResourceKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(MOD_ID, "advanced_anvil"));
    public static final ResourceKey<Item> ADVANCED_ANVIL_ITEM_KEY = ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MOD_ID, "advanced_anvil"));

    public static final Block ADVANCED_ANVIL = new AdvancedAnvilBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.ANVIL).setId(ADVANCED_ANVIL_BLOCK_KEY));
    public static final Item ADVANCED_ANVIL_ITEM = new BlockItem(ADVANCED_ANVIL, new Item.Properties().setId(ADVANCED_ANVIL_ITEM_KEY).useBlockDescriptionPrefix());

    public static MobEffect PARALYSIS_EFFECT;
    public static Holder<MobEffect> PARALYSIS_EFFECT_HOLDER;

    @Override
    public void onInitialize() {
        PARALYSIS_EFFECT = new ParalysisEffect();
        PARALYSIS_EFFECT_HOLDER = Registry.registerForHolder(BuiltInRegistries.MOB_EFFECT, Identifier.fromNamespaceAndPath(MOD_ID, "paralysis"), PARALYSIS_EFFECT);

        ModConfig.load();
        ModEventHandlers.register();
        ModLootTables.register();
        ModCreativeTab.register();
        registerParalysisEvent();

        Registry.register(BuiltInRegistries.BLOCK, ADVANCED_ANVIL_BLOCK_KEY, ADVANCED_ANVIL);
        Registry.register(BuiltInRegistries.ITEM, ADVANCED_ANVIL_ITEM_KEY, ADVANCED_ANVIL_ITEM);

        LOGGER.info("Libri mod initialized with 14 enchantments");
    }

    private void registerParalysisEvent() {
        ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
            Entity attacker = source.getEntity();
            Entity directSource = source.getDirectEntity();

            if (attacker instanceof LivingEntity livingAttacker) {
                if (PARALYSIS_EFFECT_HOLDER != null && livingAttacker.hasEffect(PARALYSIS_EFFECT_HOLDER)) {
                    return false;
                }
            }

            if (directSource instanceof AbstractArrow) {
                if (attacker instanceof LivingEntity shooter) {
                    var registryManager = entity.level().registryAccess();
                    var enchantmentRegistry = registryManager.lookupOrThrow(Registries.ENCHANTMENT);
                    Optional<Holder.Reference<Enchantment>> paralysisEnch = enchantmentRegistry.get(
                            ResourceKey.create(Registries.ENCHANTMENT, Identifier.fromNamespaceAndPath(MOD_ID, "paralysis")));

                    if (paralysisEnch.isPresent()) {
                        int mainHandLvl = EnchantmentHelper.getItemEnchantmentLevel(paralysisEnch.get(), shooter.getMainHandItem());
                        int offHandLvl = EnchantmentHelper.getItemEnchantmentLevel(paralysisEnch.get(), shooter.getOffhandItem());

                        if (mainHandLvl > 0 || offHandLvl > 0) {
                            if (PARALYSIS_EFFECT_HOLDER != null) {
                                entity.addEffect(new MobEffectInstance(PARALYSIS_EFFECT_HOLDER, 600, 0));
                            }
                        }
                    }
                }
            }

            return true;
        });
    }
}
