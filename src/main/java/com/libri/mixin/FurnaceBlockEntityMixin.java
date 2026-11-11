package com.libri.mixin;

import com.libri.config.FurnaceEnchantmentData;
import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractFurnaceBlockEntity.class)
public abstract class FurnaceBlockEntityMixin implements FurnaceEnchantmentData {
    @Unique private int libri$smeltingSpeed;
    @Unique private int libri$fuelEfficiency;
    @Unique private int libri$baseCookTimeTotal;
    @Override public int libri$getSmeltingSpeed() { return libri$smeltingSpeed; }
    @Override public void libri$setSmeltingSpeed(int v) { libri$smeltingSpeed = v; }
    @Override public int libri$getFuelEfficiency() { return libri$fuelEfficiency; }
    @Override public void libri$setFuelEfficiency(int v) { libri$fuelEfficiency = v; }
    @Override public int libri$getBaseCookTimeTotal() { return libri$baseCookTimeTotal; }
    @Override public void libri$setBaseCookTimeTotal(int v) { libri$baseCookTimeTotal = v; }

    @Inject(method = "readNbt", at = @At("TAIL"))
    private void onReadNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup, CallbackInfo ci) {
        libri$smeltingSpeed = nbt.getInt("libri:smelting_speed");
        libri$fuelEfficiency = nbt.getInt("libri:fuel_efficiency");
        libri$baseCookTimeTotal = nbt.getInt("libri:base_cook_time");
    }

    @Inject(method = "writeNbt", at = @At("TAIL"))
    private void onWriteNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup, CallbackInfo ci) {
        if (libri$smeltingSpeed > 0) nbt.putInt("libri:smelting_speed", libri$smeltingSpeed);
        if (libri$fuelEfficiency > 0) nbt.putInt("libri:fuel_efficiency", libri$fuelEfficiency);
        if (libri$baseCookTimeTotal > 0) nbt.putInt("libri:base_cook_time", libri$baseCookTimeTotal);
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private static void onTick(ServerWorld world, BlockPos pos, net.minecraft.block.BlockState state, AbstractFurnaceBlockEntity be, CallbackInfo ci) {
        if (world.isClient()) return;
        FurnaceEnchantmentData ench = (FurnaceEnchantmentData) be;

        int speed = ench.libri$getSmeltingSpeed();
        if (speed > 0) {
            FurnaceBlockEntityAccessor acc = (FurnaceBlockEntityAccessor) be;
            int currentTotal = acc.libri$getCookTimeTotal();
            if (currentTotal <= 0) return;
            int baseTotal = ench.libri$getBaseCookTimeTotal();
            int expectedScaled = baseTotal > 0 ? Math.max(1, baseTotal / (1 + speed * speed * 2)) : 0;
            if (currentTotal != expectedScaled) {
                baseTotal = currentTotal;
                ench.libri$setBaseCookTimeTotal(baseTotal);
                int scaled = Math.max(1, baseTotal / (1 + speed * speed * 2));
                if (currentTotal > scaled) {
                    acc.libri$setCookTimeTotal(scaled);
                }
            }
        }

        int fuelEff = ench.libri$getFuelEfficiency();
        if (fuelEff > 0) {
            FurnaceBlockEntityAccessor acc = (FurnaceBlockEntityAccessor) be;
            if (acc.libri$getFuelTime() > 0 && world.random.nextInt(fuelEff + 10) < fuelEff) {
                acc.libri$setFuelTime(acc.libri$getFuelTime() + 1);
            }
        }
    }

}
