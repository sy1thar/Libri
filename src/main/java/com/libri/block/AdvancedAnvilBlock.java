package com.libri.block;

import net.minecraft.block.AnvilBlock;
import net.minecraft.block.BlockState;
import net.minecraft.screen.AnvilScreenHandler;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class AdvancedAnvilBlock extends AnvilBlock {
    public AdvancedAnvilBlock(Settings settings) {
        super(settings);
    }

    @Override
    public NamedScreenHandlerFactory createScreenHandlerFactory(BlockState state, World world, BlockPos pos) {
        return new SimpleNamedScreenHandlerFactory((syncId, inventory, player) ->
                new AnvilScreenHandler(syncId, inventory, ScreenHandlerContext.create(world, pos)),
                Text.translatable("container.libri.advanced_anvil"));
    }
}
