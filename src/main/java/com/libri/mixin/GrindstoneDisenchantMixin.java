package com.libri.mixin;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.screen.GrindstoneScreenHandler;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GrindstoneScreenHandler.class)
public class GrindstoneDisenchantMixin {

    @Shadow @Final private Inventory input;
    @Shadow @Final private Inventory result;

    @Inject(method = "updateResult", at = @At("HEAD"), cancellable = true)
    private void handleUpdateResult(CallbackInfo ci) {
        ItemStack slot0 = this.input.getStack(0);
        ItemStack slot1 = this.input.getStack(1);

        int enchSlot = -1;
        ItemStack enchanted = ItemStack.EMPTY;

        if (slot0.hasEnchantments() && slot1.isOf(Items.BOOK) && !slot1.isEmpty()) {
            enchSlot = 0; enchanted = slot0;
        } else if (slot1.hasEnchantments() && slot0.isOf(Items.BOOK) && !slot0.isEmpty()) {
            enchSlot = 1; enchanted = slot1;
        }

        if (enchSlot < 0) return;

        var enchantments = EnchantmentHelper.get(enchanted);
        var it = enchantments.entrySet().iterator();
        if (!it.hasNext()) return;

        var topEntry = it.next();
        Identifier enchId = Registries.ENCHANTMENT.getId(topEntry.getKey());
        boolean isModEnchant = enchId != null && enchId.getNamespace().equals("libri");

        ItemStack book = EnchantedBookItem.forEnchantment(
                new EnchantmentLevelEntry(topEntry.getKey(), topEntry.getValue()));
        if (isModEnchant) {
            book.getOrCreateNbt().putInt("CustomModelData", 1);
        }

        this.result.setStack(0, book);
        ci.cancel();
    }
}
