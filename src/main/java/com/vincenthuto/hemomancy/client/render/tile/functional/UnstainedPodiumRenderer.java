package com.vincenthuto.hemomancy.client.render.tile.functional;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.tile.functional.UnstainedPodiumBlockEntity;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.properties.DirectionProperty;

public class UnstainedPodiumRenderer implements BlockEntityRenderer<UnstainedPodiumBlockEntity> {
	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

	public UnstainedPodiumRenderer(BlockEntityRendererProvider.Context p_173636_) {
	}

	@Override
	public void render(UnstainedPodiumBlockEntity te, float partialTicks, PoseStack matrixStackIn,
			MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {
		ResourceLocation GLASSTEXTURE = Hemomancy.rloc("textures/block/sanguine_tran_pane.png");

//		matrixStackIn.pushPose();
//		Level world = te.getLevel();
//		VertexConsumer builder = bufferIn.getBuffer(RenderType.entityTranslucent(GLASSTEXTURE));
//
//		float yScale = 1f;
//		float xScale = 1f;
//		float zScale = 1f;
//
//		int r=1,g=1,b=1;
//		builder.addVertex(matrixStackIn.last().pose(), 0, yScale, 0).setColor(r, g, b, 255).setUv(1, 1)
//				.overlayCoords(OverlayTexture.NO_OVERLAY).uv2(combinedLightIn)
//				.normal(matrixStackIn.last().normal(), 0, 1, 0);
//		builder.addVertex(matrixStackIn.last().pose(), xScale, yScale, 0).setColor(r, g, b, 255).setUv(1, 0)
//				.overlayCoords(OverlayTexture.NO_OVERLAY).uv2(combinedLightIn)
//				.normal(matrixStackIn.last().normal(), 0, 1, 0);
//		builder.addVertex(matrixStackIn.last().pose(), xScale, yScale, -zScale).setColor(r, g, b, 255).setUv(0, 0)
//				.overlayCoords(OverlayTexture.NO_OVERLAY).uv2(combinedLightIn)
//				.normal(matrixStackIn.last().normal(), 0, 1, 0);
//		builder.addVertex(matrixStackIn.last().pose(), 0, yScale, -zScale).setColor(r, g, b, 255).setUv(0, 1)
//				.overlayCoords(OverlayTexture.NO_OVERLAY).uv2(combinedLightIn)
//				.normal(matrixStackIn.last().normal(), 0, 1, 0);
//
//		matrixStackIn.popPose();

	}

}
