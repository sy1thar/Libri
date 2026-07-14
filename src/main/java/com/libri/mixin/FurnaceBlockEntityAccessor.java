package com.libri.mixin;

import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractFurnaceBlockEntity.class)
public interface FurnaceBlockEntityAccessor {
    @Accessor("cookTime")
    int libri$getCookTime();
    @Accessor("cookTime")
    void libri$setCookTime(int value);
    @Accessor("fuelTime")
    int libri$getFuelTime();
    @Accessor("fuelTime")
    void libri$setFuelTime(int value);
    @Accessor("cookTimeTotal")
    int libri$getCookTimeTotal();
    @Accessor("cookTimeTotal")
    void libri$setCookTimeTotal(int value);
}
