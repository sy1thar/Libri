package com.libri.mixin;

import net.minecraft.inventory.Inventory;
import net.minecraft.screen.GrindstoneScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(GrindstoneScreenHandler.class)
public interface GrindstoneScreenHandlerAccessor {
    @Accessor("input")
    Inventory libri$getInput();

    @Accessor("context")
    ScreenHandlerContext libri$getContext();
}
