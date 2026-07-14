package com.libri.mixin;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.GrindstoneScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.inventory.Inventory;

@Mixin(GrindstoneScreenHandler.class)
public class GrindstoneDisenchantMixin {

    @Shadow @Final private Inventory input;

    @Shadow @Final private Inventory result;

    @Shadow @Final private ScreenHandlerContext context;

    @Inject(method = "updateResult", at = @At("TAIL"))
    private void onUpdateResult(CallbackInfo ci) {
        ItemStack input0 = this.input.getStack(0);
        ItemStack input1 = this.input.getStack(1);

        boolean hasEnchanted = false;
        ItemStack enchantedItem = ItemStack.EMPTY;

        if (input0.hasEnchantments() && input1.isOf(Items.BOOK)) {
            enchantedItem = input0;
            hasEnchanted = true;
        } else if (input1.hasEnchantments() && input0.isOf(Items.BOOK)) {
            enchantedItem = input1;
            hasEnchanted = true;
        }

        if (!hasEnchanted) return;

        var baseEnch = EnchantmentHelper.getEnchantments(enchantedItem);
        if (baseEnch.isEmpty()) return;

        ItemStack enchantedBook = new ItemStack(Items.ENCHANTED_BOOK);
        var bookEnch = new ItemEnchantmentsComponent.Builder(ItemEnchantmentsComponent.DEFAULT);
        for (var entry : baseEnch.getEnchantmentEntries()) {
            bookEnch.add(entry.getKey(), entry.getIntValue());
        }
        enchantedBook.set(DataComponentTypes.ENCHANTMENTS, bookEnch.build());

        this.context.run((world, pos) -> {
            if (!world.isClient && !enchantedBook.isEmpty()) {
                world.spawnEntity(new ItemEntity(world, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, enchantedBook));
            }
        });
    }
}
