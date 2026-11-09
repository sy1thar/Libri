package com.libri.mixin;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = {"net/minecraft/screen/GrindstoneScreenHandler$2", "net/minecraft/screen/GrindstoneScreenHandler$3"})
public class GrindstoneSlotMixin {
    @Inject(method = "canInsert", at = @At("HEAD"), cancellable = true)
    private void allowBooks(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (stack.is(Items.BOOK)) {
            cir.setReturnValue(true);
        }
    }
}
