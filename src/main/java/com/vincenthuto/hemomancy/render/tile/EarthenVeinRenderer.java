package com.vincenthuto.hemomancy.render.tile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.model.block.EarthenVeinModel;
import com.vincenthuto.hemomancy.tile.EarthenVeinBlockEntity;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.properties.DirectionProperty;

public class EarthenVeinRenderer implements BlockEntityRenderer<EarthenVeinBlockEntity> {
	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
	private final EarthenVeinModel vein;
	public static ResourceLocation texture = new ResourceLocation(Hemomancy.MOD_ID,
			"textures/entity/earthen_vein/model_earthen_vein.png");

	public EarthenVeinRenderer(BlockEntityRendererProvider.Context p_173636_) {
		vein = new EarthenVeinModel(p_173636_.bakeLayer(EarthenVeinModel.earth_vein));
	}

	@SuppressWarnings("unused")
	@Override
	public void render(EarthenVeinBlockEntity te, float partialTicks, PoseStack matrixStackIn,
			MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {
		matrixStackIn.pushPose();
		matrixStackIn.translate(0.5, -0.95, 0.5);
		MultiBufferSource.BufferSource irendertypebuffer$impl = MultiBufferSource
				.immediate(Tesselator.getInstance().getBuilder());
		VertexConsumer ivertexbuilder = irendertypebuffer$impl.getBuffer(vein.renderType(texture));
//		vein.renderToBuffer(matrixStackIn, ivertexbuilder, combinedLightIn, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F,
//				1.0F);
		irendertypebuffer$impl.endBatch();
		matrixStackIn.popPose();
	}
}
