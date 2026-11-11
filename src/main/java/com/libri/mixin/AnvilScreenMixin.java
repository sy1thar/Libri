package com.libri.mixin;

import com.libri.config.ModConfig;
import net.minecraft.client.gui.screens.inventory.AnvilScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(AnvilScreen.class)
public class AnvilScreenMixin {

    @ModifyConstant(method = "extractLabels", constant = @Constant(intValue = 40))
    private int onTooExpensiveCheck(int original) {
        AnvilScreen screen = (AnvilScreen) (Object) this;
        boolean isAdvanced = screen.getTitle().getString().equals(Component.translatable("container.libri.advanced_anvil").getString());
        if (isAdvanced || ModConfig.get().removeAnvilCap) {
            return 99999;
        }
        return original;
    }
}
