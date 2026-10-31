package com.vincenthuto.hemomancy.client.render.entity.npc.court;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.vincenthuto.hemomancy.common.entity.npc.court.HydraRisingHead;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * Hydra's largest head, built from the Sanctum's own set pieces so it reads as the chimney heads come alive: a neck of
 * Kindled Tissue (the body's grown flesh) climbing out of the chimney mouth along a curve, and a Hydra Crown at its
 * end, scaled up, that turns over as the neck arches so its tentacles hang round the player it was called for.
 */
public class HydraRisingHeadRenderer extends EntityRenderer<HydraRisingHead> {
	/** Neck segments stand this far apart along the curve. */
	private static final double SPACING = 0.42D;
	private static final float NECK_ROOT = 1.15F;
	private static final float NECK_TIP = 0.7F;
	private static final float CROWN_SCALE = 2.2F;
	/** The neck starts this far down in the chimney, so it never shows a cut end at the mouth. */
	private static final double ROOT_DEPTH = 2.5D;

	private final BlockRenderDispatcher blocks;

	public HydraRisingHeadRenderer(EntityRendererProvider.Context context) {
		super(context);
		this.blocks = context.getBlockRenderDispatcher();
		this.shadowRadius = 0.0F;
	}

	@Override
	public void render(HydraRisingHead head, float yaw, float partialTick, PoseStack poseStack, MultiBufferSource buffers,
			int light) {
		float t = head.replayedAge(partialTick);
		Vec3 reach = head.reachTo();
		HydraRisingHead.Pose pose = HydraRisingHead.pose(t, reach);
		Vec3 crown = pose.crown();
		Vec3 root = new Vec3(0.0D, -ROOT_DEPTH, 0.0D);
		// The neck rises straight out of the chimney before it bends: the control point stands over the mouth.
		Vec3 control = new Vec3(0.0D, Math.max(crown.y, HydraRisingHead.RISE_HEIGHT * 0.6D) * (0.55D + 0.45D * pose.bend()), 0.0D);
		double length = root.distanceTo(control) + control.distanceTo(crown);
		int segments = Math.max(2, (int) (length / SPACING));
		BlockState tissue = BlockInit.kindled_tissue.get().defaultBlockState();
		for (int i = 0; i < segments; i++) {
			double u = i / (double) segments;
			Vec3 at = bezier(root, control, crown, u);
			float scale = NECK_ROOT + (NECK_TIP - NECK_ROOT) * (float) u;
			poseStack.pushPose();
			poseStack.translate(at.x, at.y, at.z);
			poseStack.mulPose(Axis.YP.rotationDegrees(i * 23.0F));
			poseStack.scale(scale, scale, scale);
			poseStack.translate(-0.5D, -0.5D, -0.5D);
			blocks.renderSingleBlock(tissue, poseStack, buffers, light, OverlayTexture.NO_OVERLAY);
			poseStack.popPose();
		}
		poseStack.pushPose();
		poseStack.translate(crown.x, crown.y, crown.z);
		float facing = (float) Math.toDegrees(Math.atan2(reach.x, reach.z));
		poseStack.mulPose(Axis.YP.rotationDegrees(facing));
		poseStack.mulPose(Axis.XP.rotationDegrees(180.0F * pose.turn()));
		poseStack.scale(CROWN_SCALE, CROWN_SCALE, CROWN_SCALE);
		poseStack.translate(-0.5D, -0.125D, -0.5D);
		blocks.renderSingleBlock(BlockInit.hydra_crown.get().defaultBlockState(), poseStack, buffers, light,
				OverlayTexture.NO_OVERLAY);
		poseStack.popPose();
		super.render(head, yaw, partialTick, poseStack, buffers, light);
	}

	private static Vec3 bezier(Vec3 a, Vec3 b, Vec3 c, double u) {
		double p = (1 - u) * (1 - u), q = 2 * (1 - u) * u, r = u * u;
		return a.scale(p).add(b.scale(q)).add(c.scale(r));
	}

	@Override
	public boolean shouldRender(HydraRisingHead head, Frustum frustum, double x, double y, double z) {
		// The entity is a point at the chimney mouth; what it draws reaches far past it.
		return head.shouldRenderAtSqrDistance(head.distanceToSqr(x, y, z));
	}

	@Override
	public ResourceLocation getTextureLocation(HydraRisingHead head) {
		return InventoryMenu.BLOCK_ATLAS;
	}
}
