package com.libri.mixin;

import net.minecraft.component.DataComponentTypes;
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
    @Unique private boolean libri$disenchanting = false;
    @Unique private int libri$pendingSlot = -1;
    @Unique private ItemStack libri$pendingItem = ItemStack.EMPTY;

    @ModifyArg(method = "updateResult", at = @At(value = "INVOKE", target = "Lnet/minecraft/inventory/Inventory;setStack(ILnet/minecraft/item/ItemStack;)V"), index = 1)
    private ItemStack modifyResult(ItemStack original) {
        if (libri$processing) return original;

        ItemStack slot0 = this.input.getStack(0);
        ItemStack slot1 = this.input.getStack(1);

        // Restore pending item if the slot was cleared by onTakeItem
        if (libri$pendingSlot >= 0 && this.input.getStack(libri$pendingSlot).isEmpty()) {
            libri$processing = true;
            this.input.setStack(libri$pendingSlot, libri$pendingItem);
            libri$processing = false;

            libri$pendingSlot = -1;
            libri$pendingItem = ItemStack.EMPTY;

            return ItemStack.EMPTY;
        }

        // Custom recipe: enchanted item + book
        int enchantedSlot = -1;
        int bookSlot = -1;

        if (slot0.hasEnchantments() && !slot1.isEmpty() && slot1.isOf(Items.BOOK)) {
            enchantedSlot = 0; bookSlot = 1;
        } else if (slot1.hasEnchantments() && !slot0.isEmpty() && slot0.isOf(Items.BOOK)) {
            enchantedSlot = 1; bookSlot = 0;
        }

        if (enchantedSlot >= 0) {
            var enchantments = EnchantmentHelper.getEnchantments(this.input.getStack(enchantedSlot));
            var it = enchantments.getEnchantmentEntries().iterator();
            if (it.hasNext()) {
                var topEntry = it.next();

                libri$disenchanting = true;
                libri$pendingSlot = enchantedSlot;
                libri$pendingItem = this.input.getStack(enchantedSlot).copy();

                var itemBuilder = new ItemEnchantmentsComponent.Builder(ItemEnchantmentsComponent.DEFAULT);
                boolean skip = true;
                for (var entry : enchantments.getEnchantmentEntries()) {
                    if (skip) { skip = false; continue; }
                    itemBuilder.add(entry.getKey(), entry.getIntValue());
                }
                ItemEnchantmentsComponent newEnch = itemBuilder.build();
                if (newEnch.isEmpty()) {
                    libri$pendingItem.remove(DataComponentTypes.ENCHANTMENTS);
                } else {
                    libri$pendingItem.set(DataComponentTypes.ENCHANTMENTS, newEnch);
                }

                // Pre-consume the book
                this.input.getStack(bookSlot).decrement(1);

                // Return enchanted book
                ItemStack enchantedBook = new ItemStack(Items.ENCHANTED_BOOK);
                var bookBuilder = new ItemEnchantmentsComponent.Builder(ItemEnchantmentsComponent.DEFAULT);
                bookBuilder.add(topEntry.getKey(), topEntry.getIntValue());
                enchantedBook.set(DataComponentTypes.ENCHANTMENTS, bookBuilder.build());
                return enchantedBook;
            }
        }

        // Suppress vanilla result when our modified item is present without a book
        if (libri$disenchanting) {
            boolean hasModified = false;
            for (int i = 0; i < 2; i++) {
                if (!this.input.getStack(i).isEmpty()) {
                    hasModified = true;
                    break;
                }
            }
            if (!hasModified) {
                libri$disenchanting = false;
            } else {
                return ItemStack.EMPTY;
            }
        }

        return original;
    }
}
