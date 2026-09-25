package com.vincenthuto.hemomancy.mixin.core;

import com.vincenthuto.hemomancy.common.brewing.AdvancedPotionUse;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PotionItem.class)
public abstract class MixinBoundPotion {
    @Inject(method = "finishUsingItem", at = @At("HEAD"), cancellable = true, remap = false)
    private void hemomancy$finishBoundPotion(ItemStack stack, Level level, LivingEntity entity,
                                             CallbackInfoReturnable<ItemStack> callback) {
        if (!AdvancedPotionUse.isVessel(stack)) return;
        callback.setReturnValue(entity instanceof ServerPlayer player
                ? AdvancedPotionUse.finishVessel(stack, player) : stack);
    }
}
