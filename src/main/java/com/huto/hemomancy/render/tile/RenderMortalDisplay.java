package com.huto.hemomancy.render.tile;

import com.huto.hemomancy.Hemomancy;
import com.huto.hemomancy.event.ClientTickHandler;
import com.huto.hemomancy.model.block.ModelFloatingHeart;
import com.huto.hemomancy.tile.BlockEntityMortalDisplay;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.properties.DirectionProperty;

public class RenderMortalDisplay implements BlockEntityRenderer<BlockEntityMortalDisplay> {
	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
	private final ModelFloatingHeart heart = new ModelFloatingHeart();
	public static ResourceLocation texture = new ResourceLocation(Hemomancy.MOD_ID,
			"textures/entity/model_floating_heart.png");

	public RenderMortalDisplay(BlockEntityRenderDispatcher rendererDispatcherIn) {
		super(rendererDispatcherIn);
	}

	@Override
	public void render(BlockEntityMortalDisplay te, float partialTicks, PoseStack matrixStackIn,
			MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {

		double ticks = ClientTickHandler.ticksInGame + ClientTickHandler.partialTicks - 1.3 * 0.14;
		matrixStackIn.pushPose();
		matrixStackIn.translate(0.5D, 1.2D, 0.5D);
		matrixStackIn.mulPose(new Quaternion(Vector3f.XN, 180, true));
		float currentTime = te.getLevel().getGameTime() + partialTicks;
		matrixStackIn.translate(0D, (Math.sin(Math.PI * currentTime / 2 / 32) / 5) + 0.1D, 0D);
		matrixStackIn.mulPose(Vector3f.YP.rotationDegrees((float) ticks / 2));
		float scale = (float) Math.abs(Math.cos(currentTime * 0.015f) * 0.25f) + 0.4f;
		matrixStackIn.scale(scale, scale, scale);
		matrixStackIn.translate(0, -scale * 0.7f - 0.2f, 0);
		MultiBufferSource.BufferSource irendertypebuffer$impl = MultiBufferSource
				.immediate(Tesselator.getInstance().getBuilder());
		VertexConsumer ivertexbuilder = irendertypebuffer$impl.getBuffer(heart.renderType(texture));
		heart.renderToBuffer(matrixStackIn, ivertexbuilder, combinedLightIn, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F,
				1.0F, 1.0F);
		irendertypebuffer$impl.endBatch();
		matrixStackIn.popPose();

	}
}
