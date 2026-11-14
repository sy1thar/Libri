package com.libri.mixin;

import com.libri.enchantment.ModEnchantmentHelper;
import com.libri.enchantment.ModEnchantments;
import net.minecraft.component.ComponentChanges;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Consumer;

@Mixin(ItemStack.class)
public class ItemStackMixin {

    @Inject(method = "damage(ILnet/minecraft/server/world/ServerWorld;Lnet/minecraft/server/network/ServerPlayerEntity;Ljava/util/function/Consumer;)V", at = @At("HEAD"), cancellable = true)
    private void onDamage(int amount, ServerWorld world, @Nullable ServerPlayerEntity player, Consumer<Item> breakCallback, CallbackInfo ci) {
        ItemStack stack = (ItemStack) (Object) this;
        if (ModEnchantmentHelper.getLevel(ModEnchantments.ETERNITY, stack) > 0) {
            ci.cancel();
        }
    }

    @Inject(method = "set(Lnet/minecraft/component/ComponentType;Ljava/lang/Object;)Ljava/lang/Object;", at = @At("TAIL"))
    private <T> void libri$applyCustomBookModelOnSet(ComponentType<T> type, T value, CallbackInfoReturnable<T> cir) {
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
