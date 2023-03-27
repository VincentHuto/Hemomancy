package com.vincenthuto.hemomancy.render.tile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.event.ClientTickHandler;
import com.vincenthuto.hemomancy.model.block.FloatingHeartModel;
import com.vincenthuto.hemomancy.tile.MortalDisplayBlockEntity;
import com.vincenthuto.hutoslib.math.Quaternion;
import com.vincenthuto.hutoslib.math.Vector3;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.properties.DirectionProperty;

public class MortalDisplayRenderer implements BlockEntityRenderer<MortalDisplayBlockEntity> {
	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
	public static ResourceLocation texture = new ResourceLocation(Hemomancy.MOD_ID,
			"textures/entity/model_floating_heart.png");
	private final FloatingHeartModel heart;

	public MortalDisplayRenderer(BlockEntityRendererProvider.Context p_173636_) {
		heart = new FloatingHeartModel(p_173636_.bakeLayer(FloatingHeartModel.mortal_display));
	}

	@Override
	public void render(MortalDisplayBlockEntity te, float partialTicks, PoseStack matrixStackIn,
			MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {

		double ticks = ClientTickHandler.ticksInGame + ClientTickHandler.partialTicks - 1.3 * 0.14;
		matrixStackIn.pushPose();
		matrixStackIn.translate(0.5D, 1.75D, 0.5D);
		matrixStackIn.mulPose(new Quaternion(Vector3.XN, 180, true).toMoj());
		float currentTime = te.getLevel().getGameTime() + partialTicks;
		matrixStackIn.translate(0D, (Math.sin(Math.PI * currentTime / 2 / 32) / 5) + 0.1D, 0D);
		matrixStackIn.mulPose(Vector3.YP.rotationDegrees((float) ticks / 2).toMoj());
		float scale = (float) Math.abs(Math.cos(currentTime * 0.045f) * 0.25f) + 0.4f;
		matrixStackIn.scale(scale, scale, scale);
		matrixStackIn.translate(0, -scale * 0.7f - 0.2f +.5, 0);
		MultiBufferSource.BufferSource irendertypebuffer$impl = MultiBufferSource
				.immediate(Tesselator.getInstance().getBuilder());
		VertexConsumer ivertexbuilder = irendertypebuffer$impl.getBuffer(heart.renderType(texture));
		heart.renderToBuffer(matrixStackIn, ivertexbuilder, combinedLightIn, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F,
				1.0F, 1.0F);
		irendertypebuffer$impl.endBatch();
		matrixStackIn.popPose();

	}
}
