package com.vincenthuto.hemomancy.client.render.tile.functional;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.tile.functional.EarthenVeinModel;
import com.vincenthuto.hemomancy.common.block.harbinger.functional.EarthenVeinBlock;
import com.vincenthuto.hemomancy.common.tile.functional.EarthenVeinBlockEntity;
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
	public static ResourceLocation texture = Hemomancy.rloc("textures/entity/earthen_vein/model_earthen_vein.png");

	private final EarthenVeinModel vein;

	private final EarthenVeinAnimContext animCtx = new EarthenVeinAnimContext(new AnimationState());

	public EarthenVeinRenderer(BlockEntityRendererProvider.Context p_173636_) {
		vein = new EarthenVeinModel(p_173636_.bakeLayer(EarthenVeinModel.LAYER_LOCATION));
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
				OverlayTexture.NO_OVERLAY, -1);
		pPoseStack.popPose();


	}

	public record EarthenVeinAnimContext(AnimationState state) {
	}

}

