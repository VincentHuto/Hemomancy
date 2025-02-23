package com.vincenthuto.hemomancy.client.render.tile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.block.EarthenVeinModel;
import com.vincenthuto.hemomancy.client.model.block.FloatingEyeModel;
import com.vincenthuto.hemomancy.common.block.EarthenVeinBlock;
import com.vincenthuto.hemomancy.common.tile.EarthenVeinBlockEntity;
import com.vincenthuto.hutoslib.math.Quaternion;
import com.vincenthuto.hutoslib.math.Vector3;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.properties.DirectionProperty;

public class EarthenVeinRenderer implements BlockEntityRenderer<EarthenVeinBlockEntity> {
	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
	public static ResourceLocation texture = new ResourceLocation(Hemomancy.MOD_ID,
			"textures/entity/earthen_vein/model_earthen_vein.png");
	public static ResourceLocation eyeTexture = new ResourceLocation(Hemomancy.MOD_ID,
			"textures/entity/floating_eye/model_floating_eye.png");

	private final EarthenVeinModel vein;
	private final FloatingEyeModel eye;

	private final EarthenVeinAnimContext animCtx = new EarthenVeinAnimContext(new AnimationState());

	public EarthenVeinRenderer(BlockEntityRendererProvider.Context p_173636_) {
		vein = new EarthenVeinModel(p_173636_.bakeLayer(EarthenVeinModel.LAYER_LOCATION));
		eye = new FloatingEyeModel(p_173636_.bakeLayer(FloatingEyeModel.LAYER_LOCATION));
		animCtx.state.start(0);

	}

	@Override
	public void render(EarthenVeinBlockEntity te, float partialTicks, PoseStack pPoseStack,
			MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {
		pPoseStack.pushPose();
		pPoseStack.translate(0.5, 1.51, 0.5);
		pPoseStack.mulPose(new Quaternion(Vector3.XN, 180, true).toMoj());
 
		vein.setupAnimation(te.getLevel(), partialTicks, animCtx);
		Boolean stented = te.getBlockState().getValue(EarthenVeinBlock.STENTED);
		Boolean named = te.getBlockState().getValue(EarthenVeinBlock.NAMED);

		vein.getRoot().getChild("stent").visible =stented;
		vein.getRoot().getChild("stent").getChild("nametag").visible =named;

		vein.renderToBuffer(pPoseStack, bufferIn.getBuffer(vein.renderType(texture)), combinedLightIn,
				OverlayTexture.NO_OVERLAY, 1F, 1F, 1F, 1F);
		pPoseStack.popPose();
//
//		pPoseStack.pushPose();
//		
//		pPoseStack.translate(0.75, 0.5, 0.25);
//		pPoseStack.scale(0.5f, 0.5f, 0.5f);
//		float f = te.time + partialTicks;
//		pPoseStack.translate(0.0D, 0.1F + Mth.sin(f * 0.1F) * 0.01F, 0.0D);
//
//		float f1;
//		for (f1 = te.rot - te.oRot; f1 >= (float) Math.PI; f1 -= ((float) Math.PI * 2F)) {
//		}
//		
//
//
//		while (f1 < -(float) Math.PI) {
//			f1 += ((float) Math.PI * 2F);
//		}
//		float f2 = te.oRot + f1 * partialTicks;
//		pPoseStack.mulPose(Vector3.YP.rotation(-f2 + 55).toMoj());
//
//		float f3 = Mth.lerp(partialTicks, te.oFlip, te.flip);
//		float f4 = Mth.frac(f3 + 0.25F) * 1.6F - 0.3F;
//		float f5 = Mth.frac(f3 + 0.75F) * 1.6F - 0.3F;
//		float f6 = Mth.lerp(partialTicks, te.oOpen, te.open);
//		eye.setupAnim(f, Mth.clamp(f4, 0.0F, 1.0F), Mth.clamp(f5, 0.0F, 1.0F), f6);
//
//		eye.renderToBuffer(pPoseStack, bufferIn.getBuffer(vein.renderType(eyeTexture)), combinedLightIn,
//				OverlayTexture.NO_OVERLAY, 1F, 1F, 1F, 1F);
//		pPoseStack.popPose();


	}

	public record EarthenVeinAnimContext(AnimationState state) {
	}

}
