package com.vincenthuto.hemomancy.client.render.tile.harbinger.functional;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Direction;

import java.util.List;

/**
 * The moving parts of the heart-node set pieces, drawn by their block entity renderers and by the moment ghosts:
 * the heart, the synapse's bulb, the cache's cap halves and the withered bodies. Each is a standalone model from its
 * generator in tools/model_export; the positions here mirror the constants named beside them there.
 */
public final class HeartNodeParts {
	private static final float PX = 1.0F / 16.0F;
	/** What every part draws in; a pass that draws parts outside a block entity ends this batch. */
	public static final RenderType CUTOUT = RenderType.entityCutoutNoCull(TextureAtlas.LOCATION_BLOCKS);

	public static final ModelResourceLocation HEART = model("heart_node_heart");
	public static final ModelResourceLocation HEART_WITHERED = model("heart_node_heart_withered");
	public static final ModelResourceLocation BULB = model("mycelial_synapse_bulb");
	public static final ModelResourceLocation LID_WEST = model("fruiting_body_cache_lid_west");
	public static final ModelResourceLocation LID_EAST = model("fruiting_body_cache_lid_east");
	public static final ModelResourceLocation CACHE_BODY = model("fruiting_body_cache");

	/** heart_node.py HEART_CENTRE, in blocks. */
	public static final float HEART_X = 8 * PX, HEART_Y = 4.4F * PX, HEART_Z = 8 * PX;
	/** mycelial_synapse.py BULB_CENTRE, in blocks, before the facing turn. */
	public static final float BULB_X = 8 * PX, BULB_Y = 11 * PX, BULB_Z = 8 * PX;
	/** fruiting_body_cache.py LID_HINGE_X and LID_HINGE_Y, in blocks. */
	public static final float LID_HINGE_WEST = 2.6F * PX, LID_HINGE_EAST = 13.4F * PX, LID_HINGE_Y = 5.6F * PX;

	private HeartNodeParts() {
	}

	private static ModelResourceLocation model(String name) {
		return ModelResourceLocation.standalone(Hemomancy.rloc("block/" + name));
	}

	/** The withered body of a node at {@code stage}, which only the withering ghost draws. */
	public static ModelResourceLocation witheredStage(int stage) {
		return model("heart_node_withered_stage_" + Math.max(0, Math.min(3, stage)));
	}

	/** Every part model, for registration. */
	public static List<ModelResourceLocation> all() {
		return List.of(HEART, HEART_WITHERED, BULB, LID_WEST, LID_EAST, CACHE_BODY, witheredStage(0), witheredStage(1),
				witheredStage(2), witheredStage(3));
	}

	/** Draws a part model in block space at the current pose. */
	public static void draw(PoseStack pose, MultiBufferSource buffers, ModelResourceLocation location, int light) {
		Minecraft mc = Minecraft.getInstance();
		mc.getBlockRenderer().getModelRenderer().renderModel(pose.last(), buffers.getBuffer(CUTOUT), null,
				mc.getModelManager().getModel(location), 1.0F, 1.0F, 1.0F, light, OverlayTexture.NO_OVERLAY);
	}

	/** Scales the pose by {@code scale} about a point given in blocks. */
	public static void scaleAbout(PoseStack pose, float x, float y, float z, float sx, float sy, float sz) {
		pose.translate(x, y, z);
		pose.scale(sx, sy, sz);
		pose.translate(-x, -y, -z);
	}

	/** Turns an up-pointing block model to {@code facing} about the block centre, as the synapse blockstate does. */
	public static void face(PoseStack pose, Direction facing) {
		pose.translate(0.5F, 0.5F, 0.5F);
		// The blockstate's x then y rotations, as a baked model applies them (both clockwise looking down the axis).
		switch (facing) {
			case DOWN -> pose.mulPose(Axis.XP.rotationDegrees(180));
			case NORTH -> pose.mulPose(Axis.XN.rotationDegrees(90));
			case SOUTH -> {
				pose.mulPose(Axis.YN.rotationDegrees(180));
				pose.mulPose(Axis.XN.rotationDegrees(90));
			}
			case EAST -> {
				pose.mulPose(Axis.YN.rotationDegrees(90));
				pose.mulPose(Axis.XN.rotationDegrees(90));
			}
			case WEST -> {
				pose.mulPose(Axis.YN.rotationDegrees(270));
				pose.mulPose(Axis.XN.rotationDegrees(90));
			}
			default -> {
			}
		}
		pose.translate(-0.5F, -0.5F, -0.5F);
	}

	/** {@code light} with its block light raised to at least {@code block} (0 to 15). */
	public static int lift(int light, float block) {
		int raised = Math.max(LightTexture.block(light), Math.min(15, Math.round(block)));
		return LightTexture.pack(raised, LightTexture.sky(light));
	}
}
