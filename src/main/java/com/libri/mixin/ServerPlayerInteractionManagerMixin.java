package com.libri.mixin;

import com.libri.enchantment.ModEnchantmentHelper;
import com.libri.enchantment.ModEnchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerInteractionManager.class)
public class ServerPlayerInteractionManagerMixin {

    @Unique
    private static final ThreadLocal<ItemStack> libri$preUseStack = new ThreadLocal<>();

    @Inject(method = "interactBlock", at = @At("HEAD"))
    private void libri$beforeInteractBlock(ServerPlayerEntity player, World world, ItemStack itemStack, Hand hand, BlockHitResult hitResult, CallbackInfoReturnable<ActionResult> cir) {
        if (itemStack != null && !itemStack.isEmpty() && itemStack.isDamageable()) {
            libri$preUseStack.set(itemStack.copy());
        } else {
            libri$preUseStack.remove();
        }
    }

    @Inject(method = "interactBlock", at = @At("RETURN"))
    private void libri$afterInteractBlock(ServerPlayerEntity player, World world, ItemStack itemStack, Hand hand, BlockHitResult hitResult, CallbackInfoReturnable<ActionResult> cir) {
        ItemStack pre = libri$preUseStack.get();
        libri$preUseStack.remove();
        if (pre == null) return;

        ItemStack current = player.getStackInHand(hand);
        if (current.isEmpty()) {
            int maxDmg = pre.getMaxDamage();
            int curDmg = pre.getDamage();
            if (curDmg + 1 < maxDmg) {
                ItemStack restored = pre.copy();
                if (ModEnchantmentHelper.getLevel(ModEnchantments.ETERNITY, pre) <= 0) {
                    restored.setDamage(curDmg + 1);
                }
                player.setStackInHand(hand, restored);
                player.currentScreenHandler.sendContentUpdates();
                player.playerScreenHandler.sendContentUpdates();
            }
        }
    }
}
