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
import org.joml.Vector3f;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class BloodStructureFeedWarpRenderer {
	private static final float UV_SCALE = 0.37f;
	private static final float FACE_OFFSET = 0.002f;

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
			renderConnectedShell(feed, poseStack, buffer, camera, time, partialTick);
		}
	}

	private static void renderConnectedShell(ActiveBloodStructureFeedClientData.FeedEntry feed,
			PoseStack poseStack, MultiBufferSource.BufferSource buffer, Vec3 camera, float time, float partialTick) {
		List<BlockPos> feedPositions = feed.getPositions();
		if (feedPositions.isEmpty()) {
			return;
		}
		int alpha = Math.round(feed.getFadeAlpha(partialTick) * 255.0f);
		if (alpha <= 0) {
			return;
		}

		Set<BlockPos> positions = new HashSet<>(feedPositions);
		BloodStructureFeedBounds bounds = BloodStructureFeedBounds.from(feedPositions);
		float progress = feed.getProgress();
		int tint = feed.getChannelId() == 0L ? 0xFFFFFF : rotColor(feed.getEngulfmentProgress(partialTick));
		int color = alpha << 24 | tint;
		float wiggleAmp = 0.018f + progress * 0.042f;
		RenderType renderType = HemoRenderTypes.bloodStructureWarp(time, progress, bounds.seed(), wiggleAmp,
				bounds.centerX(camera), bounds.centerY(camera), bounds.centerZ(camera),
				feed.getFinalizeProgress(), bounds.bottomY(camera), (float) bounds.height());
		VertexConsumer consumer = buffer.getBuffer(renderType);
		PoseStack.Pose pose = poseStack.last();

		for (Direction direction : Direction.values()) {
			emitExposedFaces(consumer, pose, direction, positions, camera, color);
		}

		buffer.endBatch(renderType);
	}

	private static void emitExposedFaces(VertexConsumer consumer, PoseStack.Pose pose, Direction direction,
			Set<BlockPos> positions, Vec3 camera, int color) {
		for (BlockPos pos : positions) {
			if (isInternalFace(pos, direction, positions)) {
				continue;
			}
			emitUnitFace(consumer, pose, direction, positions, pos, camera, color);
		}
	}

	private static boolean isInternalFace(BlockPos pos, Direction direction, Set<BlockPos> positions) {
		return positions.contains(pos.relative(direction));
	}

	private static void emitUnitFace(VertexConsumer consumer, PoseStack.Pose pose, Direction direction,
			Set<BlockPos> positions, BlockPos pos, Vec3 camera, int color) {
		emitFace(consumer, pose, direction, facePlane(pos, direction), faceU(pos, direction),
				faceV(pos, direction), 1, 1, positions, camera, color);
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
			int plane, int u0, int v0, int width, int height, Set<BlockPos> positions, Vec3 camera, int color) {
		int u1 = u0 + width;
		int v1 = v0 + height;
		float surface = plane + direction.getAxisDirection().getStep() * FACE_OFFSET;
		float texU0 = u0 * UV_SCALE;
		float texV0 = v0 * UV_SCALE;
		float texU1 = u1 * UV_SCALE;
		float texV1 = v1 * UV_SCALE;
		switch (direction) {
			case DOWN -> emitQuad(consumer, pose,
					x(u0, camera), y(surface, camera), z(v1, camera), texU0, texV1,
					x(u1, camera), y(surface, camera), z(v1, camera), texU1, texV1,
					x(u1, camera), y(surface, camera), z(v0, camera), texU1, texV0,
					x(u0, camera), y(surface, camera), z(v0, camera), texU0, texV0,
					positions, camera, color);
			case UP -> emitQuad(consumer, pose,
					x(u0, camera), y(surface, camera), z(v0, camera), texU0, texV0,
					x(u1, camera), y(surface, camera), z(v0, camera), texU1, texV0,
					x(u1, camera), y(surface, camera), z(v1, camera), texU1, texV1,
					x(u0, camera), y(surface, camera), z(v1, camera), texU0, texV1,
					positions, camera, color);
			case NORTH -> emitQuad(consumer, pose,
					x(u1, camera), y(v0, camera), z(surface, camera), texU1, texV0,
					x(u0, camera), y(v0, camera), z(surface, camera), texU0, texV0,
					x(u0, camera), y(v1, camera), z(surface, camera), texU0, texV1,
					x(u1, camera), y(v1, camera), z(surface, camera), texU1, texV1,
					positions, camera, color);
			case SOUTH -> emitQuad(consumer, pose,
					x(u0, camera), y(v0, camera), z(surface, camera), texU0, texV0,
					x(u1, camera), y(v0, camera), z(surface, camera), texU1, texV0,
					x(u1, camera), y(v1, camera), z(surface, camera), texU1, texV1,
					x(u0, camera), y(v1, camera), z(surface, camera), texU0, texV1,
					positions, camera, color);
			case WEST -> emitQuad(consumer, pose,
					x(surface, camera), y(v0, camera), z(u0, camera), texU0, texV0,
					x(surface, camera), y(v0, camera), z(u1, camera), texU1, texV0,
					x(surface, camera), y(v1, camera), z(u1, camera), texU1, texV1,
					x(surface, camera), y(v1, camera), z(u0, camera), texU0, texV1,
					positions, camera, color);
			case EAST -> emitQuad(consumer, pose,
					x(surface, camera), y(v0, camera), z(u1, camera), texU1, texV0,
					x(surface, camera), y(v0, camera), z(u0, camera), texU0, texV0,
					x(surface, camera), y(v1, camera), z(u0, camera), texU0, texV1,
					x(surface, camera), y(v1, camera), z(u1, camera), texU1, texV1,
					positions, camera, color);
		}
	}

	private static void emitQuad(VertexConsumer consumer, PoseStack.Pose pose,
			float x1, float y1, float z1, float u1, float v1,
			float x2, float y2, float z2, float u2, float v2,
			float x3, float y3, float z3, float u3, float v3,
			float x4, float y4, float z4, float u4, float v4,
			Set<BlockPos> positions, Vec3 camera, int color) {
		emitVertex(consumer, pose, x1, y1, z1, u1, v1, positions, camera, color);
		emitVertex(consumer, pose, x2, y2, z2, u2, v2, positions, camera, color);
		emitVertex(consumer, pose, x3, y3, z3, u3, v3, positions, camera, color);
		emitVertex(consumer, pose, x4, y4, z4, u4, v4, positions, camera, color);
	}

	private static void emitVertex(VertexConsumer consumer, PoseStack.Pose pose, float x, float y, float z,
			float u, float v, Set<BlockPos> positions, Vec3 camera, int color) {
		Vector3f normal = cornerNormal(positions,
				(int) Math.round(x + camera.x),
				(int) Math.round(y + camera.y),
				(int) Math.round(z + camera.z));
		consumer.addVertex(pose.pose(), x, y, z)
				.setColor(color >> 16 & 0xFF, color >> 8 & 0xFF, color & 0xFF, color >>> 24)
				.setUv(u, v)
				.setLight(LightTexture.FULL_BRIGHT)
				.setNormal(pose, normal.x, normal.y, normal.z);
	}

	private static Vector3f cornerNormal(Set<BlockPos> positions, int cornerX, int cornerY, int cornerZ) {
		Vector3f normal = new Vector3f();
		for (int offsetX = 0; offsetX <= 1; offsetX++) {
			for (int offsetY = 0; offsetY <= 1; offsetY++) {
				for (int offsetZ = 0; offsetZ <= 1; offsetZ++) {
					BlockPos block = new BlockPos(cornerX - offsetX, cornerY - offsetY, cornerZ - offsetZ);
					if (!positions.contains(block)) continue;
					int stepX = offsetX == 0 ? -1 : 1;
					int stepY = offsetY == 0 ? -1 : 1;
					int stepZ = offsetZ == 0 ? -1 : 1;
					if (!positions.contains(block.offset(stepX, 0, 0))) normal.x += stepX;
					if (!positions.contains(block.offset(0, stepY, 0))) normal.y += stepY;
					if (!positions.contains(block.offset(0, 0, stepZ))) normal.z += stepZ;
				}
			}
		}
		return normal;
	}

	private static int rotColor(float progress) {
		float rot = Math.max(0.0F, Math.min(1.0F, progress));
		rot = rot * rot * (3.0F - 2.0F * rot);
		int red = Math.round(216.0F + (25.0F - 216.0F) * rot);
		int green = Math.round(24.0F + (61.0F - 24.0F) * rot);
		int blue = 24;
		return red << 16 | green << 8 | blue;
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
