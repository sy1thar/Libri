package com.libri.mixin;

import com.libri.enchantment.ModEnchantmentHelper;
import com.libri.enchantment.ModEnchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemStack.class)
public class ItemStackMixin {

    @Inject(method = "damage(ILnet/minecraft/util/math/random/Random;Lnet/minecraft/server/network/ServerPlayerEntity;Ljava/lang/Runnable;)V", at = @At("HEAD"), cancellable = true)
    private void onDamage(int amount, Random random, @Nullable ServerPlayerEntity player, Runnable breakCallback, CallbackInfo ci) {
        ItemStack stack = (ItemStack) (Object) this;
        if (ModEnchantmentHelper.getLevel(ModEnchantments.ETERNITY, stack) > 0) {
            ci.cancel();
        }
    }
}
