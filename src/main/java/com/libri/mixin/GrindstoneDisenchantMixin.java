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
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GrindstoneScreenHandler.class)
public class GrindstoneDisenchantMixin {

    @Shadow @Final private Inventory input;
    @Shadow @Final private Inventory result;

    @Shadow public void sendContentUpdates() {}

    @Unique private boolean libri$processing = false;
    @Unique private boolean libri$disenchanting = false;
    @Unique private int libri$pendingSlot = -1;
    @Unique private ItemStack libri$pendingItem = ItemStack.EMPTY;

    @Inject(method = "updateResult", at = @At("HEAD"), cancellable = true)
    private void onUpdateResult(CallbackInfo ci) {
        if (libri$processing) {
            ci.cancel();
            return;
        }

        ItemStack slot0 = this.input.getStack(0);
        ItemStack slot1 = this.input.getStack(1);

        // Restore pending item if the slot was cleared by onTakeItem
        if (libri$pendingSlot >= 0 && this.input.getStack(libri$pendingSlot).isEmpty()) {
            libri$processing = true;
            this.input.setStack(libri$pendingSlot, libri$pendingItem);
            libri$processing = false;

            libri$pendingSlot = -1;
            libri$pendingItem = ItemStack.EMPTY;

            this.result.setStack(2, ItemStack.EMPTY);
            sendContentUpdates();
            ci.cancel();
            return;
        }

        // Custom recipe: enchanted item + book
        boolean hasRecipe = false;
        int enchantedSlot = -1;
        int bookSlot = -1;

        if (slot0.hasEnchantments() && !slot1.isEmpty() && slot1.isOf(Items.BOOK)) {
            hasRecipe = true; enchantedSlot = 0; bookSlot = 1;
        } else if (slot1.hasEnchantments() && !slot0.isEmpty() && slot0.isOf(Items.BOOK)) {
            hasRecipe = true; enchantedSlot = 1; bookSlot = 0;
        }

        if (hasRecipe) {
            var enchantments = EnchantmentHelper.getEnchantments(this.input.getStack(enchantedSlot));
            var it = enchantments.getEnchantmentEntries().iterator();
            if (it.hasNext()) {
                var topEntry = it.next();

                // Save modified item (remove top enchantment) for restore after onTakeItem clears it
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

                // Create enchanted book result
                ItemStack enchantedBook = new ItemStack(Items.ENCHANTED_BOOK);
                var bookBuilder = new ItemEnchantmentsComponent.Builder(ItemEnchantmentsComponent.DEFAULT);
                bookBuilder.add(topEntry.getKey(), topEntry.getIntValue());
                enchantedBook.set(DataComponentTypes.ENCHANTMENTS, bookBuilder.build());
                this.result.setStack(2, enchantedBook);

                // Pre-consume the book
                this.input.getStack(bookSlot).decrement(1);

                sendContentUpdates();
            }

            ci.cancel();
            return;
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
                this.result.setStack(2, ItemStack.EMPTY);
                sendContentUpdates();
                ci.cancel();
            }
        }
    }


}
