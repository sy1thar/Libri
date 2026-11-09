package com.libri.mixin;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.GrindstoneMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(GrindstoneMenu.class)
public interface GrindstoneScreenHandlerAccessor {
    @Accessor("input")
    Container libri$getInput();

    @Accessor("context")
    ContainerLevelAccess libri$getContext();
}
