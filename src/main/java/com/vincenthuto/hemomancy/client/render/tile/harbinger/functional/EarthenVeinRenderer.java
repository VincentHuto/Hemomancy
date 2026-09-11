package com.vincenthuto.hemomancy.client.render.tile.harbinger.functional;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.tile.functional.EarthenVeinModel;
import com.vincenthuto.hemomancy.client.render.world.EarthenVeinFeedingTendrilRenderer;
import com.vincenthuto.hemomancy.client.vein.EarthenVeinTravelClientState;
import com.vincenthuto.hemomancy.common.block.harbinger.functional.EarthenVeinBlock;
import com.vincenthuto.hemomancy.common.tile.harbinger.functional.EarthenVeinBlockEntity;
import com.vincenthuto.hutoslib.math.Quaternion;
import com.vincenthuto.hutoslib.math.Vector3;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.properties.DirectionProperty;

public class EarthenVeinRenderer implements BlockEntityRenderer<EarthenVeinBlockEntity> {
	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
	public static ResourceLocation texture = Hemomancy.rloc("textures/entity/earthen_vein/model_earthen_vein.png");
	private static final float TEMPORARY_GROWTH_TICKS = 8.0F;
	private static final float TEMPORARY_MIN_SCALE = 0.15F;

	private final EarthenVeinModel vein;

	private final AnimationState animationState = new AnimationState();

	public EarthenVeinRenderer(BlockEntityRendererProvider.Context p_173636_) {
		vein = new EarthenVeinModel(p_173636_.bakeLayer(EarthenVeinModel.LAYER_LOCATION));
		animationState.start(0);

	}

	@Override
	public void render(EarthenVeinBlockEntity te, float partialTicks, PoseStack pPoseStack,
			MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {
		pPoseStack.pushPose();
		float growth = 1.0F;
		if (te.isTemporary()) {
			float progress = Mth.clamp((te.time + partialTicks) / TEMPORARY_GROWTH_TICKS, 0.0F, 1.0F);
			growth = TEMPORARY_MIN_SCALE + (1.0F - TEMPORARY_MIN_SCALE) * progress;
		}
		float mouth = 0.0F;
		var visual = EarthenVeinTravelClientState.visual(te.getLevel().dimension().location(), te.getBlockPos());
		if (visual != null) {
			switch (visual.phase()) {
				case FEEDING -> mouth = 0.18F + visual.progress() * 0.38F;
				case READY -> mouth = 0.82F;
				case SWALLOWING -> {
					mouth = 0.82F + visual.progress() * 0.18F;
					growth *= 1.0F + visual.progress() * 0.50F;
				}
				case EJECTING -> {
					mouth = 1.0F - visual.progress() * 0.55F;
					growth *= 1.5F - visual.progress() * 0.5F;
				}
				default -> { }
			}
			EarthenVeinFeedingTendrilRenderer.render(pPoseStack, bufferIn, te.getBlockPos(), visual,
					te.getLevel().getGameTime() + partialTicks);
		}
		pPoseStack.translate(0.5, 1.51D + (growth - 1.0F) * 1.5D, 0.5);
		pPoseStack.mulPose(new Quaternion(Vector3.XN, 180, true).toMoj());
		pPoseStack.scale(growth, growth, growth);
 
		vein.setupAnimation(te.getLevel(), partialTicks, new EarthenVeinAnimContext(animationState, mouth));
		Boolean stented = te.getBlockState().getValue(EarthenVeinBlock.STENTED);
		Boolean named = te.getBlockState().getValue(EarthenVeinBlock.NAMED);

		vein.getRoot().getChild("stent").visible =stented;
		vein.getRoot().getChild("stent").getChild("nametag").visible =named;

		vein.renderToBuffer(pPoseStack, bufferIn.getBuffer(vein.renderType(texture)), combinedLightIn,
				OverlayTexture.NO_OVERLAY, -1);
		pPoseStack.popPose();


	}

	public record EarthenVeinAnimContext(AnimationState state, float mouthProgress) {
	}

}

