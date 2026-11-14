package com.libri.mixin;

import com.libri.enchantment.ModEnchantmentHelper;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ItemModelResolver.class)
public class ItemModelResolverMixin {

    @Redirect(
            method = {
                    "appendItemLayers(Lnet/minecraft/client/renderer/item/ItemStackRenderState;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/ItemOwner;I)V",
                    "shouldPlaySwapAnimation(Lnet/minecraft/world/item/ItemStack;)Z",
                    "swapAnimationScale(Lnet/minecraft/world/item/ItemStack;)F"
            },
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStack;get(Lnet/minecraft/core/component/DataComponentType;)Ljava/lang/Object;"
            )
    )
    private Object libri$redirectItemModelForModEnchantedBooks(ItemStack stack, DataComponentType<?> type) {
        if (type == DataComponents.ITEM_MODEL && stack != null && stack.is(Items.ENCHANTED_BOOK) && ModEnchantmentHelper.hasModEnchantment(stack)) {
            return ModEnchantmentHelper.BOOK_MODEL_ID;
        }
        return stack.get(type);
    }
}
