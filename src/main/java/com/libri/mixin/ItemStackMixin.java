package com.libri.mixin;

import com.libri.enchantment.ModEnchantmentHelper;
import com.libri.enchantment.ModEnchantments;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

@Mixin(ItemStack.class)
public class ItemStackMixin {

    @Inject(method = "hurtAndBreak(ILnet/minecraft/server/level/ServerLevel;Lnet/minecraft/server/level/ServerPlayer;Ljava/util/function/Consumer;)V", at = @At("HEAD"), cancellable = true)
    private void onDamage(int amount, ServerLevel world, @Nullable ServerPlayer player, Consumer<ItemStack> breakCallback, CallbackInfo ci) {
        ItemStack stack = (ItemStack) (Object) this;
        if (ModEnchantmentHelper.getLevel(ModEnchantments.ETERNITY, stack) > 0) {
            ci.cancel();
        }
    }

    @Inject(method = "consume(ILnet/minecraft/world/entity/LivingEntity;)V", at = @At("HEAD"), cancellable = true)
    private void onConsume(int amount, @Nullable LivingEntity entity, CallbackInfo ci) {
        ItemStack stack = (ItemStack) (Object) this;
        // Tools and damageable items must NEVER be consumed as stackable items (e.g. by BlockTransformer or stacking mods).
        if (stack.isDamageableItem()) {
            ci.cancel();
            if (entity != null && amount > 0) {
                EquipmentSlot slot = EquipmentSlot.MAINHAND;
                try {
                    if (entity.getUsedItemHand() != null) {
                        slot = entity.getUsedItemHand().asEquipmentSlot();
                    }
                } catch (Exception ignored) {}
                stack.hurtAndBreak(amount, entity, slot);
            }
        }
    }
}
