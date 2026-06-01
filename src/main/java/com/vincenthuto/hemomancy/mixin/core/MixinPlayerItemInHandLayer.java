package com.vincenthuto.hemomancy.mixin.core;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.client.morphling.MorphlingPlayerPartVisibility;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.SporiticThuribleItem;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.LivingFlailItem;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.PlayerItemInHandLayer;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerItemInHandLayer.class)
public class MixinPlayerItemInHandLayer {

    @Inject(method = "renderArmWithItem", at = @At("HEAD"), cancellable = true, remap = false)
    private void hemomancy$hideHeldItemsForMorphlingReplacementParts(LivingEntity livingEntity,
            ItemStack itemStack, ItemDisplayContext displayContext, HumanoidArm arm,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, CallbackInfo ci) {
        if (MorphlingPlayerPartVisibility.shouldHideHeldItem(arm)
                || itemStack.is(Items.SPYGLASS) && MorphlingPlayerPartVisibility.isHeadHidden()
                || itemStack.getItem() instanceof SporiticThuribleItem
                || itemStack.getItem() instanceof LivingFlailItem) {
            ci.cancel();
        }
    }
}
