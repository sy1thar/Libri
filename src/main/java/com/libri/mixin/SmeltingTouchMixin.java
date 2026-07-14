package com.libri.mixin;

import com.libri.enchantment.ModEnchantmentHelper;
import com.libri.enchantment.ModEnchantments;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.input.SingleStackRecipeInput;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(Block.class)
public class SmeltingTouchMixin {

    @Inject(method = "dropStacks(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/entity/BlockEntity;Lnet/minecraft/entity/Entity;Lnet/minecraft/item/ItemStack;)V", at = @At("HEAD"), cancellable = true)
    private static void onDropStacks(BlockState state, World world, BlockPos pos, BlockEntity blockEntity, Entity entity, ItemStack tool, CallbackInfo ci) {
        if (world.isClient) return;
        if (!(entity instanceof ServerPlayerEntity player)) return;
        if (tool.isEmpty()) return;
        if (ModEnchantmentHelper.getLevel(ModEnchantments.SMELTING_TOUCH, EnchantmentHelper.getEnchantments(tool)) <= 0) return;

        ServerWorld serverWorld = (ServerWorld) world;
        ci.cancel();
        List<ItemStack> drops = Block.getDroppedStacks(state, serverWorld, pos, blockEntity, entity, tool);

        List<ItemStack> smelted = new ArrayList<>();
        for (ItemStack drop : drops) {
            if (drop.isEmpty()) continue;
            var recipe = serverWorld.getRecipeManager()
                    .getFirstMatch(RecipeType.SMELTING, new SingleStackRecipeInput(drop), serverWorld);
            if (recipe.isPresent()) {
                ItemStack result = recipe.get().value().getResult(serverWorld.getRegistryManager());
                if (!result.isEmpty()) {
                    result.setCount(result.getCount() * drop.getCount());
                    smelted.add(result);
                    continue;
                }
            }
            smelted.add(drop.copy());
        }

        for (ItemStack drop : smelted) {
            if (!drop.isEmpty()) {
                Block.dropStack(world, pos, drop);
            }
        }
        state.onStacksDropped(serverWorld, pos, tool, true);
    }
}
