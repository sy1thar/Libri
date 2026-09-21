package com.libri.mixin;

import com.libri.config.FurnaceEnchantmentData;
import com.libri.enchantment.ModEnchantmentHelper;
import com.libri.enchantment.ModEnchantments;
import com.libri.helper.TelepathyHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.input.SingleStackRecipeInput;
import net.minecraft.registry.RegistryKeys;
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
public class BlockDropMixin {

    @Inject(method = "dropStacks(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/entity/BlockEntity;Lnet/minecraft/entity/Entity;Lnet/minecraft/item/ItemStack;)V", at = @At("HEAD"), cancellable = true)
    private static void onDropStacks(BlockState state, World world, BlockPos pos, BlockEntity blockEntity, Entity entity, ItemStack tool, CallbackInfo ci) {
        if (world.isClient) return;
        if (!(entity instanceof ServerPlayerEntity player)) return;

        ServerWorld serverWorld = (ServerWorld) world;
        var ench = EnchantmentHelper.getEnchantments(tool);
        boolean telekinesis = !tool.isEmpty() && ModEnchantmentHelper.getLevel(ModEnchantments.TELEKINESIS, ench) > 0;
        boolean smeltingTouch = !tool.isEmpty() && ModEnchantmentHelper.getLevel(ModEnchantments.SMELTING_TOUCH, ench) > 0;
        boolean sifting = !tool.isEmpty() && ModEnchantmentHelper.getLevel(ModEnchantments.SIFTING, ench) > 0;
        boolean enchantedFurnace = false;

        if (blockEntity instanceof AbstractFurnaceBlockEntity furnace) {
            FurnaceEnchantmentData data = (FurnaceEnchantmentData) furnace;
            int speed = data.libri$getSmeltingSpeed();
            int fuel = data.libri$getFuelEfficiency();
            enchantedFurnace = speed > 0 || fuel > 0;
        }

        if (!telekinesis && !enchantedFurnace && !smeltingTouch && !sifting) return;

        try {
            if (telekinesis) {
                TelepathyHelper.BLOCK_MINER.set(player);
            }

            ci.cancel();
            List<ItemStack> drops = Block.getDroppedStacks(state, serverWorld, pos, blockEntity, entity, tool);

            if (smeltingTouch) {
                drops = smeltDrops(serverWorld, drops);
            }

            if (sifting) {
                java.util.List<String> junk = com.libri.config.ModConfig.get().siftingJunkBlocks;
                drops.removeIf(stack -> junk.contains(net.minecraft.registry.Registries.ITEM.getId(stack.getItem()).toString()));
            }

            if (enchantedFurnace) {
                var registry = serverWorld.getRegistryManager().get(RegistryKeys.ENCHANTMENT);
                var builder = new ItemEnchantmentsComponent.Builder(ItemEnchantmentsComponent.DEFAULT);
                FurnaceEnchantmentData data = (FurnaceEnchantmentData) blockEntity;
                int speed = data.libri$getSmeltingSpeed();
                int fuel = data.libri$getFuelEfficiency();
                var speedEnch = registry.getEntry(ModEnchantments.SMELTING_SPEED);
                var fuelEnch = registry.getEntry(ModEnchantments.FUEL_EFFICIENCY);
                speedEnch.ifPresent(entry -> builder.add(entry, speed));
                fuelEnch.ifPresent(entry -> builder.add(entry, fuel));
                for (ItemStack stack : drops) {
                    if (stack.isOf(state.getBlock().asItem())) {
                        stack.set(DataComponentTypes.ENCHANTMENTS, builder.build());
                    }
                }
            }

            for (ItemStack drop : drops) {
                if (!drop.isEmpty()) {
                    if (telekinesis) {
                        player.getInventory().offerOrDrop(drop);
                    } else {
                        Block.dropStack(world, pos, drop);
                    }
                }
            }

            state.onStacksDropped(serverWorld, pos, tool, true);
        } finally {
            TelepathyHelper.BLOCK_MINER.remove();
        }
    }

    @Inject(method = "dropStacks(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)V", at = @At("HEAD"), cancellable = true)
    private static void onNaturalDropStacks(BlockState state, World world, BlockPos pos, CallbackInfo ci) {
        if (world.isClient) return;
        ServerPlayerEntity player = TelepathyHelper.BLOCK_MINER.get();
        if (player == null) return;

        ItemStack tool = player.getMainHandStack();
        if (tool.isEmpty()) return;
        var ench = EnchantmentHelper.getEnchantments(tool);
        boolean telekinesis = ModEnchantmentHelper.getLevel(ModEnchantments.TELEKINESIS, ench) > 0;
        boolean smeltingTouch = ModEnchantmentHelper.getLevel(ModEnchantments.SMELTING_TOUCH, ench) > 0;
        boolean sifting = ModEnchantmentHelper.getLevel(ModEnchantments.SIFTING, ench) > 0;
        if (!telekinesis && !smeltingTouch && !sifting) return;

        ServerWorld serverWorld = (ServerWorld) world;
        ci.cancel();
        List<ItemStack> drops = Block.getDroppedStacks(state, serverWorld, pos, null, player, tool);

        if (smeltingTouch) {
            drops = smeltDrops(serverWorld, drops);
        }

        if (sifting) {
            java.util.List<String> junk = com.libri.config.ModConfig.get().siftingJunkBlocks;
            drops.removeIf(stack -> junk.contains(net.minecraft.registry.Registries.ITEM.getId(stack.getItem()).toString()));
        }

        for (ItemStack drop : drops) {
            if (!drop.isEmpty()) {
                if (telekinesis) {
                    player.getInventory().offerOrDrop(drop);
                } else {
                    Block.dropStack(world, pos, drop);
                }
            }
        }
        state.onStacksDropped(serverWorld, pos, tool, true);
    }

    private static List<ItemStack> smeltDrops(ServerWorld world, List<ItemStack> drops) {
        List<ItemStack> smelted = new ArrayList<>();
        for (ItemStack drop : drops) {
            if (drop.isEmpty()) continue;
            ItemStack matchInput = drop;
            var recipe = world.getRecipeManager()
                    .getFirstMatch(RecipeType.SMELTING, new SingleStackRecipeInput(matchInput), world);
            if (!recipe.isPresent()) {
                matchInput = new ItemStack(drop.getItem(), drop.getCount());
                recipe = world.getRecipeManager()
                        .getFirstMatch(RecipeType.SMELTING, new SingleStackRecipeInput(matchInput), world);
            }
            if (recipe.isPresent()) {
                ItemStack result = recipe.get().value().getResult(world.getRegistryManager()).copy();
                if (!result.isEmpty()) {
                    result.setCount(Math.min(drop.getCount(), 64));
                    smelted.add(result);
                    continue;
                }
            }
            smelted.add(drop.copy());
        }
        return smelted;
    }
}
