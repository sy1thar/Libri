package com.libri.mixin;

import com.libri.enchantment.ModEnchantmentHelper;
import com.libri.enchantment.ModEnchantments;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(LivingEntity.class)
public class SacrificeMixin {

    @Inject(method = "dropLoot(Lnet/minecraft/entity/damage/DamageSource;Z)V", at = @At("HEAD"), cancellable = true)
    private void onDropLoot(DamageSource source, boolean causedByPlayer, CallbackInfo ci) {
        if (!causedByPlayer) return;
        if ((Object) this instanceof PlayerEntity) return;
        if (!(source.getAttacker() instanceof ServerPlayerEntity player)) return;

        ItemStack weapon = player.getMainHandStack();
        if (weapon.isEmpty()) return;
        var ench = EnchantmentHelper.getEnchantments(weapon);

        if (ModEnchantmentHelper.getLevel(ModEnchantments.SACRIFICE, ench) <= 0) return;

        LivingEntity victim = (LivingEntity) (Object) this;
        ServerWorld world = (ServerWorld) victim.getWorld();

        List<ItemStack> drops = new ArrayList<>();
        var lookup = world.getServer().getReloadableRegistries();
        var lootTable = lookup.getLootTable(victim.getLootTable());

        var ctx = new LootContextParameterSet.Builder(world)
                .add(LootContextParameters.THIS_ENTITY, victim)
                .add(LootContextParameters.ORIGIN, victim.getPos())
                .add(LootContextParameters.DAMAGE_SOURCE, source)
                .addOptional(LootContextParameters.KILLER_ENTITY, source.getAttacker())
                .addOptional(LootContextParameters.DIRECT_KILLER_ENTITY, source.getSource())
                .build(LootContextTypes.ENTITY);
        lootTable.generateLoot(ctx, victim.getLootTableSeed(), drops::add);

        int lootingLevel = ModEnchantmentHelper.getLevel(Enchantments.LOOTING, ench);
        float lootingMultiplier = 1.0f + (lootingLevel * 0.5f);

        int totalXp = 0;
        for (ItemStack stack : drops) {
            int baseXp = stack.isDamageable() ? 5 : 2;
            totalXp += (int) (stack.getCount() * baseXp * lootingMultiplier);
        }

        if (totalXp > 0) {
            world.spawnEntity(new ExperienceOrbEntity(world, victim.getX(), victim.getY(), victim.getZ(), totalXp));
        }

        ci.cancel();
    }
}
