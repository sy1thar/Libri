package com.libri.mixin;

import com.libri.enchantment.ModEnchantmentHelper;
import com.libri.enchantment.ModEnchantments;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public class ItemStackMixin {

    private static final TagKey<Item> FURNACE_TAG = TagKey.of(RegistryKeys.ITEM, new Identifier("libri", "enchantable/furnace"));

    @Inject(method = "damage(ILnet/minecraft/util/math/random/Random;Lnet/minecraft/server/network/ServerPlayerEntity;)Z", at = @At("HEAD"), cancellable = true)
    private void onDamage(int amount, Random random, @Nullable ServerPlayerEntity player, CallbackInfoReturnable<Boolean> cir) {
        ItemStack stack = (ItemStack) (Object) this;
        if (ModEnchantmentHelper.getLevel(ModEnchantments.ETERNITY, stack) > 0) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "isEnchantable()Z", at = @At("RETURN"), cancellable = true)
    private void onIsEnchantable(CallbackInfoReturnable<Boolean> cir) {
        ItemStack stack = (ItemStack) (Object) this;
        if (!cir.getReturnValueZ() && stack.isIn(FURNACE_TAG) && !stack.hasEnchantments()) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "setNbt", at = @At("TAIL"))
    private void libri$applyCustomBookModelOnSetNbt(NbtCompound nbt, CallbackInfo ci) {
        ModEnchantmentHelper.applyCustomBookModelIfModEnchanted((ItemStack) (Object) this);
    }

    @Inject(method = "setSubNbt", at = @At("TAIL"))
    private void libri$applyCustomBookModelOnSetSubNbt(String key, NbtElement element, CallbackInfo ci) {
        ModEnchantmentHelper.applyCustomBookModelIfModEnchanted((ItemStack) (Object) this);
    }

    @Inject(method = "getName", at = @At("RETURN"), cancellable = true)
    private void libri$colorCustomBookNameGold(CallbackInfoReturnable<Text> cir) {
        ItemStack stack = (ItemStack) (Object) this;
        if (stack.isOf(Items.ENCHANTED_BOOK) && ModEnchantmentHelper.hasModEnchantment(stack)) {
            ModEnchantmentHelper.applyCustomBookModelIfModEnchanted(stack);
            MutableText goldenName = Text.empty().append(cir.getReturnValue()).formatted(Formatting.GOLD);
            if (stack.hasCustomName()) {
                goldenName.formatted(Formatting.ITALIC);
            }
            cir.setReturnValue(goldenName);
        }
    }
}
