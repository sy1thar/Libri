package com.libri.mixin;

import com.libri.enchantment.ModEnchantmentHelper;
import com.libri.enchantment.ModEnchantments;
import com.libri.helper.TelepathyHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
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

        ItemStack weapon = player.getMainHandItem();
        if (!weapon.isEmpty() && weapon.isEnchanted()) {
            var ench = EnchantmentHelper.getEnchantmentsForCrafting(weapon);
            if (ModEnchantmentHelper.getLevel(ModEnchantments.SACRIFICE, ench) > 0) {
                int baseXp = stack.isDamageableItem() ? 5 : 2;
                int lootingLevel = ModEnchantmentHelper.getLevel(Enchantments.LOOTING, ench);
                int xp = (int) (stack.getCount() * baseXp * (1.0f + lootingLevel * 0.5f));
                if (xp > 0) {
                    player.level().addFreshEntity(new ExperienceOrb(
                        player.level(), player.getX(), player.getY(), player.getZ(), xp));
                }
                ci.setReturnValue(null);
                return;
            }
        }

        player.getInventory().placeItemBackInInventory(stack);
        ci.setReturnValue(null);
    }
}
