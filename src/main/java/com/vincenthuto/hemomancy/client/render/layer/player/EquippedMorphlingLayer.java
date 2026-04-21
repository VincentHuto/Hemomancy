package com.vincenthuto.hemomancy.client.render.layer.player;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/**
 * Renders the player's currently equipped morphling on their right arm
 * in 3rd-person view.
 */
public class EquippedMorphlingLayer<T extends LivingEntity, M extends HumanoidModel<T>> extends RenderLayer<T, M> {

	public EquippedMorphlingLayer(LivingEntityRenderer<T, M> renderer) {
		super(renderer);
	}

	@Override
	public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, T entity,
			float limbSwing, float limbSwingAmount, float partialTicks,
			float ageInTicks, float netHeadYaw, float headPitch) {

		if (!(entity instanceof Player player))
			return;

		HemoCapabilityAccess.getEquippedMorphling(player).ifPresent(cap -> {
			ItemStack morphling = cap.getEquippedMorphling();
			if (morphling.isEmpty())
				return;

			poseStack.pushPose();

			// Translate to the right arm
			this.getParentModel().rightArm.translateAndRotate(poseStack);

			// Position on the outer face of the right arm, roughly mid-forearm
			poseStack.translate(-0.0625F, 0.4375F, -0.125F);

			// Rotate to lay flat against the arm surface (facing outward)
			poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
			poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));

			// Scale down so the 16x16 item fits nicely on the arm
			float scale = 0.35F;
			poseStack.scale(scale, scale, scale);

			// Render the item as a flat icon
			net.minecraft.client.Minecraft.getInstance().getItemRenderer().renderStatic(
					morphling, ItemDisplayContext.FIXED,
					packedLight, OverlayTexture.NO_OVERLAY,
					poseStack, buffer, entity.level(), entity.getId());

			poseStack.popPose();
		});
	}
}
