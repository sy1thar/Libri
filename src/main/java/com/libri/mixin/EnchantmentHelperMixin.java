package com.libri.mixin;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnchantmentHelper.class)
public class EnchantmentHelperMixin {

    private static final TagKey<net.minecraft.world.item.Item> FURNACE_TAG = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("libri", "enchantable/furnace"));

    @Inject(method = "canHaveEnchantments", at = @At("HEAD"), cancellable = true)
    private static void onCanHaveEnchantments(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (stack.is(FURNACE_TAG)) {
            cir.setReturnValue(true);
        }
    }
}
