package com.libri.mixin;

import com.libri.config.ModConfig;
import net.minecraft.screen.AnvilScreenHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.item.ItemStack;
import net.minecraft.enchantment.EnchantmentHelper;

@Mixin(AnvilScreenHandler.class)
public class AnvilScreenHandlerMixin {

    @ModifyConstant(method = "updateResult", constant = @Constant(intValue = 40))
    private int onLevelCap(int original) {
        net.minecraft.screen.ScreenHandlerContext ctx = ((ForgingScreenHandlerAccessor) this).getContext();
        boolean isAdvanced = ctx.get((world, pos) -> world.getBlockState(pos).isOf(com.libri.LibriMod.ADVANCED_ANVIL)).orElse(false);
        if (isAdvanced) {
            return 99999;
        }
        return original;
    }

    @Inject(method = "updateResult", at = @At("RETURN"))
    private void preventWastingEnchantments(CallbackInfo ci) {
        AnvilScreenHandler handler = (AnvilScreenHandler) (Object) this;
        ItemStack input1 = handler.getSlot(0).getStack();
        ItemStack input2 = handler.getSlot(1).getStack();
        ItemStack output = handler.getSlot(2).getStack();

        if (input1.isEmpty() || input2.isEmpty() || output.isEmpty()) return;

        boolean nameChanged = !input1.getName().getString().equals(output.getName().getString());
        boolean damageChanged = input1.getDamage() != output.getDamage();
        boolean enchantmentsChanged = !EnchantmentHelper.getEnchantments(input1).equals(EnchantmentHelper.getEnchantments(output));

        if (!nameChanged && !damageChanged && !enchantmentsChanged) {
            handler.getSlot(2).setStack(ItemStack.EMPTY);
        }
    }
}
