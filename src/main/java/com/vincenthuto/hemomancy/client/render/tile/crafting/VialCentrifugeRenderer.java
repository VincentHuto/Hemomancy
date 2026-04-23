package com.vincenthuto.hemomancy.client.render.tile.crafting;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.tile.crafting.CentrifugeArmsModel;
import com.vincenthuto.hemomancy.client.model.tile.crafting.CentrifugeStandModel;
import com.vincenthuto.hemomancy.common.tile.crafting.VialCentrifugeBlockEntity;
import com.vincenthuto.hutoslib.client.HlClientTickHandler;
import com.vincenthuto.hutoslib.math.Quaternion;
import com.vincenthuto.hutoslib.math.Vector3;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.properties.DirectionProperty;

public class VialCentrifugeRenderer implements BlockEntityRenderer<VialCentrifugeBlockEntity> {
	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
	public static ResourceLocation texture = Hemomancy.rloc("textures/entity/model_centrifuge_arms.png");

	public static ResourceLocation stand_texture = Hemomancy.rloc("textures/entity/model_centrifuge_stand.png");

	private final CentrifugeArmsModel arms;
	private final CentrifugeStandModel stand;

	public VialCentrifugeRenderer(BlockEntityRendererProvider.Context p_173636_) {
		arms = new CentrifugeArmsModel(p_173636_.bakeLayer(CentrifugeArmsModel.LAYER_LOCATION));
		stand = new CentrifugeStandModel(p_173636_.bakeLayer(CentrifugeStandModel.LAYER_LOCATION));
	}

	@Override
	public void render(VialCentrifugeBlockEntity te, float partialTicks, PoseStack matrixStackIn,
			MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {
		double ticks = HlClientTickHandler	.ticksInGame + HlClientTickHandler.partialTicks - 1.3 * 0.14;
		matrixStackIn.pushPose();
		matrixStackIn.translate(0.5D, 1.95D, 0.5D);
		matrixStackIn.mulPose(new Quaternion(Vector3.XN, 180, true).toMoj());
		float spinSpeed = (float) mapOneRangeToAnother(te.dataAccess.get(0), 0, 200, 0, 8, 10);
		float spinMod = spinSpeed < 1 && spinSpeed > 0 ? 0 : spinSpeed;
	//	System.out.println(spinMod);
		matrixStackIn.mulPose(Vector3.YP.rotationDegrees((float) ticks * spinMod).toMoj());
		// Displaying vials in slots
		arms.vial1.visible = !te.inventory.get(2).isEmpty() && te.inventory.get(2).has(DataComponents.CUSTOM_DATA);
		arms.vial1Empty.visible = !te.inventory.get(2).isEmpty() && !te.inventory.get(2).has(DataComponents.CUSTOM_DATA);

		arms.vial2.visible = !te.inventory.get(3).isEmpty() && te.inventory.get(3).has(DataComponents.CUSTOM_DATA);
		arms.vial2Empty.visible = !te.inventory.get(3).isEmpty() && !te.inventory.get(3).has(DataComponents.CUSTOM_DATA);

		arms.vial3.visible = !te.inventory.get(4).isEmpty() && te.inventory.get(4).has(DataComponents.CUSTOM_DATA);
		arms.vial3Empty.visible = !te.inventory.get(4).isEmpty() && !te.inventory.get(4).has(DataComponents.CUSTOM_DATA);

		arms.vial4.visible = !te.inventory.get(5).isEmpty() && te.inventory.get(5).has(DataComponents.CUSTOM_DATA);
		arms.vial4Empty.visible = !te.inventory.get(5).isEmpty() && !te.inventory.get(5).has(DataComponents.CUSTOM_DATA);

		arms.vial5.visible = !te.inventory.get(6).isEmpty() && te.inventory.get(6).has(DataComponents.CUSTOM_DATA);
		arms.vial5Empty.visible = !te.inventory.get(6).isEmpty() && !te.inventory.get(6).has(DataComponents.CUSTOM_DATA);

		arms.vial6.visible = !te.inventory.get(7).isEmpty() && te.inventory.get(7).has(DataComponents.CUSTOM_DATA);
		arms.vial6Empty.visible = !te.inventory.get(7).isEmpty() && !te.inventory.get(7).has(DataComponents.CUSTOM_DATA);

		arms.vial7.visible = !te.inventory.get(8).isEmpty() && te.inventory.get(8).has(DataComponents.CUSTOM_DATA);
		arms.vial7Empty.visible = !te.inventory.get(8).isEmpty() && !te.inventory.get(8).has(DataComponents.CUSTOM_DATA);

		arms.vial8.visible = !te.inventory.get(9).isEmpty() && te.inventory.get(9).has(DataComponents.CUSTOM_DATA);
		arms.vial8Empty.visible = !te.inventory.get(9).isEmpty() && !te.inventory.get(9).has(DataComponents.CUSTOM_DATA);

		// Render
		VertexConsumer vertexConsumer = bufferIn.getBuffer(RenderType.entityTranslucentCull(texture));
		arms.renderToBuffer(matrixStackIn, vertexConsumer, combinedLightIn, OverlayTexture.NO_OVERLAY, -1);
		matrixStackIn.popPose();


		matrixStackIn.pushPose();
		matrixStackIn.translate(0.5D, 1.5D, 0.5D);
		matrixStackIn.mulPose(new Quaternion(Vector3.XN, 180, true).toMoj());
		VertexConsumer standivertexbuilder = bufferIn.getBuffer(arms.renderType(texture));
		stand.renderToBuffer(matrixStackIn, standivertexbuilder, combinedLightIn, OverlayTexture.NO_OVERLAY, -1);
		matrixStackIn.popPose();

	}

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
}

