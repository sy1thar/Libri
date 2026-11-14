package com.libri.mixin;

import com.libri.enchantment.ModEnchantmentHelper;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemGroups.class)
public class ItemGroupsMixin {
    @Inject(method = "updateEntries", at = @At("RETURN"))
    private static void onUpdateEntries(CallbackInfo ci) {
        ItemGroups.getGroups().forEach(group -> {
            group.getDisplayStacks().removeIf(stack ->
                stack.isOf(Items.ENCHANTED_BOOK) && ModEnchantmentHelper.hasModEnchantment(stack)
            );
            group.getSearchTabStacks().removeIf(stack ->
                stack.isOf(Items.ENCHANTED_BOOK) && ModEnchantmentHelper.hasModEnchantment(stack)
            );
        });
    }
}
