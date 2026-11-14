package com.libri.mixin;

import com.libri.enchantment.ModEnchantmentHelper;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.numeric.CustomModelDataProperty;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CustomModelDataProperty.class)
public class CustomModelDataPropertyMixin {

    @Inject(method = "get(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/client/multiplayer/ClientLevel;Lnet/minecraft/world/entity/ItemOwner;I)F", at = @At("HEAD"), cancellable = true)
    private void libri$forceCustomBookModel(ItemStack itemStack, @Nullable ClientLevel clientLevel, @Nullable ItemOwner itemOwner, int i, CallbackInfoReturnable<Float> cir) {
        if (itemStack != null && itemStack.is(Items.ENCHANTED_BOOK) && ModEnchantmentHelper.hasModEnchantment(itemStack)) {
            cir.setReturnValue(1.0f);
        }
    }
}
