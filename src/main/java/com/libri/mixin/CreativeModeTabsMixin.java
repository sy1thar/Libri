package com.libri.mixin;

import com.libri.enchantment.ModEnchantmentHelper;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.enchantment.Enchantment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.stream.Stream;

@Mixin(CreativeModeTabs.class)
public class CreativeModeTabsMixin {

    @Redirect(
            method = {
                    "generateEnchantmentBookTypesOnlyMaxLevel(Lnet/minecraft/world/item/CreativeModeTab$Output;Lnet/minecraft/core/HolderLookup;Lnet/minecraft/world/item/CreativeModeTab$TabVisibility;)V",
                    "generateEnchantmentBookTypesAllLevels(Lnet/minecraft/world/item/CreativeModeTab$Output;Lnet/minecraft/core/HolderLookup;Lnet/minecraft/world/item/CreativeModeTab$TabVisibility;)V"
            },
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/core/HolderLookup;listElements()Ljava/util/stream/Stream;"
            )
    )
    private static Stream<Holder.Reference<Enchantment>> libri$excludeModEnchantmentsFromVanillaTabs(HolderLookup<Enchantment> lookup) {
        return lookup.listElements().filter(ref -> !ModEnchantmentHelper.isModEnchantment(ref));
    }
}
