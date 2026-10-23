package com.vincenthuto.hemomancy.client.render.tile.harbinger.crafting;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.render.AnimationTime;
import com.vincenthuto.hemomancy.common.block.harbinger.crafting.ArmatureGeometry;
import com.vincenthuto.hemomancy.common.block.harbinger.crafting.HematicArmatureBlock;
import com.vincenthuto.hemomancy.common.station.StationTierProperty;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.HematicArmatureBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

/**
 * Draws what of the Hematic Armature lies outside the controller's own block model: the bowl
 * wings and upper frame sections, the Consecrated banners swaying under the beam, the Monolithic
 * heart, the stored blood in the side vials and heart, and the reagents floating over their bowls.
 * Sections and parts come from tools/model_export/hematic_armature.py, which shifts each into
 * vanilla's element bounds; the offsets below put them back. Anchors live in ArmatureGeometry.
 */
public class HematicArmatureRenderer implements BlockEntityRenderer<HematicArmatureBlockEntity> {
	public static final String[] PART_MODELS = {"west", "east", "upper", "consecrated_west", "consecrated_east",
			"consecrated_upper", "monolithic_west", "monolithic_east", "monolithic_upper", "banner",
			"banner_monolithic", "vial_blood", "heart_glass", "heart_blood"};

	private static final String[] STAGE_PREFIX = {"", "consecrated_", "monolithic_"};
	/** Wings were exported 16 px toward the controller, the upper frame and its parts 48 px down. */
	private static final float WING_SHIFT = 16F, UPPER_LIFT = 48F;

	public HematicArmatureRenderer(BlockEntityRendererProvider.Context context) {}

	@Override
	public void render(HematicArmatureBlockEntity armature, float partialTicks, PoseStack pose,
			MultiBufferSource buffers, int light, int overlay) {
		if (armature.getLevel() == null) return;
		Direction facing = armature.getBlockState().getValue(HematicArmatureBlock.FACING);
		int stage = StationTierProperty.stage(armature.getBlockState());
		Minecraft mc = Minecraft.getInstance();
		float time = AnimationTime.gameTime(armature.getLevel(), partialTicks);

		pose.pushPose();
		pose.translate(.5, 0, .5);
		pose.mulPose(Axis.YP.rotationDegrees(switch (facing) {
			case EAST -> -90; case SOUTH -> 180; case WEST -> 90; default -> 0;
		}));
		pose.translate(-.5, 0, -.5);
		renderFrame(mc, stage, time, pose, buffers, light, overlay);
		double max = armature.getMaxBloodVolume();
		float fill = max <= 0 ? 0 : (float) Math.min(1, armature.getBloodVolume() / max);
		if (fill > 0) renderBlood(mc, stage, fill, pose, buffers, light, overlay);
		pose.popPose();

		renderBowlItems(armature, mc, time, pose, buffers, light, overlay, itemRotationFor(facing));
	}

	/**
	 * Draws the sections and parts around the controller's block model in the north-facing frame:
	 * both wings, the upper frame and, once consecrated, the banners and Monolithic heart glass.
	 * Shared with the item renderer.
	 */
	public static void renderFrame(Minecraft mc, int stage, float time, PoseStack pose, MultiBufferSource buffers,
			int light, int overlay) {
		String prefix = STAGE_PREFIX[Mth.clamp(stage, 0, 2)];
		pose.pushPose();
		pose.translate(-WING_SHIFT / 16F, 0, 0);
		renderModel(mc, prefix + "west", pose, buffers, light, overlay, false);
		pose.translate(2 * WING_SHIFT / 16F, 0, 0);
		renderModel(mc, prefix + "east", pose, buffers, light, overlay, false);
		pose.popPose();

		pose.pushPose();
		pose.translate(0, UPPER_LIFT / 16F, 0);
		renderModel(mc, prefix + "upper", pose, buffers, light, overlay, false);
		if (stage == 2) renderModel(mc, "heart_glass", pose, buffers, light, overlay, true);
		pose.popPose();

		if (stage > 0) {
			// The banners hang still in a draught: a slow opposed sway and a slight flutter.
			float sway = Mth.sin(time * .08F) * 2.6F;
			float flutter = Mth.sin(time * .11F + .8F) * 1F;
			float[] hinge = {ArmatureGeometry.BANNER_X[0], ArmatureGeometry.BANNER_PIVOT[0], ArmatureGeometry.BANNER_PIVOT[1]};
			for (int k = 0; k < 2; k++) {
				pose.pushPose();
				pose.translate((ArmatureGeometry.BANNER_X[k] - ArmatureGeometry.BANNER_X[0]) / 16F, 0, 0);
				px(pose, hinge);
				pose.mulPose(Axis.ZP.rotationDegrees(k == 0 ? sway : -sway));
				pose.mulPose(Axis.XP.rotationDegrees(flutter));
				pxBack(pose, hinge);
				pose.translate(0, UPPER_LIFT / 16F, 0);
				renderModel(mc, stage == 1 ? "banner" : "banner_monolithic", pose, buffers, light, overlay, false);
				pose.popPose();
			}
		}
	}

	/** Stretches the unit-high fluid models from each vessel's floor to the stored level. */
	private static void renderBlood(Minecraft mc, int stage, float fill, PoseStack pose, MultiBufferSource buffers,
			int light, int overlay) {
		float[] vial = ArmatureGeometry.VIAL_FLUID;
		float[] floor = {ArmatureGeometry.VIAL_X[0], vial[1], ArmatureGeometry.VIAL_Z};
		for (float x : ArmatureGeometry.VIAL_X) {
			pose.pushPose();
			pose.translate((x - floor[0]) / 16F, 0, 0);
			px(pose, floor);
			pose.scale(1, (vial[2] - vial[1]) * fill, 1);
			pxBack(pose, floor);
			renderModel(mc, "vial_blood", pose, buffers, light, overlay, false);
			pose.popPose();
		}
		if (stage == 2) {
			float[] heart = ArmatureGeometry.HEART;
			float[] heartFloor = {heart[0], heart[3], heart[1]};
			pose.pushPose();
			px(pose, heartFloor);
			pose.scale(1, (heart[4] - heart[3]) * fill, 1);
			pxBack(pose, heartFloor);
			pose.translate(0, UPPER_LIFT / 16F, 0);
			renderModel(mc, "heart_blood", pose, buffers, light, overlay, false);
			pose.popPose();
		}
	}

	private static void renderBowlItems(HematicArmatureBlockEntity armature, Minecraft mc, float time, PoseStack pose,
			MultiBufferSource buffers, int light, int overlay, float yRot) {
		ItemRenderer itemRenderer = mc.getItemRenderer();
		for (int slot = HematicArmatureBlockEntity.SLOT_HEAD_REAGENT;
			 slot <= HematicArmatureBlockEntity.SLOT_FEET_REAGENT; slot++) {
			ItemStack stack = armature.getItem(slot);
			if (stack.isEmpty()) continue;
			double[] bowl = ArmatureGeometry.BOWLS[slot];
			float bob = Mth.sin((time + slot * 11.0F) * 0.12F) * 0.025F;
			pose.pushPose();
			pose.translate(0.5D, 0.0D, 0.5D);
			pose.mulPose(Axis.YP.rotationDegrees(yRot));
			pose.translate(bowl[0], bowl[1] + bob, bowl[2]);
			pose.mulPose(Axis.YP.rotationDegrees((time * 2.5F + slot * 47.0F) % 360.0F));
			pose.scale(0.38F, 0.38F, 0.38F);
			itemRenderer.renderStatic(stack, ItemDisplayContext.FIXED, light, overlay, pose, buffers,
					armature.getLevel(), slot);
			pose.popPose();
		}
	}

	/** Bowl anchors are south-facing offsets, turned the same way the block entity aims its particles. */
	private static float itemRotationFor(Direction facing) {
		return switch (facing) {
			case NORTH -> 180.0F;
			case EAST -> 90.0F;
			case WEST -> 270.0F;
			default -> 0.0F;
		};
	}

	private static void renderModel(Minecraft mc, String name, PoseStack pose, MultiBufferSource buffers, int light,
			int overlay, boolean translucent) {
		var model = mc.getModelManager().getModel(ModelResourceLocation.standalone(Hemomancy.rloc("block/hematic_armature_" + name)));
		var buffer = buffers.getBuffer(translucent ? RenderType.entityTranslucentCull(TextureAtlas.LOCATION_BLOCKS)
				: RenderType.entityCutoutNoCull(TextureAtlas.LOCATION_BLOCKS));
		mc.getBlockRenderer().getModelRenderer().renderModel(pose.last(), buffer, null, model, 1, 1, 1, light, overlay);
	}

	private static void px(PoseStack pose, float[] at) { pose.translate(at[0] / 16F, at[1] / 16F, at[2] / 16F); }
	private static void pxBack(PoseStack pose, float[] at) { pose.translate(-at[0] / 16F, -at[1] / 16F, -at[2] / 16F); }

	@Override
	public boolean shouldRenderOffScreen(HematicArmatureBlockEntity armature) {
		// The frame rises into the chunk section above the controller; keep drawing it from there.
		return true;
	}

	@Override
	public AABB getRenderBoundingBox(HematicArmatureBlockEntity armature) {
		return new AABB(armature.getBlockPos()).inflate(2, 0, 2).expandTowards(0, 4, 0);
	}
}
