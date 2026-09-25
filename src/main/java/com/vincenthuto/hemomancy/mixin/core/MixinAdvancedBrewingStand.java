package com.vincenthuto.hemomancy.mixin.core;

import com.vincenthuto.hemomancy.common.init.DataComponentInit;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BrewingStandBlockEntity.class)
public abstract class MixinAdvancedBrewingStand {
    @Inject(method = "isBrewable", at = @At("HEAD"), cancellable = true, remap = false)
    private static void hemomancy$keepAdvancedPotionsOutOfStand(PotionBrewing brewing,
            NonNullList<ItemStack> slots, CallbackInfoReturnable<Boolean> callback) {
        for (int i = 0; i < Math.min(3, slots.size()); i++) {
            if (slots.get(i).has(DataComponentInit.ADVANCED_BREW.get())) {
                callback.setReturnValue(false);
                return;
            }
        }
    }
}
