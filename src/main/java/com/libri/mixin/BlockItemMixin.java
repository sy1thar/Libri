package com.libri.mixin;

import com.libri.config.FurnaceEnchantmentData;
import com.libri.enchantment.ModEnchantmentHelper;
import com.libri.enchantment.ModEnchantments;
import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockItem.class)
public class BlockItemMixin {

    @Inject(method = "postPlacement", at = @At("TAIL"))
    private void onPostPlacement(BlockPos pos, World world, PlayerEntity player, ItemStack stack, net.minecraft.block.BlockState state, CallbackInfoReturnable<Boolean> cir) {
        if (world.isClient) return;
        BlockEntity be = world.getBlockEntity(pos);
        if (be instanceof AbstractFurnaceBlockEntity furnace) {
            var ench = EnchantmentHelper.get(stack);
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
