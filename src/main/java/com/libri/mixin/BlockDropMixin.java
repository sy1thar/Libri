package com.libri.mixin;

import com.libri.config.FurnaceEnchantmentData;
import com.libri.enchantment.ModEnchantmentHelper;
import com.libri.enchantment.ModEnchantments;
import com.libri.helper.TelepathyHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Prediction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(Block.class)
public class BlockDropMixin {

    @Inject(method = "dropResources(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/entity/BlockEntity;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/item/ItemStack;)V", at = @At("HEAD"), cancellable = true)
    private static void onDropStacks(BlockState state, Level world, BlockPos pos, BlockEntity blockEntity, Entity entity, ItemStack tool, CallbackInfo ci) {
        if (world.isClientSide()) return;
        if (!(entity instanceof ServerPlayer player)) return;

        ServerLevel serverWorld = (ServerLevel) world;
        var ench = EnchantmentHelper.getEnchantmentsForCrafting(tool);
        boolean telekinesis = !tool.isEmpty() && ModEnchantmentHelper.getLevel(ModEnchantments.TELEKINESIS, ench) > 0;
        boolean smeltingTouch = !tool.isEmpty() && ModEnchantmentHelper.getLevel(ModEnchantments.SMELTING_TOUCH, ench) > 0;
        boolean sifting = !tool.isEmpty() && ModEnchantmentHelper.getLevel(ModEnchantments.SIFTING, ench) > 0;
        boolean enchantedFurnace = false;

        BlockPos originPos = TelepathyHelper.RESONANCE_ORIGIN.get();
        List<ItemStack> resDrops = TelepathyHelper.RESONANCE_DROPS.get();
        boolean resonanceRedirect = originPos != null && !originPos.equals(pos);
        boolean isResonating = resDrops != null;
        BlockPos dropPos = originPos != null ? originPos : pos;

        if (blockEntity instanceof AbstractFurnaceBlockEntity furnace) {
            FurnaceEnchantmentData data = (FurnaceEnchantmentData) furnace;
            int speed = data.libri$getSmeltingSpeed();
            int fuel = data.libri$getFuelEfficiency();
            enchantedFurnace = speed > 0 || fuel > 0;
        }

        if (!telekinesis && !enchantedFurnace && !smeltingTouch && !sifting && !resonanceRedirect && !isResonating) return;

        try {
            if (telekinesis) {
                TelepathyHelper.BLOCK_MINER.set(player);
            }

            ci.cancel();
            List<ItemStack> drops = Block.getDrops(state, serverWorld, pos, blockEntity, entity, tool);

            if (smeltingTouch) {
                drops = smeltDrops(serverWorld, drops);
            }

            if (sifting) {
                List<String> junk = com.libri.config.ModConfig.get().siftingJunkBlocks;
                drops.removeIf(stack -> junk.contains(BuiltInRegistries.ITEM.getKey(stack.getItem()).toString()));
            }

            if (enchantedFurnace) {
                var registry = serverWorld.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
                var builder = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
                FurnaceEnchantmentData data = (FurnaceEnchantmentData) blockEntity;
                int speed = data.libri$getSmeltingSpeed();
                int fuel = data.libri$getFuelEfficiency();
                var speedEnch = registry.get(ModEnchantments.SMELTING_SPEED);
                var fuelEnch = registry.get(ModEnchantments.FUEL_EFFICIENCY);
                speedEnch.ifPresent(entry -> builder.upgrade(entry, speed));
                fuelEnch.ifPresent(entry -> builder.upgrade(entry, fuel));
                for (ItemStack stack : drops) {
                    if (stack.is(state.getBlock().asItem())) {
                        stack.set(DataComponents.ENCHANTMENTS, builder.toImmutable());
                    }
                }
            }

            for (ItemStack drop : drops) {
                if (!drop.isEmpty()) {
                    if (telekinesis) {
                        player.getInventory().placeItemBackInInventory(drop, Prediction.SERVER_ONLY);
                    } else if (resDrops != null) {
                        resDrops.add(drop);
                    } else {
                        Block.popResource(world, dropPos, drop);
                    }
                }
            }

            state.spawnAfterBreak(serverWorld, dropPos, tool, true);
        } finally {
            TelepathyHelper.BLOCK_MINER.remove();
        }
    }

    @Inject(method = "dropResources(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)V", at = @At("HEAD"), cancellable = true)
    private static void onNaturalDropStacks(BlockState state, Level world, BlockPos pos, CallbackInfo ci) {
        if (world.isClientSide()) return;
        ServerPlayer player = TelepathyHelper.BLOCK_MINER.get();
        if (player == null) return;

        ItemStack tool = player.getMainHandItem();
        if (tool.isEmpty()) return;
        var ench = EnchantmentHelper.getEnchantmentsForCrafting(tool);
        boolean telekinesis = ModEnchantmentHelper.getLevel(ModEnchantments.TELEKINESIS, ench) > 0;
        boolean smeltingTouch = ModEnchantmentHelper.getLevel(ModEnchantments.SMELTING_TOUCH, ench) > 0;
        boolean sifting = ModEnchantmentHelper.getLevel(ModEnchantments.SIFTING, ench) > 0;

        BlockPos originPos = TelepathyHelper.RESONANCE_ORIGIN.get();
        List<ItemStack> resDrops = TelepathyHelper.RESONANCE_DROPS.get();
        boolean resonanceRedirect = originPos != null && !originPos.equals(pos);
        boolean isResonating = resDrops != null;
        BlockPos dropPos = originPos != null ? originPos : pos;

        if (!telekinesis && !smeltingTouch && !sifting && !resonanceRedirect && !isResonating) return;

        ServerLevel serverWorld = (ServerLevel) world;
        ci.cancel();
        List<ItemStack> drops = Block.getDrops(state, serverWorld, pos, null, player, tool);

        if (smeltingTouch) {
            drops = smeltDrops(serverWorld, drops);
        }

        if (sifting) {
            List<String> junk = com.libri.config.ModConfig.get().siftingJunkBlocks;
            drops.removeIf(stack -> junk.contains(BuiltInRegistries.ITEM.getKey(stack.getItem()).toString()));
        }

        for (ItemStack drop : drops) {
            if (!drop.isEmpty()) {
                if (telekinesis) {
                    player.getInventory().placeItemBackInInventory(drop, Prediction.SERVER_ONLY);
                } else if (resDrops != null) {
                    resDrops.add(drop);
                } else {
                    Block.popResource(world, dropPos, drop);
                }
            }
        }
        state.spawnAfterBreak(serverWorld, dropPos, tool, true);
    }

    private static List<ItemStack> smeltDrops(ServerLevel world, List<ItemStack> drops) {
        List<ItemStack> smelted = new ArrayList<>();
        for (ItemStack drop : drops) {
            if (drop.isEmpty()) continue;
            SingleRecipeInput matchInput = new SingleRecipeInput(drop);
            var recipe = world.recipeAccess()
                    .getRecipeFor(RecipeType.SMELTING, matchInput, world);
            if (!recipe.isPresent()) {
                matchInput = new SingleRecipeInput(new ItemStack(drop.getItem(), drop.getCount()));
                recipe = world.recipeAccess()
                        .getRecipeFor(RecipeType.SMELTING, matchInput, world);
            }
            if (recipe.isPresent()) {
                ItemStack result = recipe.get().value().assemble(matchInput).copy();
                if (!result.isEmpty()) {
                    long remaining = (long) result.getCount() * drop.getCount();
                    while (remaining > 0) {
                        int count = (int) Math.min(remaining, result.getMaxStackSize());
                        smelted.add(result.copyWithCount(count));
                        remaining -= count;
                    }
                    continue;
                }
            }
            smelted.add(drop.copy());
        }
        return smelted;
    }
}
