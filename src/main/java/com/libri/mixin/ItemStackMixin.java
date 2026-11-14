package com.libri.mixin;

import com.libri.enchantment.ModEnchantmentHelper;
import com.libri.enchantment.ModEnchantments;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Consumer;

@Mixin(ItemStack.class)
public class ItemStackMixin {

    @Inject(method = "hurtAndBreak(ILnet/minecraft/server/level/ServerLevel;Lnet/minecraft/server/level/ServerPlayer;Ljava/util/function/Consumer;)V", at = @At("HEAD"), cancellable = true)
    private void onDamage(int amount, ServerLevel world, @Nullable ServerPlayer player, Consumer<ItemStack> breakCallback, CallbackInfo ci) {
        ItemStack stack = (ItemStack) (Object) this;
        if (ModEnchantmentHelper.getLevel(ModEnchantments.ETERNITY, stack) > 0) {
            ci.cancel();
        }
    }

    @Inject(method = "consume(ILnet/minecraft/world/entity/LivingEntity;)V", at = @At("HEAD"), cancellable = true)
    private void onConsume(int amount, @Nullable LivingEntity entity, CallbackInfo ci) {
        ItemStack stack = (ItemStack) (Object) this;
        // Tools and damageable items must NEVER be consumed as stackable items (e.g. by BlockTransformer or stacking mods).
        if (stack.isDamageableItem()) {
            ci.cancel();
            if (entity != null && amount > 0) {
                EquipmentSlot slot = EquipmentSlot.MAINHAND;
                try {
                    if (entity.getUsedItemHand() != null) {
                        slot = entity.getUsedItemHand().asEquipmentSlot();
                    }
                } catch (Exception ignored) {}
                stack.hurtAndBreak(amount, entity, slot);
            }
        }
    }

    @Inject(method = "enchant(Lnet/minecraft/core/Holder;I)V", at = @At("TAIL"))
    private void libri$applyCustomBookModelOnEnchant(Holder<Enchantment> enchantment, int level, CallbackInfo ci) {
        if (ModEnchantmentHelper.isModEnchantment(enchantment)) {
            ModEnchantmentHelper.applyCustomBookModelIfModEnchanted((ItemStack) (Object) this);
        }
    }

    @Inject(method = "set(Lnet/minecraft/core/component/DataComponentType;Ljava/lang/Object;)Ljava/lang/Object;", at = @At("TAIL"))
    private <T> void libri$applyCustomBookModelOnSet(DataComponentType<T> type, T value, CallbackInfoReturnable<T> cir) {
        if (type == DataComponents.STORED_ENCHANTMENTS || type == DataComponents.ENCHANTMENTS) {
            ModEnchantmentHelper.applyCustomBookModelIfModEnchanted((ItemStack) (Object) this);
        }
    }

    @Inject(method = "applyComponents(Lnet/minecraft/core/component/DataComponentPatch;)V", at = @At("TAIL"))
    private void libri$applyCustomBookModelOnApplyPatch(DataComponentPatch patch, CallbackInfo ci) {
        ModEnchantmentHelper.applyCustomBookModelIfModEnchanted((ItemStack) (Object) this);
    }

    @Inject(method = "applyComponents(Lnet/minecraft/core/component/DataComponentMap;)V", at = @At("TAIL"))
    private void libri$applyCustomBookModelOnApplyMap(DataComponentMap map, CallbackInfo ci) {
        ModEnchantmentHelper.applyCustomBookModelIfModEnchanted((ItemStack) (Object) this);
    }

    @Inject(method = "getStyledHoverName", at = @At("RETURN"), cancellable = true)
    private void libri$colorCustomBookNameGold(CallbackInfoReturnable<Component> cir) {
        ItemStack stack = (ItemStack) (Object) this;
        if (stack.is(Items.ENCHANTED_BOOK) && ModEnchantmentHelper.hasModEnchantment(stack)) {
            MutableComponent goldenName = Component.empty().append(stack.getHoverName()).withStyle(ChatFormatting.GOLD);
            if (stack.has(DataComponents.CUSTOM_NAME)) {
                goldenName.withStyle(ChatFormatting.ITALIC);
            }
            cir.setReturnValue(goldenName);
        }
    }
}
