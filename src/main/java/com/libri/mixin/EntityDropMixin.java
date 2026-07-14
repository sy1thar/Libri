package com.libri.mixin;

import com.libri.enchantment.ModEnchantmentHelper;
import com.libri.enchantment.ModEnchantments;
import com.libri.helper.TelepathyHelper;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class EntityDropMixin {

    @Inject(method = "dropStack(Lnet/minecraft/item/ItemStack;F)Lnet/minecraft/entity/ItemEntity;", at = @At("HEAD"), cancellable = true)
    private void onDropStack(ItemStack stack, float yOffset, CallbackInfoReturnable<Entity> ci) {
        var player = TelepathyHelper.TELEKINESIS_ATTACKER.get();
        if (player == null) return;
        if (stack.isEmpty()) return;

        ItemStack weapon = player.getMainHandStack();
        if (!weapon.isEmpty() && weapon.hasEnchantments()) {
            var ench = EnchantmentHelper.getEnchantments(weapon);
            if (ModEnchantmentHelper.getLevel(ModEnchantments.SACRIFICE, ench) > 0) {
                int baseXp = stack.isDamageable() ? 5 : 2;
                int lootingLevel = ModEnchantmentHelper.getLevel(Enchantments.LOOTING, ench);
                int xp = (int) (stack.getCount() * baseXp * (1.0f + lootingLevel * 0.5f));
                if (xp > 0) {
                    player.getWorld().spawnEntity(new ExperienceOrbEntity(
                        player.getWorld(), player.getX(), player.getY(), player.getZ(), xp));
                }
                ci.setReturnValue(null);
                return;
            }
        }

        player.getInventory().offerOrDrop(stack);
        ci.setReturnValue(null);
    }
}
