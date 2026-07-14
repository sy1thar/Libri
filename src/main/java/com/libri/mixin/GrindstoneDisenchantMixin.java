package com.libri.mixin;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.CustomModelDataComponent;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.GrindstoneScreenHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(GrindstoneScreenHandler.class)
public class GrindstoneDisenchantMixin {

    @Shadow @Final private Inventory input;
    @Shadow @Final private Inventory result;

    @Unique private boolean libri$processing = false;

    @ModifyArg(method = "updateResult", at = @At(value = "INVOKE", target = "Lnet/minecraft/inventory/Inventory;setStack(ILnet/minecraft/item/ItemStack;)V"), index = 1)
    private ItemStack modifyResult(ItemStack original) {
        if (libri$processing) return original;

        ItemStack slot0 = this.input.getStack(0);
        ItemStack slot1 = this.input.getStack(1);

        int enchSlot = -1;
        int bookSlot = -1;
        ItemStack enchanted = ItemStack.EMPTY;

        if (slot0.hasEnchantments() && slot1.isOf(Items.BOOK) && !slot1.isEmpty()) {
            enchSlot = 0; bookSlot = 1; enchanted = slot0;
        } else if (slot1.hasEnchantments() && slot0.isOf(Items.BOOK) && !slot0.isEmpty()) {
            enchSlot = 1; bookSlot = 0; enchanted = slot1;
        }

        if (enchSlot < 0) return original;

        var enchantments = EnchantmentHelper.getEnchantments(enchanted);
        var it = enchantments.getEnchantmentEntries().iterator();
        if (!it.hasNext()) return original;

        var topEntry = it.next();

        // Create enchanted book result
        ItemStack book = new ItemStack(Items.ENCHANTED_BOOK);
        var bb = new ItemEnchantmentsComponent.Builder(ItemEnchantmentsComponent.DEFAULT);
        bb.add(topEntry.getKey(), topEntry.getIntValue());
        book.set(DataComponentTypes.STORED_ENCHANTMENTS, bb.build());
        book.set(DataComponentTypes.CUSTOM_MODEL_DATA, new CustomModelDataComponent(1));

        // Modify the enchanted item in-place (remove top enchant)
        libri$processing = true;
        var builder = new ItemEnchantmentsComponent.Builder(ItemEnchantmentsComponent.DEFAULT);
        boolean skip = true;
        for (var entry : enchantments.getEnchantmentEntries()) {
            if (skip) { skip = false; continue; }
            builder.add(entry.getKey(), entry.getIntValue());
        }
        ItemEnchantmentsComponent newEnch = builder.build();
        if (newEnch.isEmpty()) {
            enchanted.remove(DataComponentTypes.ENCHANTMENTS);
        } else {
            enchanted.set(DataComponentTypes.ENCHANTMENTS, newEnch);
        }
        this.input.setStack(enchSlot, enchanted);
        libri$processing = false;

        return book;
    }
}
