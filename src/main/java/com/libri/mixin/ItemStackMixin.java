package com.libri.mixin;

import com.libri.enchantment.ModEnchantmentHelper;
import com.libri.enchantment.ModEnchantments;
import net.minecraft.component.ComponentChanges;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.DataComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public class ItemStackMixin {

    @Inject(method = "damage(ILnet/minecraft/util/math/random/Random;Lnet/minecraft/server/network/ServerPlayerEntity;Ljava/lang/Runnable;)V", at = @At("HEAD"), cancellable = true)
    private void onDamage(int amount, Random random, @Nullable ServerPlayerEntity player, Runnable breakCallback, CallbackInfo ci) {
        ItemStack stack = (ItemStack) (Object) this;
        if (ModEnchantmentHelper.getLevel(ModEnchantments.ETERNITY, stack) > 0) {
            ci.cancel();
        }
    }

    @Inject(method = "set(Lnet/minecraft/component/DataComponentType;Ljava/lang/Object;)Ljava/lang/Object;", at = @At("TAIL"))
    private <T> void libri$applyCustomBookModelOnSet(DataComponentType<T> type, T value, CallbackInfoReturnable<T> cir) {
        if (type == DataComponentTypes.STORED_ENCHANTMENTS || type == DataComponentTypes.ENCHANTMENTS) {
            ModEnchantmentHelper.applyCustomBookModelIfModEnchanted((ItemStack) (Object) this);
        }
    }

    @Inject(method = "applyChanges", at = @At("TAIL"))
    private void libri$applyCustomBookModelOnApplyChanges(ComponentChanges changes, CallbackInfo ci) {
        ModEnchantmentHelper.applyCustomBookModelIfModEnchanted((ItemStack) (Object) this);
    }

    @Inject(method = "applyComponentsFrom", at = @At("TAIL"))
    private void libri$applyCustomBookModelOnApplyMap(ComponentMap map, CallbackInfo ci) {
        ModEnchantmentHelper.applyCustomBookModelIfModEnchanted((ItemStack) (Object) this);
    }

    @Inject(method = "getName", at = @At("RETURN"), cancellable = true)
    private void libri$colorCustomBookNameGold(CallbackInfoReturnable<Text> cir) {
        ItemStack stack = (ItemStack) (Object) this;
        if (stack.isOf(Items.ENCHANTED_BOOK) && ModEnchantmentHelper.hasModEnchantment(stack)) {
            MutableText goldenName = Text.empty().append(cir.getReturnValue()).formatted(Formatting.GOLD);
            if (stack.contains(DataComponentTypes.CUSTOM_NAME)) {
                goldenName.formatted(Formatting.ITALIC);
            }
            cir.setReturnValue(goldenName);
        }
    }
}
