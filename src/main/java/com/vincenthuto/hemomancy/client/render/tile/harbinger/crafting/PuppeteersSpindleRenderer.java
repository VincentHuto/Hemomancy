package com.vincenthuto.hemomancy.client.render.tile.harbinger.crafting;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.tile.crafting.PuppeteersSpindleModel;
import com.vincenthuto.hemomancy.common.block.harbinger.crafting.PuppeteersSpindleBlock;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.PuppeteersSpindleBlockEntity;
import com.vincenthuto.hutoslib.math.Vector3;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;

public class PuppeteersSpindleRenderer implements BlockEntityRenderer<PuppeteersSpindleBlockEntity> {
	public static final ResourceLocation TEXTURE =
			Hemomancy.rloc("textures/block/polished_venous_stone_bricks.png");

	private final PuppeteersSpindleModel model;

	public PuppeteersSpindleRenderer(BlockEntityRendererProvider.Context context) {
		this.model = new PuppeteersSpindleModel(context.bakeLayer(PuppeteersSpindleModel.LAYER_LOCATION));
	}

	@Override
	public boolean shouldRenderOffScreen(PuppeteersSpindleBlockEntity spindle) {
		return true;
	}

	@Override
	public void render(PuppeteersSpindleBlockEntity spindle, float partialTicks, PoseStack poseStack,
			MultiBufferSource buffer, int combinedLight, int combinedOverlay) {
		poseStack.pushPose();
		poseStack.translate(0.5D, -0D, 0.5D);
		poseStack.mulPose(Vector3.XP.rotationDegrees(180.0F).toMoj());

		Direction facing = spindle.getBlockState().getValue(PuppeteersSpindleBlock.FACING);
		float yRot = switch (facing) {
			case NORTH -> 180.0F;
			case EAST -> 270.0F;
			case SOUTH -> 0.0F;
			case WEST -> 90.0F;
			default -> 0.0F;
		};
		poseStack.mulPose(Vector3.YP.rotationDegrees(yRot).toMoj());

		float ageInTicks = partialTicks;
		if (spindle.getLevel() != null) {
			ageInTicks += spindle.getLevel().getGameTime();
		}
		this.model.setupAnim(ageInTicks);

		VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));
		this.model.renderToBuffer(poseStack, vertexConsumer, combinedLight, OverlayTexture.NO_OVERLAY, -1);
		poseStack.popPose();
	}
}
