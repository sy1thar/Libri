package com.libri.mixin;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.GrindstoneScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net/minecraft/screen/GrindstoneScreenHandler$4")
public class GrindstoneTakeMixin {

    @Shadow(remap = false) @Final
    private GrindstoneScreenHandler field_16780;

    @Unique
    private ItemStack libri$savedTool = ItemStack.EMPTY;
    @Unique
    private ItemEnchantmentsComponent libri$savedRemainingEnch = ItemEnchantmentsComponent.DEFAULT;
    @Unique
    private int libri$savedEnchSlot = -1;
    @Unique
    private int libri$savedLevel = 0;

    @Inject(method = "onTakeItem", at = @At("HEAD"))
    private void preOnTakeItem(PlayerEntity player, ItemStack stack, CallbackInfo ci) {
        GrindstoneScreenHandler handler = this.field_16780;
        Inventory input = ((GrindstoneScreenHandlerAccessor) handler).libri$getInput();

        ItemStack slot0 = input.getStack(0);
        ItemStack slot1 = input.getStack(1);

        int enchSlot = -1;
        int bookSlot = -1;

        if (slot0.hasEnchantments() && !slot0.isOf(Items.ENCHANTED_BOOK) && slot1.isOf(Items.BOOK) && !slot1.isEmpty()) {
            enchSlot = 0; bookSlot = 1;
        } else if (slot1.hasEnchantments() && !slot1.isOf(Items.ENCHANTED_BOOK) && slot0.isOf(Items.BOOK) && !slot0.isEmpty()) {
            enchSlot = 1; bookSlot = 0;
        }

        if (enchSlot < 0) return;

        ItemStack enchantedItem = input.getStack(enchSlot).copy();
        ItemStack bookStack = input.getStack(bookSlot);

        var enchantments = enchantedItem.getOrDefault(DataComponentTypes.ENCHANTMENTS, ItemEnchantmentsComponent.DEFAULT);
        var entries = enchantments.getEnchantmentEntries();
        var it = entries.iterator();
        if (!it.hasNext()) return;

        var topEntry = it.next();
        int level = topEntry.getValue();

        var newBuilder = new ItemEnchantmentsComponent.Builder(ItemEnchantmentsComponent.DEFAULT);
        while (it.hasNext()) {
            var entry = it.next();
            newBuilder.add(entry.getKey(), entry.getIntValue());
        }
        ItemEnchantmentsComponent remainingEnch = newBuilder.build();

        this.libri$savedTool = enchantedItem;
        this.libri$savedRemainingEnch = remainingEnch;
        this.libri$savedEnchSlot = enchSlot;
        this.libri$savedLevel = level;

        enchantedItem.set(DataComponentTypes.ENCHANTMENTS, ItemEnchantmentsComponent.DEFAULT);
        enchantedItem.set(DataComponentTypes.REPAIR_COST, 0);
        input.setStack(enchSlot, enchantedItem);

        bookStack.decrement(1);
        if (bookStack.isEmpty()) {
            input.setStack(bookSlot, ItemStack.EMPTY);
        } else {
            input.setStack(bookSlot, bookStack);
        }

        for (var slot : handler.slots) {
            if (slot.inventory == input) {
                slot.markDirty();
            }
        }
    }

    @Inject(method = "onTakeItem", at = @At("RETURN"))
    private void postOnTakeItem(PlayerEntity player, ItemStack stack, CallbackInfo ci) {
        if (this.libri$savedEnchSlot < 0) return;

        GrindstoneScreenHandler handler = this.field_16780;
        Inventory input = ((GrindstoneScreenHandlerAccessor) handler).libri$getInput();

        this.libri$savedTool.set(DataComponentTypes.ENCHANTMENTS, this.libri$savedRemainingEnch);
        this.libri$savedTool.set(DataComponentTypes.REPAIR_COST, 0);
        input.setStack(this.libri$savedEnchSlot, this.libri$savedTool);
        for (var slot : handler.slots) {
            if (slot.inventory == input) {
                slot.markDirty();
            }
        }

        ScreenHandlerContext context = ((GrindstoneScreenHandlerAccessor) handler).libri$getContext();
        context.run((world, pos) -> {
            if (world instanceof ServerWorld sw) {
                int xp = (int) Math.ceil(this.libri$savedLevel / 2.0);
                player.addExperience(xp);
            }
        });

        this.libri$savedEnchSlot = -1;
    }
}
