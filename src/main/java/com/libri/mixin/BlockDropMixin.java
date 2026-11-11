package com.libri.mixin;

import com.libri.config.FurnaceEnchantmentData;
import com.libri.enchantment.ModEnchantmentHelper;
import com.libri.enchantment.ModEnchantments;
import com.libri.helper.TelepathyHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Mixin(Block.class)
public class BlockDropMixin {

    @Inject(method = "dropStacks(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/entity/BlockEntity;Lnet/minecraft/entity/Entity;Lnet/minecraft/item/ItemStack;)V", at = @At("HEAD"), cancellable = true)
    private static void onDropStacks(BlockState state, World world, BlockPos pos, BlockEntity blockEntity, Entity entity, ItemStack tool, CallbackInfo ci) {
        if (world.isClient) return;
        if (!(entity instanceof ServerPlayerEntity player)) return;

        ServerWorld serverWorld = (ServerWorld) world;
        var ench = EnchantmentHelper.get(tool);
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
            List<ItemStack> drops = Block.getDroppedStacks(state, serverWorld, pos, blockEntity, entity, tool);

            if (smeltingTouch) {
                drops = smeltDrops(serverWorld, drops);
            }

            if (sifting) {
                List<String> junk = com.libri.config.ModConfig.get().siftingJunkBlocks;
                drops.removeIf(stack -> junk.contains(net.minecraft.registry.Registries.ITEM.getId(stack.getItem()).toString()));
            }

            if (enchantedFurnace) {
                Map<Enchantment, Integer> furnaceEnch = new LinkedHashMap<>();
                FurnaceEnchantmentData data = (FurnaceEnchantmentData) blockEntity;
                int speed = data.libri$getSmeltingSpeed();
                int fuel = data.libri$getFuelEfficiency();
                if (speed > 0) furnaceEnch.put(ModEnchantments.SMELTING_SPEED, speed);
                if (fuel > 0) furnaceEnch.put(ModEnchantments.FUEL_EFFICIENCY, fuel);
                for (ItemStack stack : drops) {
                    if (stack.isOf(state.getBlock().asItem())) {
                        EnchantmentHelper.set(furnaceEnch, stack);
                    }
                }
            }

            for (ItemStack drop : drops) {
                if (!drop.isEmpty()) {
                    if (telekinesis) {
                        player.getInventory().offerOrDrop(drop);
                    } else if (resDrops != null) {
                        resDrops.add(drop);
                    } else {
                        Block.dropStack(world, dropPos, drop);
                    }
                }
            }

            state.onStacksDropped(serverWorld, dropPos, tool, true);
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
        var ench = EnchantmentHelper.get(tool);
        boolean telekinesis = ModEnchantmentHelper.getLevel(ModEnchantments.TELEKINESIS, ench) > 0;
        boolean smeltingTouch = ModEnchantmentHelper.getLevel(ModEnchantments.SMELTING_TOUCH, ench) > 0;
        boolean sifting = ModEnchantmentHelper.getLevel(ModEnchantments.SIFTING, ench) > 0;

        BlockPos originPos = TelepathyHelper.RESONANCE_ORIGIN.get();
        List<ItemStack> resDrops = TelepathyHelper.RESONANCE_DROPS.get();
        boolean resonanceRedirect = originPos != null && !originPos.equals(pos);
        boolean isResonating = resDrops != null;
        BlockPos dropPos = originPos != null ? originPos : pos;

        if (!telekinesis && !smeltingTouch && !sifting && !resonanceRedirect && !isResonating) return;

        ServerWorld serverWorld = (ServerWorld) world;
        ci.cancel();
        List<ItemStack> drops = Block.getDroppedStacks(state, serverWorld, pos, null, player, tool);

        if (smeltingTouch) {
            drops = smeltDrops(serverWorld, drops);
        }

        if (sifting) {
            List<String> junk = com.libri.config.ModConfig.get().siftingJunkBlocks;
            drops.removeIf(stack -> junk.contains(net.minecraft.registry.Registries.ITEM.getId(stack.getItem()).toString()));
        }

        for (ItemStack drop : drops) {
            if (!drop.isEmpty()) {
                if (telekinesis) {
                    player.getInventory().offerOrDrop(drop);
                } else if (resDrops != null) {
                    resDrops.add(drop);
                } else {
                    Block.dropStack(world, dropPos, drop);
                }
            }
        }
        state.onStacksDropped(serverWorld, dropPos, tool, true);
    }

    private static List<ItemStack> smeltDrops(ServerWorld world, List<ItemStack> drops) {
        List<ItemStack> smelted = new ArrayList<>();
        for (ItemStack drop : drops) {
            if (drop.isEmpty()) continue;
            ItemStack matchInput = drop;
            var recipe = world.getRecipeManager()
                    .getFirstMatch(RecipeType.SMELTING, new SimpleInventory(matchInput), world);
            if (!recipe.isPresent()) {
                matchInput = new ItemStack(drop.getItem(), drop.getCount());
                recipe = world.getRecipeManager()
                        .getFirstMatch(RecipeType.SMELTING, new SimpleInventory(matchInput), world);
            }
            if (recipe.isPresent()) {
                ItemStack result = recipe.get().value().getResult(world.getRegistryManager()).copy();
                if (!result.isEmpty()) {
                    int total = drop.getCount() * result.getCount();
                    int maxStack = result.getMaxCount();
                    while (total > 0) {
                        int take = Math.min(total, maxStack);
                        ItemStack stack = result.copy();
                        stack.setCount(take);
                        smelted.add(stack);
                        total -= take;
                    }
                    continue;
                }
            }
            smelted.add(drop.copy());
        }
        return smelted;
    }
}
