package com.vincenthuto.hemomancy.client.render.layer.player;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.client.model.armor.SilentArchonArmorModel;
import com.vincenthuto.hemomancy.client.morphling.MorphlingPlayerPartVisibility;
import com.vincenthuto.hemomancy.client.render.armor.SilentArchonArmorRenderHelper;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class SilentArchonArmorOverlayLayer<T extends LivingEntity, M extends HumanoidModel<T>>
		extends RenderLayer<T, M> {
	private static final EquipmentSlot[] ARMOR_SLOTS = {
			EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
	};
	private static final int OVERLAY_LIGHT = 0x00F000F0;

	public SilentArchonArmorOverlayLayer(LivingEntityRenderer<T, M> renderer) {
		super(renderer);
	}

	@Override
	public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, T entity,
			float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks,
			float netHeadYaw, float headPitch) {
		for (EquipmentSlot slot : ARMOR_SLOTS) {
			ItemStack stack = entity.getItemBySlot(slot);
			if (SilentArchonArmorRenderHelper.isSilentArchon(stack)) {
				renderSlot(poseStack, buffer, stack, slot);
			}
		}
	}

	private void renderSlot(PoseStack poseStack, MultiBufferSource buffer, ItemStack stack, EquipmentSlot slot) {
		SilentArchonArmorModel<T> model = SilentArchonArmorRenderHelper.modelForSlot(slot);
		model.setAllVisible(true);
		this.getParentModel().copyPropertiesTo(model);
		MorphlingPlayerPartVisibility.applyToArmorModel(model, slot);

		VertexConsumer consumer = buffer.getBuffer(SilentArchonArmorRenderHelper.overlayRenderType(stack, slot, false));
		poseStack.pushPose();
		poseStack.scale(SilentArchonArmorRenderHelper.OVERLAY_SCALE, SilentArchonArmorRenderHelper.OVERLAY_SCALE,
				SilentArchonArmorRenderHelper.OVERLAY_SCALE);
		model.renderToBuffer(poseStack, consumer, OVERLAY_LIGHT, OverlayTexture.NO_OVERLAY,
				SilentArchonArmorRenderHelper.OVERLAY_COLOR);
		poseStack.popPose();
	}
}
