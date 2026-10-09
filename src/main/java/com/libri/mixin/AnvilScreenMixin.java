package com.libri.mixin;

import com.libri.config.ModConfig;
import net.minecraft.client.gui.screen.ingame.AnvilScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(AnvilScreen.class)
public class AnvilScreenMixin {

    @ModifyConstant(method = "drawForeground", constant = @Constant(intValue = 40))
    private int onTooExpensiveCheck(int original) {
        AnvilScreen screen = (AnvilScreen) (Object) this;
        if (screen.getTitle().getString().equals(net.minecraft.text.Text.translatable("container.libri.advanced_anvil").getString())) {
            return 99999;
        }
        return original;
    }
}
