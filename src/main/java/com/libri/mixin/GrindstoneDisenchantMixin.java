package com.libri.mixin;

import com.libri.enchantment.ModEnchantmentHelper;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.GrindstoneMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GrindstoneMenu.class)
public class GrindstoneDisenchantMixin {

    @Shadow @Final private Container repairSlots;
    @Shadow @Final private Container resultSlots;

    @Inject(method = "createResult", at = @At("HEAD"), cancellable = true)
    private void handleUpdateResult(CallbackInfo ci) {
        ItemStack slot0 = this.repairSlots.getItem(0);
        ItemStack slot1 = this.repairSlots.getItem(1);

        int enchSlot = -1;
        ItemStack enchanted = ItemStack.EMPTY;

        if (slot0.isEnchanted() && slot1.is(Items.BOOK) && !slot1.isEmpty()) {
            enchSlot = 0; enchanted = slot0;
        } else if (slot1.isEnchanted() && slot0.is(Items.BOOK) && !slot0.isEmpty()) {
            enchSlot = 1; enchanted = slot1;
        }

        if (enchSlot < 0) return;

        var enchantments = EnchantmentHelper.getEnchantmentsForCrafting(enchanted);
        var it = enchantments.entrySet().iterator();
        if (!it.hasNext()) return;

        var topEntry = it.next();

        boolean isModEnchant = ModEnchantmentHelper.isModEnchantment(topEntry.getKey());

        ItemStack book = new ItemStack(Items.ENCHANTED_BOOK);
        var bb = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
        bb.upgrade(topEntry.getKey(), topEntry.getIntValue());
        book.set(DataComponents.STORED_ENCHANTMENTS, bb.toImmutable());
        if (isModEnchant) {
            ModEnchantmentHelper.applyCustomBookModel(book);
        }

        this.resultSlots.setItem(0, book);
        ci.cancel();
    }
}
