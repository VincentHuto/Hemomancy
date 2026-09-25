package com.vincenthuto.hemomancy.mixin.core;

import com.vincenthuto.hemomancy.client.morphling.MorphlingPlayerPartVisibility;
import com.vincenthuto.hemomancy.common.init.DataComponentInit;
import com.vincenthuto.hemomancy.common.init.RenderTypeInit;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EquipmentSlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HumanoidArmorLayer.class)
public class MixinHumanoidArmorLayer {
	@ModifyVariable(method = "renderArmorPiece(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/EquipmentSlot;ILnet/minecraft/client/model/HumanoidModel;FFFFFF)V",
			at = @At("HEAD"), argsOnly = true, index = 2, remap = false)
	private MultiBufferSource hemomancy$crimsonWornArmor(MultiBufferSource original,
			com.mojang.blaze3d.vertex.PoseStack pose, MultiBufferSource buffer,
			LivingEntity wearer, EquipmentSlot slot) {
		if (!wearer.getItemBySlot(slot).has(DataComponentInit.SCRIPTORIUM_PROVENANCE.get())) return original;
		return type -> original.getBuffer(RenderTypeInit.scriptoriumGlint(type));
	}

    @Inject(method = "setPartVisibility", at = @At("RETURN"), remap = false)
    private void hemomancy$hideMorphlingReplacementArmorParts(HumanoidModel<?> model,
            EquipmentSlot slot, CallbackInfo ci) {
        MorphlingPlayerPartVisibility.applyToArmorModel(model, slot);
    }
}
