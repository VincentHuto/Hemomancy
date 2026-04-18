package com.vincenthuto.hemomancy.client.render.tile.functional;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.tile.functional.HarbingerSaintSarcophagusModel;
import com.vincenthuto.hemomancy.common.tile.functional.SaintSarcophagusBlockEntity;
import com.vincenthuto.hutoslib.math.Vector3;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.properties.DirectionProperty;

public class SaintSarcophagusRenderer implements BlockEntityRenderer<SaintSarcophagusBlockEntity> {
	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
	public static final ResourceLocation TEXTURE = new ResourceLocation(Hemomancy.MOD_ID,
			"textures/entity/model_saint_sarcophagus.png");

	private final HarbingerSaintSarcophagusModel model;

	public SaintSarcophagusRenderer(BlockEntityRendererProvider.Context context) {
		this.model = new HarbingerSaintSarcophagusModel(context.bakeLayer(HarbingerSaintSarcophagusModel.LAYER_LOCATION));
	}

	@Override
	public boolean shouldRenderOffScreen(SaintSarcophagusBlockEntity te) {
		return true;
	}

	@Override
	public void render(SaintSarcophagusBlockEntity te, float partialTicks, PoseStack ms, MultiBufferSource bufferIn,
			int combinedLightIn, int combinedOverlayIn) {
		ms.pushPose();

		ms.translate(0.5D, 1.5D, 0.5D);
		ms.mulPose(Vector3.XP.rotationDegrees(180f).toMoj());

		Direction facing = te.getBlockState().getValue(FACING);
		float yRot = switch (facing) {
			case NORTH -> 180f;
			case EAST  -> 270f;
			case SOUTH -> 0f;
			case WEST  -> 90f;
			default    -> 0f;
		};
		ms.mulPose(Vector3.YP.rotationDegrees(yRot).toMoj());

		VertexConsumer vertexConsumer = bufferIn.getBuffer(RenderType.entityTranslucentCull(TEXTURE));
		model.renderToBuffer(ms, vertexConsumer, combinedLightIn, OverlayTexture.NO_OVERLAY,
				1.0F, 1.0F, 1.0F, 1.0F);

		ms.popPose();
	}
}
