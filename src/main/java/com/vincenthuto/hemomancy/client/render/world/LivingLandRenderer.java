package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.client.render.AnimationTime;
import com.vincenthuto.hemomancy.client.render.HemoRenderTypes;
import com.vincenthuto.hemomancy.common.entity.manipulation.LivingLandStrikeEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * The blood that holds Living Land's lifted ground (core-loop Track 9). Over every carried block, the infusion vein
 * net, fixed to the block as it moves; along the column, a cord of living blood from the socket it left to the head;
 * round the socket, a dark pool where the ground tore. The blocks themselves are drawn by the strike's own renderer.
 */
public final class LivingLandRenderer {
	private static final double RANGE = 64.0D;
	/** The vein net, white-tinted and opaque, fully spread. */
	private static final int VEIN_COLOR = 0xFFFFFFFF;
	private static final double CORD_WIDTH = 0.16D;
	/** The net sits just outside the drawn block. */
	private static final double SCALE = com.vincenthuto.hemomancy.client.render.entity.misc.LivingLandStrikeRenderer.BLOCK_SCALE + 0.01D;
	/** A raised block is the world's own, full size: its net sits a hair outside it. */
	private static final double RAISED_SCALE = 1.006D;
	/** The torn pool round a socket: a little wider than the block it lost. */
	private static final double POOL_SIZE = 1.1D;

	private LivingLandRenderer() {
	}

	public static void render(PoseStack poseStack, float partialTick) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.level == null) return;
		Vec3 camera = mc.gameRenderer.getMainCamera().getPosition();
		List<LivingLandStrikeEntity> strikes = new ArrayList<>();
		for (Entity entity : mc.level.entitiesForRendering()) {
			if (entity instanceof LivingLandStrikeEntity strike && strike.length() > 0
					&& strike.position().distanceToSqr(camera) < RANGE * RANGE) {
				strikes.add(strike);
			}
		}
		if (strikes.isEmpty()) return;
		float time = AnimationTime.gameTime(mc.level, partialTick);
		MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
		for (LivingLandStrikeEntity strike : strikes) veins(strike, poseStack, buffers, camera, time, partialTick);
		OutpouringBrushes.begin(time);
		for (LivingLandStrikeEntity strike : strikes) cord(strike, poseStack, camera, time, partialTick);
		OutpouringBrushes.flush(buffers);
	}

	/** The infusion vein net over each carried block's faces, spreading from the socket. */
	private static void veins(LivingLandStrikeEntity strike, PoseStack poseStack, MultiBufferSource.BufferSource buffers,
			Vec3 camera, float time, float partialTick) {
		Vec3 head = strike.getPosition(partialTick);
		Vec3 root = strike.root();
		Direction face = strike.face();
		double scale = strike.isRaise() ? RAISED_SCALE : SCALE;
		for (int i = 0; i < strike.length(); i++) {
			Vec3 centre = centre(strike, i, head);
			double half = 0.5D * scale;
			Vec3 corner = centre.subtract(half, half, half);
			float seed = (float) ((strike.getId() * 7 + i * 13) % 127);
			RenderType veins = HemoRenderTypes.bloodInfusionVeins(time, 1.0F, seed, 0.0F, 0.0F,
					(float) (root.x - camera.x), (float) (root.y - camera.y), (float) (root.z - camera.z),
					face.getStepX(), face.getStepY(), face.getStepZ(),
					(float) (corner.x - camera.x), (float) (corner.y - camera.y), (float) (corner.z - camera.z));
			// A unit cube at the block's own corner, shrunk about its centre to the size the strike draws it at:
			// emitFace places BlockPos.ZERO's faces relative to a camera moved back by the unshrunk corner.
			Vec3 relative = centre.subtract(camera);
			poseStack.pushPose();
			poseStack.translate(relative.x, relative.y, relative.z);
			poseStack.scale((float) scale, (float) scale, (float) scale);
			poseStack.translate(-relative.x, -relative.y, -relative.z);
			Vec3 shifted = camera.subtract(centre.subtract(0.5D, 0.5D, 0.5D));
			for (Direction side : Direction.values()) {
				BlockShellFaces.emitFace(buffers.getBuffer(veins), poseStack.last(), BlockPos.ZERO, side, shifted, VEIN_COLOR);
			}
			poseStack.popPose();
			buffers.endBatch(veins);
		}
	}

	/** A living-blood cord threaded through the column from its socket, and the torn pool round the socket. */
	private static void cord(LivingLandStrikeEntity strike, PoseStack poseStack, Vec3 camera, float time, float partialTick) {
		Vec3 head = strike.getPosition(partialTick);
		Vec3 root = strike.root();
		Direction face = strike.face();
		int seed = strike.getId() * 31;
		List<Vec3> path = new ArrayList<>();
		path.add(root.subtract(camera));
		for (int i = strike.length() - 1; i >= 0; i--) {
			Vec3 centre = centre(strike, i, head);
			if (centre.subtract(root).dot(Vec3.atLowerCornerOf(face.getNormal())) > 0.0D) path.add(centre.subtract(camera));
		}
		if (path.size() >= 2) {
			OutpouringBrushes.ribbonPath(poseStack, OutpouringBrushes.flow(OutpouringBrushes.Mat.ANIMUS), path, CORD_WIDTH * 1.4D,
					CORD_WIDTH, OutpouringBrushes.RIBBON, seed, 0xFFFFFF, 1.0F);
			for (int i = 1; i < path.size(); i++) {
				OutpouringBrushes.sinew(poseStack, path.get(i - 1), path.get(i), CORD_WIDTH * 0.6D, 0.06D, time, seed + i,
						OutpouringBrushes.BLOOD, 0.95F);
			}
		}
		Vec3 normal = Vec3.atLowerCornerOf(face.getNormal());
		Vec3 right = Math.abs(normal.y) > 0.5D ? new Vec3(1.0D, 0.0D, 0.0D) : normal.cross(new Vec3(0.0D, 1.0D, 0.0D)).normalize();
		Vec3 up = normal.cross(right).normalize();
		OutpouringBrushes.card(poseStack, OutpouringBrushes.flow(OutpouringBrushes.Mat.ANIMUS),
				root.add(normal.scale(0.02D)).subtract(camera), right, up, POOL_SIZE, POOL_SIZE, OutpouringBrushes.POOL, seed,
				0xFFFFFF, 0.6F);
	}

	/**
	 * Where block {@code index} stands: along the strike's curve, or for a raise's marker, stacked out of the patch
	 * face (the raised blocks are real; the marker only draws the blood on them).
	 */
	private static Vec3 centre(LivingLandStrikeEntity strike, int index, Vec3 head) {
		if (!strike.isRaise()) return strike.blockCentre(index, head);
		// The root is the patch's face; for a raise the column runs back from the outermost block to the face.
		int out = strike.length() - index;
		return strike.root().add(Vec3.atLowerCornerOf(strike.face().getNormal()).scale(out - 0.5D));
	}
}
