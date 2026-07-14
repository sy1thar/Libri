package com.libri.mixin;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnchantmentHelper.class)
public class EnchantmentHelperMixin {

    private static final TagKey<net.minecraft.item.Item> FURNACE_TAG = TagKey.of(RegistryKeys.ITEM, Identifier.of("libri", "enchantable/furnace"));

    @Inject(method = "canHaveEnchantments", at = @At("HEAD"), cancellable = true)
    private static void onCanHaveEnchantments(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (stack.isIn(FURNACE_TAG)) {
            cir.setReturnValue(true);
        }
    }
}
