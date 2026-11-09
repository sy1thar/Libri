package com.libri.mixin;

import com.libri.enchantment.ModEnchantmentHelper;
import com.libri.enchantment.ModEnchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

@Mixin(Inventory.class)
public class PlayerDropMixin {
    @Unique
    private final Map<Integer, ItemStack> libri$soulboundItems = new HashMap<>();

    @Inject(method = "dropAll", at = @At("HEAD"))
    private void extractSoulbound(CallbackInfo ci) {
        libri$soulboundItems.clear();
        Inventory inventory = (Inventory) (Object) this;
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (!stack.isEmpty() && ModEnchantmentHelper.getLevel(ModEnchantments.SOULBOUND, stack) > 0) {
                libri$soulboundItems.put(i, stack.copy());
                inventory.setItem(i, ItemStack.EMPTY);
            }
        }
    }

    @Inject(method = "dropAll", at = @At("RETURN"))
    private void restoreSoulbound(CallbackInfo ci) {
        Inventory inventory = (Inventory) (Object) this;
        for (Map.Entry<Integer, ItemStack> entry : libri$soulboundItems.entrySet()) {
            inventory.setItem(entry.getKey(), entry.getValue());
        }
        libri$soulboundItems.clear();
    }
}
