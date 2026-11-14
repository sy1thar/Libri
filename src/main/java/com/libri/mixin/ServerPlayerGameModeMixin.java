package com.libri.mixin;

import com.libri.enchantment.ModEnchantmentHelper;
import com.libri.enchantment.ModEnchantments;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerGameMode.class)
public class ServerPlayerGameModeMixin {

    @Unique
    private static final ThreadLocal<ItemStack> libri$preUseStack = new ThreadLocal<>();

    @Inject(method = "useItemOn", at = @At("HEAD"))
    private void libri$beforeUseItemOn(ServerPlayer player, Level level, ItemStack itemStack, InteractionHand hand, BlockHitResult hitResult, CallbackInfoReturnable<InteractionResult> cir) {
        if (itemStack != null && !itemStack.isEmpty() && itemStack.isDamageableItem()) {
            libri$preUseStack.set(itemStack.copy());
        } else {
            libri$preUseStack.remove();
        }
    }

    @Inject(method = "useItemOn", at = @At("RETURN"))
    private void libri$afterUseItemOn(ServerPlayer player, Level level, ItemStack itemStack, InteractionHand hand, BlockHitResult hitResult, CallbackInfoReturnable<InteractionResult> cir) {
        ItemStack pre = libri$preUseStack.get();
        libri$preUseStack.remove();
        if (pre == null) return;

        ItemStack current = player.getItemInHand(hand);
        // If the tool became empty or was lost unexpectedly without legitimately breaking
        if (current.isEmpty()) {
            int maxDmg = pre.getMaxDamage();
            int curDmg = pre.getDamageValue();
            // If the item still had durability remaining, prevent unintended voiding
            if (curDmg + 1 < maxDmg) {
                ItemStack restored = pre.copy();
                if (ModEnchantmentHelper.getLevel(ModEnchantments.ETERNITY, pre) <= 0) {
                    restored.setDamageValue(curDmg + 1);
                }
                player.setItemInHand(hand, restored);
                player.containerMenu.broadcastChanges();
                player.inventoryMenu.broadcastChanges();
                player.containerMenu.sendAllDataToRemote();
                return;
            }
        }

        InteractionResult res = cir.getReturnValue();
        if (res != null && res.consumesAction()) {
            player.containerMenu.broadcastChanges();
            player.inventoryMenu.broadcastChanges();
        }
    }
}
