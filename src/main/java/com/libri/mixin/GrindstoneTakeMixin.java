package com.libri.mixin;

import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.GrindstoneMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net/minecraft/screen/GrindstoneScreenHandler$4")
public class GrindstoneTakeMixin {

    @Shadow(remap = false) @Final
    private GrindstoneMenu field_16780;

    @Inject(method = "onTakeItem", at = @At("HEAD"), cancellable = true)
    private void preOnTakeItem(Player player, ItemStack stack, CallbackInfo ci) {
        GrindstoneMenu handler = this.field_16780;
        Container input = ((GrindstoneScreenHandlerAccessor) handler).libri$getInput();

        ItemStack slot0 = input.getItem(0);
        ItemStack slot1 = input.getItem(1);

        int enchSlot = -1;
        int bookSlot = -1;

        if (slot0.isEnchanted() && !slot0.is(Items.ENCHANTED_BOOK) && slot1.is(Items.BOOK) && !slot1.isEmpty()) {
            enchSlot = 0; bookSlot = 1;
        } else if (slot1.isEnchanted() && !slot1.is(Items.ENCHANTED_BOOK) && slot0.is(Items.BOOK) && !slot0.isEmpty()) {
            enchSlot = 1; bookSlot = 0;
        }

        if (enchSlot < 0) return;

        ItemStack enchantedItem = input.getItem(enchSlot);
        ItemStack bookStack = input.getItem(bookSlot);

        var enchantments = enchantedItem.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        var entries = enchantments.entrySet();
        var it = entries.iterator();
        if (!it.hasNext()) return;

        var topEntry = it.next();
        int level = topEntry.getIntValue();

        var newBuilder = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
        while (it.hasNext()) {
            var entry = it.next();
            newBuilder.upgrade(entry.getKey(), entry.getIntValue());
        }
        ItemEnchantments remainingEnch = newBuilder.toImmutable();

        enchantedItem.set(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        enchantedItem.set(DataComponents.REPAIR_COST, 0);
        input.setItem(enchSlot, enchantedItem);

        bookStack.shrink(1);
        if (bookStack.isEmpty()) {
            input.setItem(bookSlot, ItemStack.EMPTY);
        } else {
            input.setItem(bookSlot, bookStack);
        }

        for (var slot : handler.slots) {
            if (slot.container == input) {
                slot.setChanged();
            }
        }

        enchantedItem.set(DataComponents.ENCHANTMENTS, remainingEnch);
        enchantedItem.set(DataComponents.REPAIR_COST, 0);
        input.setItem(enchSlot, enchantedItem);
        for (var slot : handler.slots) {
            if (slot.container == input) {
                slot.setChanged();
            }
        }

        ContainerLevelAccess context = ((GrindstoneScreenHandlerAccessor) handler).libri$getContext();
        context.execute((world, pos) -> {
            if (world instanceof ServerLevel) {
                int xp = (int) Math.ceil(level / 2.0);
                player.giveExperiencePoints(xp);
            }
        });

        ci.cancel();
    }
}
