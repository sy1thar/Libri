package com.libri.mixin;

import com.libri.config.ModConfig;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AnvilMenu.class)
public class AnvilScreenHandlerMixin {

    @ModifyConstant(method = "createResult", constant = @Constant(intValue = 40))
    private int onLevelCap(int original) {
        net.minecraft.world.inventory.ContainerLevelAccess ctx = ((ForgingScreenHandlerAccessor) this).getContext();
        boolean isAdvanced = ctx.evaluate((world, pos) -> world.getBlockState(pos).is(com.libri.LibriMod.ADVANCED_ANVIL)).orElse(false);
        if (isAdvanced || ModConfig.get().removeAnvilCap) {
            return 99999;
        }
        return original;
    }

    @Inject(method = "createResult", at = @At("RETURN"))
    private void preventWastingEnchantments(CallbackInfo ci) {
        AnvilMenu handler = (AnvilMenu) (Object) this;
        ItemStack input1 = handler.getSlot(0).getItem();
        ItemStack input2 = handler.getSlot(1).getItem();
        ItemStack output = handler.getSlot(2).getItem();

        if (input1.isEmpty() || input2.isEmpty() || output.isEmpty()) return;

        boolean nameChanged = !input1.getHoverName().getString().equals(output.getHoverName().getString());
        boolean damageChanged = input1.getDamageValue() != output.getDamageValue();
        boolean enchantmentsChanged = !EnchantmentHelper.getEnchantmentsForCrafting(input1).equals(EnchantmentHelper.getEnchantmentsForCrafting(output));

        if (!nameChanged && !damageChanged && !enchantmentsChanged) {
            handler.getSlot(2).setByPlayer(ItemStack.EMPTY);
        }
    }
}
