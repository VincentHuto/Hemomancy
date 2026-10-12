package com.vincenthuto.hemomancy.client.render.tile.harbinger.functional;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.block.harbinger.functional.DendriticDistributorBlock;
import com.vincenthuto.hemomancy.common.tile.harbinger.functional.DendriticDistributorBlockEntity;
import com.vincenthuto.hemomancy.common.tile.harbinger.functional.DendriticSelectorRules;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.ModelResourceLocation;

/**
 * Turns the selector axon to the distributor's selected pattern and lights that bouton, with an impulse
 * running out along its dendrite. Part models are authored for the centre slot (pointing -z) in
 * tools/model_export/dendritic_distributor.py; HUB and the impulse run mirror that script.
 */
public class DendriticDistributorRenderer implements BlockEntityRenderer<DendriticDistributorBlockEntity> {
	public static final String[] PART_MODELS = { "selector", "bouton_lit", "impulse" };
	private static final ModelResourceLocation SELECTOR = part("selector");
	private static final ModelResourceLocation BOUTON_LIT = part("bouton_lit");
	private static final ModelResourceLocation IMPULSE = part("impulse");
	private static final float PX = 1 / 16F;
	private static final float HUB_X = 8 * PX;
	private static final float HUB_Z = 9 * PX;
	/** IMPULSE_END - IMPULSE_START in the authoring script. */
	private static final float IMPULSE_RUN = 3 * PX;

	public DendriticDistributorRenderer(BlockEntityRendererProvider.Context context) {
	}

	public static ModelResourceLocation part(String name) {
		return ModelResourceLocation.standalone(Hemomancy.rloc("block/dendritic_distributor_" + name));
	}

	@Override
	public void render(DendriticDistributorBlockEntity te, float partialTicks, PoseStack pose,
			MultiBufferSource buffers, int light, int overlay) {
		long time = te.getLevel() != null ? te.getLevel().getGameTime() : 0L;
		pose.pushPose();
		pose.translate(0.5D, 0.0D, 0.5D);
		// Matches the blockstate's y rotation of the north-facing model.
		pose.mulPose(Axis.YP.rotationDegrees(switch (te.getBlockState().getValue(DendriticDistributorBlock.FACING)) {
			case EAST -> -90;
			case SOUTH -> 180;
			case WEST -> 90;
			default -> 0;
		}));
		pose.translate(HUB_X - 0.5D, 0.0D, HUB_Z - 0.5D);

		pose.pushPose();
		pose.mulPose(Axis.YP.rotationDegrees(te.selectorDegrees(partialTicks)));
		pose.translate(-HUB_X, 0.0D, -HUB_Z);
		draw(pose, buffers, SELECTOR, 1.0F, light, overlay);
		pose.popPose();

		// The bouton only wakes once the axon has synapsed on it, so a sweep never lights the wrong lobe.
		if (te.selectorSettled()) {
			pose.mulPose(Axis.YP.rotationDegrees(te.targetSelectorDegrees()));
			pose.translate(-HUB_X, 0.0D, -HUB_Z);
			draw(pose, buffers, BOUTON_LIT, DendriticSelectorRules.glow(time, partialTicks), LightTexture.FULL_BRIGHT,
					overlay);
			float travel = DendriticSelectorRules.impulseProgress(time, partialTicks);
			if (travel >= 0.0F) {
				pose.translate(0.0D, 0.0D, -travel * IMPULSE_RUN);
				draw(pose, buffers, IMPULSE, 1.0F, LightTexture.FULL_BRIGHT, overlay);
			}
		}
		pose.popPose();
	}

	private static void draw(PoseStack pose, MultiBufferSource buffers, ModelResourceLocation location, float tint,
			int light, int overlay) {
		Minecraft mc = Minecraft.getInstance();
		mc.getBlockRenderer().getModelRenderer().renderModel(pose.last(),
				buffers.getBuffer(RenderType.entityCutoutNoCull(TextureAtlas.LOCATION_BLOCKS)), null,
				mc.getModelManager().getModel(location), tint, tint, tint, light, overlay);
	}
}
