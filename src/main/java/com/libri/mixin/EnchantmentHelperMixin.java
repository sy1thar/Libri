package com.libri.mixin;

import com.libri.enchantment.ModEnchantments;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(EnchantmentHelper.class)
public class EnchantmentHelperMixin {

    private static final TagKey<Item> FURNACE_TAG = TagKey.of(RegistryKeys.ITEM, new Identifier("libri", "enchantable/furnace"));

    @Inject(method = "getPossibleEntries", at = @At("RETURN"))
    private static void onGetPossibleEntries(int power, ItemStack stack, boolean treasureAllowed, CallbackInfoReturnable<List<EnchantmentLevelEntry>> cir) {
        if (!stack.isIn(FURNACE_TAG)) return;
        List<EnchantmentLevelEntry> list = cir.getReturnValue();
        libri$addFurnaceEnchant(list, ModEnchantments.SMELTING_SPEED, power);
        libri$addFurnaceEnchant(list, ModEnchantments.FUEL_EFFICIENCY, power);
    }

    @Unique
    private static void libri$addFurnaceEnchant(List<EnchantmentLevelEntry> list, Enchantment ench, int power) {
        if (!ench.isAvailableForRandomSelection()) return;
        for (int lvl = ench.getMaxLevel(); lvl >= ench.getMinLevel(); lvl--) {
            if (power >= ench.getMinPower(lvl) && power <= ench.getMaxPower(lvl)) {
                list.add(new EnchantmentLevelEntry(ench, lvl));
                break;
            }
        }
    }
}
