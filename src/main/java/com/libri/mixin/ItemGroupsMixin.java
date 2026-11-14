package com.libri.mixin;

import com.libri.enchantment.ModEnchantmentHelper;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.stream.Stream;

@Mixin(ItemGroups.class)
public class ItemGroupsMixin {

    @Redirect(
            method = {
                    "addMaxLevelEnchantedBooks",
                    "addAllLevelEnchantedBooks"
            },
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/registry/RegistryWrapper;streamEntries()Ljava/util/stream/Stream;"
            )
    )
    private static Stream<RegistryEntry.Reference<Enchantment>> libri$excludeModEnchantments(RegistryWrapper<Enchantment> wrapper) {
        return wrapper.streamEntries().filter(entry -> !ModEnchantmentHelper.isModEnchantment(entry));
    }
}
