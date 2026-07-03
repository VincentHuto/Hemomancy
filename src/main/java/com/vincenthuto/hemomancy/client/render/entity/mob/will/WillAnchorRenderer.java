package com.vincenthuto.hemomancy.client.render.entity.mob.will;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.mob.monster.will.WillAnchorEntity;
import com.vincenthuto.hemomancy.common.item.harbinger.scar.fungal.OculifloraReticularisItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

public class WillAnchorRenderer extends EntityRenderer<WillAnchorEntity> {
	private static final ResourceLocation TEXTURE = Hemomancy.rloc("textures/entity/wretched_will/modelwretchedwill.png");

	public WillAnchorRenderer(EntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	public ResourceLocation getTextureLocation(WillAnchorEntity entity) {
		return TEXTURE;
	}

	@Override
	public void render(WillAnchorEntity entity, float entityYaw, float partialTicks,
			PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
		LocalPlayer player = Minecraft.getInstance().player;
		if (player == null || !OculifloraReticularisItem.networkSightActive(player)) {
			return;
		}

		poseStack.pushPose();
		float pulse = 0.5F + 0.5F * (float) Math.sin((entity.tickCount + partialTicks) * 0.22F);
		float radius = 0.55F + pulse * 0.18F;
		float height = 1.4F + pulse * 0.35F;
		int alpha = 105 + (int) (pulse * 95.0F);
		VertexConsumer consumer = bufferSource.getBuffer(RenderType.lines());
		Matrix4f matrix = poseStack.last().pose();
		line(consumer, matrix, -radius, 0.1F, 0.0F, radius, 0.1F, 0.0F, alpha);
		line(consumer, matrix, 0.0F, 0.1F, -radius, 0.0F, 0.1F, radius, alpha);
		line(consumer, matrix, 0.0F, 0.0F, 0.0F, 0.0F, height, 0.0F, alpha);
		poseStack.popPose();
		super.render(entity, entityYaw, partialTicks, poseStack, bufferSource, packedLight);
	}

	private static void line(VertexConsumer consumer, Matrix4f matrix, float x1, float y1, float z1,
			float x2, float y2, float z2, int alpha) {
		consumer.addVertex(matrix, x1, y1, z1).setColor(183, 72, 230, alpha);
		consumer.addVertex(matrix, x2, y2, z2).setColor(84, 220, 198, alpha);
	}
}
