package com.vincenthuto.hemomancy.render.tile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.event.ClientTickHandler;
import com.vincenthuto.hemomancy.model.block.CentrifugeArmsModel;
import com.vincenthuto.hemomancy.tile.VialCentrifugeBlockEntity;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.properties.DirectionProperty;

public class VialCentrifugeRenderer implements BlockEntityRenderer<VialCentrifugeBlockEntity> {
	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
	private final CentrifugeArmsModel arms;
	public static ResourceLocation texture = new ResourceLocation(Hemomancy.MOD_ID,
			"textures/entity/model_centrifuge_arms.png");

	public VialCentrifugeRenderer(BlockEntityRendererProvider.Context p_173636_) {
		arms = new CentrifugeArmsModel(p_173636_.bakeLayer(CentrifugeArmsModel.LAYER_LOCATION));
	}

	@Override
	public void render(VialCentrifugeBlockEntity te, float partialTicks, PoseStack matrixStackIn,
			MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {

		double ticks = ClientTickHandler.ticksInGame + ClientTickHandler.partialTicks - 1.3 * 0.14;
		matrixStackIn.pushPose();
		matrixStackIn.translate(0.5D, 1.95D, 0.5D);
		matrixStackIn.mulPose(new Quaternion(Vector3f.XN, 180, true));
		float currentTime = te.getLevel().getGameTime() + partialTicks;
		matrixStackIn.mulPose(Vector3f.YP.rotationDegrees((float) ticks / 2));
		arms.vial1.visible = true;
		MultiBufferSource.BufferSource irendertypebuffer$impl = MultiBufferSource
				.immediate(Tesselator.getInstance().getBuilder());
		VertexConsumer ivertexbuilder = irendertypebuffer$impl.getBuffer(arms.renderType(texture));
		arms.renderToBuffer(matrixStackIn, ivertexbuilder, combinedLightIn, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F,
				1.0F, 1.0F);
		irendertypebuffer$impl.endBatch();
		matrixStackIn.popPose();

	}
}
