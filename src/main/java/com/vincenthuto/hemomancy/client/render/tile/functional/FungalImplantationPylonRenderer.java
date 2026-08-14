package com.vincenthuto.hemomancy.client.render.tile.functional;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.tile.functional.FungalImplantationPylonModel;
import com.vincenthuto.hemomancy.common.tile.functional.FungalImplantationPylonBlockEntity;
import com.vincenthuto.hutoslib.math.Quaternion;
import com.vincenthuto.hutoslib.math.Vector3;

import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.properties.DirectionProperty;

public class FungalImplantationPylonRenderer implements BlockEntityRenderer<FungalImplantationPylonBlockEntity> {
	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
	public static ResourceLocation texture = Hemomancy.rloc("textures/entity/fungal_implantation_pylon/fungal_implantation_pylon.png");

	private final FungalImplantationPylonModel vein;

	private final FungalImplantationPylonAnimContext animCtx = new FungalImplantationPylonAnimContext(new AnimationState());

	public FungalImplantationPylonRenderer(BlockEntityRendererProvider.Context p_173636_) {
		vein = new FungalImplantationPylonModel(p_173636_.bakeLayer(FungalImplantationPylonModel.LAYER_LOCATION));
		animCtx.state.start(0);

	}

	@Override
	public void render(FungalImplantationPylonBlockEntity te, float partialTicks, PoseStack pPoseStack,
			MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {
		pPoseStack.pushPose();
		pPoseStack.translate(0.5, 1.51, 0.5);
		pPoseStack.mulPose(new Quaternion(Vector3.XN, 180, true).toMoj());

		vein.setupAnimation(te.getLevel(), partialTicks, animCtx);

		// The filler blocks at +1 and +2 above this block occlude the skylight, causing
		// combinedLightIn to be nearly zero (completely dark). Sample light from one
		// block above the top filler (+3) so the model is lit correctly.
		BlockPos abovePos = te.getBlockPos().above(3);
		int light = LevelRenderer.getLightColor(te.getLevel(), abovePos);

		vein.renderToBuffer(pPoseStack, bufferIn.getBuffer(vein.renderType(texture)), light,
				OverlayTexture.NO_OVERLAY, -1);
		pPoseStack.popPose();


	}

	public record FungalImplantationPylonAnimContext(AnimationState state) {
	}

}

