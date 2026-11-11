package com.libri.mixin;

import com.libri.config.ModConfig;
import com.libri.enchantment.ModEnchantmentHelper;
import com.libri.enchantment.ModEnchantments;
import com.libri.helper.TelepathyHelper;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityDropMixin {

    @Inject(method = "drop(Lnet/minecraft/entity/damage/DamageSource;)V", at = @At("HEAD"))
    private void onDropStart(DamageSource source, CallbackInfo ci) {
        TelepathyHelper.TELEKINESIS_ATTACKER.set(null);
        LivingEntity entity = (LivingEntity) (Object) this;
        if (entity instanceof PlayerEntity) return;
        if (!(entity.getWorld() instanceof ServerWorld world)) return;
        if (source.getAttacker() instanceof ServerPlayerEntity player) {
            ItemStack weapon = player.getMainHandStack();
            if (!weapon.isEmpty() && weapon.hasEnchantments()) {
                var ench = EnchantmentHelper.getEnchantments(weapon);
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
                            entity.dropStack(headStack);
                        }
                    }

                    if (trophyLevel >= config.trophyEggMinLevel) {
                        if (world.random.nextFloat() < config.trophyEggChance) {
                            SpawnEggItem egg = SpawnEggItem.forEntity(entity.getType());
                            if (egg != null) {
                                entity.dropStack(new ItemStack(egg));
                            }
                        }
                    }
                }
            }
        }
    }

    @Inject(method = "drop(Lnet/minecraft/entity/damage/DamageSource;)V", at = @At("RETURN"))
    private void onDropEnd(DamageSource source, CallbackInfo ci) {
        TelepathyHelper.TELEKINESIS_ATTACKER.remove();
    }
}
