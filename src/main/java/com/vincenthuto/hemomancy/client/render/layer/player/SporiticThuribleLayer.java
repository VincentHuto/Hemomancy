package com.vincenthuto.hemomancy.client.render.layer.player;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.vincenthuto.hemomancy.client.model.item.SporiticThuribleModel;
import com.vincenthuto.hemomancy.client.render.item.SporiticThuribleRenderHelper;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.SporiticThuribleItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class SporiticThuribleLayer<T extends LivingEntity, M extends HumanoidModel<T>> extends RenderLayer<T, M> {
	private final SporiticThuribleModel<T> model;

	public SporiticThuribleLayer(LivingEntityRenderer<T, M> owner) {
		super(owner);
		this.model = new SporiticThuribleModel<>(
				Minecraft.getInstance().getEntityModels().bakeLayer(SporiticThuribleModel.LAYER_LOCATION));
	}

	@Override
	public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, T entity, float limbSwing,
			float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
		if (!(entity instanceof Player player)) {
			return;
		}
		ItemStack offhand = player.getOffhandItem();
		if (!(offhand.getItem() instanceof SporiticThuribleItem)) {
			return;
		}

		HumanoidArm offhandArm = player.getMainArm().getOpposite();
		poseStack.pushPose();
		getParentModel().translateToHand(offhandArm, poseStack);
		float side = offhandArm == HumanoidArm.LEFT ? -1.0f : 1.0f;
		poseStack.translate(side * 0.08f, 0.62f, -0.10f);
		poseStack.mulPose(Axis.ZP.rotationDegrees(side * 4.0f));
		poseStack.scale(0.78f, 0.78f, 0.78f);
		SporiticThuribleRenderHelper.renderHeld(model, player, offhandArm, offhand, poseStack, buffer,
				packedLight, OverlayTexture.NO_OVERLAY, false);
		poseStack.popPose();
	}
}
