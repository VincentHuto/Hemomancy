package com.vincenthuto.hemomancy.render.tile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.event.ClientTickHandler;
import com.vincenthuto.hemomancy.model.block.CentrifugeArmsModel;
import com.vincenthuto.hemomancy.tile.VialCentrifugeBlockEntity;
import com.vincenthuto.hutoslib.math.Quaternion;
import com.vincenthuto.hutoslib.math.Vector3;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.properties.DirectionProperty;

public class VialCentrifugeRenderer implements BlockEntityRenderer<VialCentrifugeBlockEntity> {
	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
	public static ResourceLocation texture = new ResourceLocation(Hemomancy.MOD_ID,
			"textures/entity/model_centrifuge_arms.png");
	public static double mapOneRangeToAnother(double sourceNumber, double fromA, double fromB, double toA, double toB,
			int decimalPrecision) {
		double deltaA = fromB - fromA;
		double deltaB = toB - toA;
		double scale = deltaB / deltaA;
		double negA = -1 * fromA;
		double offset = (negA * scale) + toA;
		double finalNumber = (sourceNumber * scale) + offset;
		int calcScale = (int) Math.pow(10, decimalPrecision);
		return finalNumber;
	}

	private final CentrifugeArmsModel arms;

	public VialCentrifugeRenderer(BlockEntityRendererProvider.Context p_173636_) {
		arms = new CentrifugeArmsModel(p_173636_.bakeLayer(CentrifugeArmsModel.LAYER_LOCATION));
	}

	@Override
	public void render(VialCentrifugeBlockEntity te, float partialTicks, PoseStack matrixStackIn,
			MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {
		double ticks = ClientTickHandler.ticksInGame + ClientTickHandler.partialTicks - 1.3 * 0.14;
		matrixStackIn.pushPose();
		matrixStackIn.translate(0.5D, 1.95D, 0.5D);
		matrixStackIn.mulPose(new Quaternion(Vector3.XN, 180, true).toMoj());
		float spinSpeed = (float) mapOneRangeToAnother(te.dataAccess.get(0), 0, 200, 0, 8, 10);
		float spinMod = spinSpeed < 1 && spinSpeed > 0 ? 0 : spinSpeed;
	//	System.out.println(spinMod);
		matrixStackIn.mulPose(Vector3.YP.rotationDegrees((float) ticks * spinMod).toMoj());
		// Displaying vials in slots
		arms.vial1.visible = !te.inventory.get(2).isEmpty() && te.inventory.get(2).hasTag();
		arms.vial1Empty.visible = !te.inventory.get(2).isEmpty() && !te.inventory.get(2).hasTag();

		arms.vial2.visible = !te.inventory.get(3).isEmpty() && te.inventory.get(3).hasTag();
		arms.vial2Empty.visible = !te.inventory.get(3).isEmpty() && !te.inventory.get(3).hasTag();

		arms.vial3.visible = !te.inventory.get(4).isEmpty() && te.inventory.get(4).hasTag();
		arms.vial3Empty.visible = !te.inventory.get(4).isEmpty() && !te.inventory.get(4).hasTag();

		arms.vial4.visible = !te.inventory.get(5).isEmpty() && te.inventory.get(5).hasTag();
		arms.vial4Empty.visible = !te.inventory.get(5).isEmpty() && !te.inventory.get(5).hasTag();

		arms.vial5.visible = !te.inventory.get(6).isEmpty() && te.inventory.get(6).hasTag();
		arms.vial5Empty.visible = !te.inventory.get(6).isEmpty() && !te.inventory.get(6).hasTag();

		arms.vial6.visible = !te.inventory.get(7).isEmpty() && te.inventory.get(7).hasTag();
		arms.vial6Empty.visible = !te.inventory.get(7).isEmpty() && !te.inventory.get(7).hasTag();

		arms.vial7.visible = !te.inventory.get(8).isEmpty() && te.inventory.get(8).hasTag();
		arms.vial7Empty.visible = !te.inventory.get(8).isEmpty() && !te.inventory.get(8).hasTag();

		arms.vial8.visible = !te.inventory.get(9).isEmpty() && te.inventory.get(9).hasTag();
		arms.vial8Empty.visible = !te.inventory.get(9).isEmpty() && !te.inventory.get(9).hasTag();

		// Render
		MultiBufferSource.BufferSource irendertypebuffer$impl = MultiBufferSource
				.immediate(Tesselator.getInstance().getBuilder());
		VertexConsumer ivertexbuilder = irendertypebuffer$impl.getBuffer(arms.renderType(texture));
		arms.renderToBuffer(matrixStackIn, ivertexbuilder, combinedLightIn, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F,
				1.0F);
		irendertypebuffer$impl.endBatch();
		matrixStackIn.popPose();

	}
}
