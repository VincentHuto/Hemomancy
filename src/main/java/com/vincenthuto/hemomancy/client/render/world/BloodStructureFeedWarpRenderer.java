package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.client.data.ActiveBloodStructureFeedClientData;
import com.vincenthuto.hemomancy.client.render.HemoRenderTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class BloodStructureFeedWarpRenderer {
	private static final float UV_SCALE = 0.37f;

	private BloodStructureFeedWarpRenderer() {
	}

	public static void render(PoseStack poseStack, float partialTick) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.level == null || ActiveBloodStructureFeedClientData.getActiveFeeds().isEmpty()) {
			return;
		}

		MultiBufferSource.BufferSource buffer = mc.renderBuffers().bufferSource();
		Vec3 camera = mc.gameRenderer.getMainCamera().getPosition();
		float time = mc.level.getGameTime() + partialTick;

		for (ActiveBloodStructureFeedClientData.FeedEntry feed : ActiveBloodStructureFeedClientData.getActiveFeeds()) {
			renderConnectedShell(feed, poseStack, buffer, camera, time);
		}
	}

	private static void renderConnectedShell(ActiveBloodStructureFeedClientData.FeedEntry feed,
			PoseStack poseStack, MultiBufferSource.BufferSource buffer, Vec3 camera, float time) {
		List<BlockPos> feedPositions = feed.getPositions();
		if (feedPositions.isEmpty()) {
			return;
		}

		Set<BlockPos> positions = new HashSet<>(feedPositions);
		BloodStructureFeedBounds bounds = BloodStructureFeedBounds.from(feedPositions);
		float progress = feed.getProgress();
		float wiggleAmp = 0.018f + progress * 0.042f;
		RenderType renderType = HemoRenderTypes.bloodStructureWarp(time, progress, bounds.seed(), wiggleAmp,
				bounds.centerX(camera), bounds.centerY(camera), bounds.centerZ(camera));
		VertexConsumer consumer = buffer.getBuffer(renderType);
		PoseStack.Pose pose = poseStack.last();

		for (Direction direction : Direction.values()) {
			emitExposedFaces(consumer, pose, direction, positions, camera);
		}

		buffer.endBatch(renderType);
	}

	private static void emitExposedFaces(VertexConsumer consumer, PoseStack.Pose pose, Direction direction,
			Set<BlockPos> positions, Vec3 camera) {
		for (BlockPos pos : positions) {
			if (isInternalFace(pos, direction, positions)) {
				continue;
			}
			emitUnitFace(consumer, pose, direction, pos, camera);
		}
	}

	private static boolean isInternalFace(BlockPos pos, Direction direction, Set<BlockPos> positions) {
		return positions.contains(pos.relative(direction));
	}

	private static void emitUnitFace(VertexConsumer consumer, PoseStack.Pose pose, Direction direction,
			BlockPos pos, Vec3 camera) {
		emitFace(consumer, pose, direction, facePlane(pos, direction), faceU(pos, direction),
				faceV(pos, direction), 1, 1, camera);
	}

	private static int facePlane(BlockPos pos, Direction direction) {
		return switch (direction) {
			case DOWN -> pos.getY();
			case UP -> pos.getY() + 1;
			case NORTH -> pos.getZ();
			case SOUTH -> pos.getZ() + 1;
			case WEST -> pos.getX();
			case EAST -> pos.getX() + 1;
		};
	}

	private static int faceU(BlockPos pos, Direction direction) {
		return switch (direction) {
			case DOWN, UP, NORTH, SOUTH -> pos.getX();
			case WEST, EAST -> pos.getZ();
		};
	}

	private static int faceV(BlockPos pos, Direction direction) {
		return switch (direction) {
			case DOWN, UP -> pos.getZ();
			case NORTH, SOUTH, WEST, EAST -> pos.getY();
		};
	}

	private static void emitFace(VertexConsumer consumer, PoseStack.Pose pose, Direction direction,
			int plane, int u0, int v0, int width, int height, Vec3 camera) {
		int u1 = u0 + width;
		int v1 = v0 + height;
		float texU0 = u0 * UV_SCALE;
		float texV0 = v0 * UV_SCALE;
		float texU1 = u1 * UV_SCALE;
		float texV1 = v1 * UV_SCALE;
		switch (direction) {
			case DOWN -> emitQuad(consumer, pose,
					x(u0, camera), y(plane, camera), z(v1, camera), texU0, texV1,
					x(u1, camera), y(plane, camera), z(v1, camera), texU1, texV1,
					x(u1, camera), y(plane, camera), z(v0, camera), texU1, texV0,
					x(u0, camera), y(plane, camera), z(v0, camera), texU0, texV0,
					direction);
			case UP -> emitQuad(consumer, pose,
					x(u0, camera), y(plane, camera), z(v0, camera), texU0, texV0,
					x(u1, camera), y(plane, camera), z(v0, camera), texU1, texV0,
					x(u1, camera), y(plane, camera), z(v1, camera), texU1, texV1,
					x(u0, camera), y(plane, camera), z(v1, camera), texU0, texV1,
					direction);
			case NORTH -> emitQuad(consumer, pose,
					x(u1, camera), y(v0, camera), z(plane, camera), texU1, texV0,
					x(u0, camera), y(v0, camera), z(plane, camera), texU0, texV0,
					x(u0, camera), y(v1, camera), z(plane, camera), texU0, texV1,
					x(u1, camera), y(v1, camera), z(plane, camera), texU1, texV1,
					direction);
			case SOUTH -> emitQuad(consumer, pose,
					x(u0, camera), y(v0, camera), z(plane, camera), texU0, texV0,
					x(u1, camera), y(v0, camera), z(plane, camera), texU1, texV0,
					x(u1, camera), y(v1, camera), z(plane, camera), texU1, texV1,
					x(u0, camera), y(v1, camera), z(plane, camera), texU0, texV1,
					direction);
			case WEST -> emitQuad(consumer, pose,
					x(plane, camera), y(v0, camera), z(u0, camera), texU0, texV0,
					x(plane, camera), y(v0, camera), z(u1, camera), texU1, texV0,
					x(plane, camera), y(v1, camera), z(u1, camera), texU1, texV1,
					x(plane, camera), y(v1, camera), z(u0, camera), texU0, texV1,
					direction);
			case EAST -> emitQuad(consumer, pose,
					x(plane, camera), y(v0, camera), z(u1, camera), texU1, texV0,
					x(plane, camera), y(v0, camera), z(u0, camera), texU0, texV0,
					x(plane, camera), y(v1, camera), z(u0, camera), texU0, texV1,
					x(plane, camera), y(v1, camera), z(u1, camera), texU1, texV1,
					direction);
		}
	}

	private static void emitQuad(VertexConsumer consumer, PoseStack.Pose pose,
			float x1, float y1, float z1, float u1, float v1,
			float x2, float y2, float z2, float u2, float v2,
			float x3, float y3, float z3, float u3, float v3,
			float x4, float y4, float z4, float u4, float v4,
			Direction direction) {
		float nx = direction.getStepX();
		float ny = direction.getStepY();
		float nz = direction.getStepZ();
		emitVertex(consumer, pose, x1, y1, z1, u1, v1, nx, ny, nz);
		emitVertex(consumer, pose, x2, y2, z2, u2, v2, nx, ny, nz);
		emitVertex(consumer, pose, x3, y3, z3, u3, v3, nx, ny, nz);
		emitVertex(consumer, pose, x4, y4, z4, u4, v4, nx, ny, nz);
	}

	private static void emitVertex(VertexConsumer consumer, PoseStack.Pose pose, float x, float y, float z,
			float u, float v, float nx, float ny, float nz) {
		consumer.addVertex(pose.pose(), x, y, z)
				.setColor(255, 255, 255, 255)
				.setUv(u, v)
				.setLight(LightTexture.FULL_BRIGHT)
				.setNormal(pose, nx, ny, nz);
	}

	private static float x(double worldX, Vec3 camera) {
		return (float) (worldX - camera.x);
	}

	private static float y(double worldY, Vec3 camera) {
		return (float) (worldY - camera.y);
	}

	private static float z(double worldZ, Vec3 camera) {
		return (float) (worldZ - camera.z);
	}

}
