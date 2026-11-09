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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
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

    public static final Block ADVANCED_ANVIL = new AdvancedAnvilBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.ANVIL));
    public static final Item ADVANCED_ANVIL_ITEM = new BlockItem(ADVANCED_ANVIL, new Item.Properties());

    public static MobEffect PARALYSIS_EFFECT;

    @Override
    public void onInitialize() {
        PARALYSIS_EFFECT = new ParalysisEffect();
        Registry.register(BuiltInRegistries.MOB_EFFECT, ResourceLocation.fromNamespaceAndPath(MOD_ID, "paralysis"), PARALYSIS_EFFECT);

        ModConfig.load();
        ModEventHandlers.register();
        ModLootTables.register();
        ModCreativeTab.register();
        registerParalysisEvent();

        Registry.register(BuiltInRegistries.BLOCK, ResourceLocation.fromNamespaceAndPath(MOD_ID, "advanced_anvil"), ADVANCED_ANVIL);
        Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath(MOD_ID, "advanced_anvil"), ADVANCED_ANVIL_ITEM);

        LOGGER.info("Libri mod initialized with 9 enchantments");
    }

    private void registerParalysisEvent() {
        ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
            Entity attacker = source.getEntity();
            Entity directSource = source.getDirectEntity();

            if (attacker instanceof LivingEntity livingAttacker) {
                Optional<Holder.Reference<MobEffect>> effectEntry = BuiltInRegistries.MOB_EFFECT.getHolder(BuiltInRegistries.MOB_EFFECT.getId(PARALYSIS_EFFECT));
                if (effectEntry.isPresent() && livingAttacker.hasEffect(effectEntry.get())) {
                    return false;
                }
            }

            if (directSource instanceof AbstractArrow) {
                if (attacker instanceof LivingEntity shooter) {
                    var registryManager = entity.level().registryAccess();
                    var enchantmentRegistry = registryManager.lookupOrThrow(Registries.ENCHANTMENT);
                    Optional<Holder.Reference<Enchantment>> paralysisEnch = enchantmentRegistry.get(
                            ResourceKey.create(Registries.ENCHANTMENT, ResourceLocation.fromNamespaceAndPath(MOD_ID, "paralysis")));

                    if (paralysisEnch.isPresent()) {
                        int mainHandLvl = EnchantmentHelper.getItemEnchantmentLevel(paralysisEnch.get(), shooter.getMainHandItem());
                        int offHandLvl = EnchantmentHelper.getItemEnchantmentLevel(paralysisEnch.get(), shooter.getOffhandItem());

                        if (mainHandLvl > 0 || offHandLvl > 0) {
                            Optional<Holder.Reference<MobEffect>> effectEntry = BuiltInRegistries.MOB_EFFECT.getHolder(BuiltInRegistries.MOB_EFFECT.getId(PARALYSIS_EFFECT));
                            effectEntry.ifPresent(entry -> entity.addEffect(new MobEffectInstance(entry, 600, 0)));
                        }
                    }
                }
            }

            return true;
        });
    }
}
