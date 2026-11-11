package com.libri.mixin;

import com.libri.config.FurnaceEnchantmentData;
import com.libri.enchantment.ModEnchantmentHelper;
import com.libri.enchantment.ModEnchantments;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockItem.class)
public class BlockItemMixin {

    @Inject(method = "updateCustomBlockEntityTag(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/item/ItemStack;)Z", at = @At("TAIL"))
    private static void onPostPlacement(Level world, Player player, BlockPos pos, ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (world.isClientSide()) return;
        BlockEntity be = world.getBlockEntity(pos);
        if (be instanceof AbstractFurnaceBlockEntity furnace) {
            var ench = EnchantmentHelper.getEnchantmentsForCrafting(stack);
            int speed = ModEnchantmentHelper.getLevel(ModEnchantments.SMELTING_SPEED, ench);
            int fuel = ModEnchantmentHelper.getLevel(ModEnchantments.FUEL_EFFICIENCY, ench);
            if (speed > 0 || fuel > 0) {
                FurnaceEnchantmentData data = (FurnaceEnchantmentData) furnace;
                data.libri$setSmeltingSpeed(speed);
                data.libri$setFuelEfficiency(fuel);
            }
        }
    }
}
