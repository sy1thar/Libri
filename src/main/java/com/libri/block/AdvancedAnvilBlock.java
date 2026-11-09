package com.libri.block;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AnvilBlock;
import net.minecraft.world.level.block.state.BlockState;

public class AdvancedAnvilBlock extends AnvilBlock {
    public AdvancedAnvilBlock(Properties settings) {
        super(settings);
    }

    @Override
    public MenuProvider getMenuProvider(BlockState state, Level world, BlockPos pos) {
        return new SimpleMenuProvider((syncId, inventory, player) ->
                new AnvilMenu(syncId, inventory, ContainerLevelAccess.create(world, pos)),
                Component.translatable("container.libri.advanced_anvil"));
    }
}
