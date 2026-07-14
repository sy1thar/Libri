package com.libri.mixin;

import com.libri.config.ModConfig;
import net.minecraft.screen.AnvilScreenHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(AnvilScreenHandler.class)
public class AnvilScreenHandlerMixin {

    @ModifyConstant(method = "updateResult", constant = @Constant(intValue = 40))
    private int onLevelCap(int original) {
        if (ModConfig.get().removeAnvilCap) {
            return 99999;
        }
        return original;
    }
}
