package com.libri.mixin;

import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractFurnaceBlockEntity.class)
public interface FurnaceBlockEntityAccessor {
    @Accessor("cookingTimer")
    int libri$getCookTime();
    @Accessor("cookingTimer")
    void libri$setCookTime(int value);
    @Accessor("litTimeRemaining")
    int libri$getFuelTime();
    @Accessor("litTimeRemaining")
    void libri$setFuelTime(int value);
    @Accessor("cookingTotalTime")
    int libri$getCookTimeTotal();
    @Accessor("cookingTotalTime")
    void libri$setCookTimeTotal(int value);
}
