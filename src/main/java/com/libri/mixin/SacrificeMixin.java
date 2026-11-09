package com.libri.mixin;

import com.libri.enchantment.ModEnchantmentHelper;
import com.libri.enchantment.ModEnchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

@Mixin(LivingEntity.class)
public class SacrificeMixin {

    @Inject(method = "dropLoot(Lnet/minecraft/entity/damage/DamageSource;Z)V", at = @At("HEAD"), cancellable = true)
    private void onDropLoot(DamageSource source, boolean causedByPlayer, CallbackInfo ci) {
        if (!causedByPlayer) return;
        if ((Object) this instanceof net.minecraft.world.entity.player.Player) return;
        if (!(source.getEntity() instanceof ServerPlayer player)) return;

        ItemStack weapon = player.getMainHandItem();
        if (weapon.isEmpty()) return;
        var ench = EnchantmentHelper.getEnchantmentsForCrafting(weapon);

        if (ModEnchantmentHelper.getLevel(ModEnchantments.SACRIFICE, ench) <= 0) return;

        LivingEntity victim = (LivingEntity) (Object) this;
        ServerLevel world = (ServerLevel) victim.level();

        List<ItemStack> drops = new ArrayList<>();
        var lookup = world.getServer().reloadableRegistries();
        var lootTable = lookup.getLootTable(victim.getLootTable());

        var ctx = new LootParams.Builder(world)
                .withParameter(LootContextParams.THIS_ENTITY, victim)
                .withParameter(LootContextParams.ORIGIN, victim.position())
                .withParameter(LootContextParams.DAMAGE_SOURCE, source)
                .withOptionalParameter(LootContextParams.ATTACKING_ENTITY, source.getEntity())
                .withOptionalParameter(LootContextParams.DIRECT_ATTACKING_ENTITY, source.getDirectEntity())
                .create(LootContextParamSets.ENTITY);
        lootTable.getRandomItems(ctx, victim.getLootTableSeed(), drops::add);

        int lootingLevel = ModEnchantmentHelper.getLevel(Enchantments.LOOTING, ench);
        float lootingMultiplier = 1.0f + (lootingLevel * 0.5f);

        int totalXp = 0;
        for (ItemStack stack : drops) {
            int baseXp = stack.isDamageableItem() ? 5 : 2;
            totalXp += (int) (stack.getCount() * baseXp * lootingMultiplier);
        }

        if (totalXp > 0) {
            world.addFreshEntity(new ExperienceOrb(world, victim.getX(), victim.getY(), victim.getZ(), totalXp));
        }

        ci.cancel();
    }
}
