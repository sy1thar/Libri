package com.libri.mixin;

import com.libri.enchantment.ModEnchantmentHelper;
import com.libri.enchantment.ModEnchantments;
import com.libri.helper.TelepathyHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import com.libri.config.ModConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityDropMixin {

    @Inject(method = "drop(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/damage/DamageSource;)V", at = @At("HEAD"))
    private void onDropStart(ServerLevel world, DamageSource source, CallbackInfo ci) {
        TelepathyHelper.TELEKINESIS_ATTACKER.set(null);
        LivingEntity entity = (LivingEntity) (Object) this;
        if (entity instanceof net.minecraft.world.entity.player.Player) return;
        if (source.getEntity() instanceof ServerPlayer player) {
            ItemStack weapon = player.getMainHandItem();
            if (!weapon.isEmpty() && weapon.isEnchanted()) {
                var ench = EnchantmentHelper.getEnchantmentsForCrafting(weapon);
                if (ModEnchantmentHelper.getLevel(ModEnchantments.TELEKINESIS, ench) > 0
                        || ModEnchantmentHelper.getLevel(ModEnchantments.SACRIFICE, ench) > 0) {
                    TelepathyHelper.TELEKINESIS_ATTACKER.set(player);
                }

                int trophyLevel = ModEnchantmentHelper.getLevel(ModEnchantments.TROPHY, ench);
                if (trophyLevel > 0) {
                    ModConfig config = ModConfig.get();

                    float headChance = config.trophyHeadBaseChance + (trophyLevel * config.trophyHeadLevelMultiplier);
                    if (world.random.nextFloat() < headChance) {
                        ItemStack headStack = ItemStack.EMPTY;
                        if (entity.getType() == EntityType.ZOMBIE) headStack = new ItemStack(Items.ZOMBIE_HEAD);
                        else if (entity.getType() == EntityType.SKELETON) headStack = new ItemStack(Items.SKELETON_SKULL);
                        else if (entity.getType() == EntityType.CREEPER) headStack = new ItemStack(Items.CREEPER_HEAD);
                        else if (entity.getType() == EntityType.WITHER_SKELETON) headStack = new ItemStack(Items.WITHER_SKELETON_SKULL);
                        else if (entity.getType() == EntityType.PIGLIN) headStack = new ItemStack(Items.PIGLIN_HEAD);
                        else if (entity.getType() == EntityType.PLAYER) headStack = new ItemStack(Items.PLAYER_HEAD);
                        
                        if (!headStack.isEmpty()) {
                            entity.spawnAtLocation(headStack);
                        }
                    }

                    if (trophyLevel >= config.trophyEggMinLevel) {
                        if (world.random.nextFloat() < config.trophyEggChance) {
                            SpawnEggItem egg = SpawnEggItem.byId(entity.getType());
                            if (egg != null) {
                                entity.spawnAtLocation(new ItemStack(egg));
                            }
                        }
                    }
                }
            }
        }
    }

    @Inject(method = "drop(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/damage/DamageSource;)V", at = @At("RETURN"))
    private void onDropEnd(ServerLevel world, DamageSource source, CallbackInfo ci) {
        TelepathyHelper.TELEKINESIS_ATTACKER.remove();
    }
}
