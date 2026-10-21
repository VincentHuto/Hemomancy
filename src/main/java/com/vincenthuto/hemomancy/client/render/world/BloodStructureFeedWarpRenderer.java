package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.client.data.ActiveBloodStructureFeedClientData;
import com.vincenthuto.hemomancy.client.render.HemoRenderTypes;
import com.vincenthuto.hemomancy.common.event.BloodFeedStyle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class BloodStructureFeedWarpRenderer {

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
		RenderType renderType;
		if (feed.getStyle() == BloodFeedStyle.INFUSION) {
			renderInfusionVeins(feed, positions, poseStack, buffer, camera, time, partialTick, bounds.seed(), color);
			if (feed.getFinalizeProgress() <= 0.0f) {
				return;
			}
			renderType = HemoRenderTypes.bloodInfusionWarp(time, progress, bounds.seed(), wiggleAmp,
					bounds.centerX(camera), bounds.centerY(camera), bounds.centerZ(camera),
					feed.getFinalizeProgress(), bounds.bottomY(camera), (float) bounds.height(),
					feed.getCrackShare());
		} else {
			renderType = HemoRenderTypes.bloodStructureWarp(time, progress, bounds.seed(), wiggleAmp,
					bounds.centerX(camera), bounds.centerY(camera), bounds.centerZ(camera),
					feed.getFinalizeProgress(), bounds.bottomY(camera), (float) bounds.height());
		}
		VertexConsumer consumer = buffer.getBuffer(renderType);
		PoseStack.Pose pose = poseStack.last();

		BlockShellFaces.emit(consumer, pose, positions, camera, color);

		buffer.endBatch(renderType);
	}

	/** The veins an infusion spreads over the block's own faces, before the shell ever arrives. */
	private static void renderInfusionVeins(ActiveBloodStructureFeedClientData.FeedEntry feed, Set<BlockPos> positions,
			PoseStack poseStack, MultiBufferSource.BufferSource buffer, Vec3 camera, float time, float partialTick,
			float seed, int color) {
		BlockPos block = feed.getPositions().getFirst();
		Vec3 origin = feed.getOrigin(partialTick);
		Direction struck = struckFace(block, origin);
		RenderType veins = HemoRenderTypes.bloodInfusionVeins(time, feed.getProgress(), seed,
				feed.getFinalizeProgress(), feed.getCrackShare(),
				(float) (origin.x - camera.x), (float) (origin.y - camera.y), (float) (origin.z - camera.z),
				struck.getStepX(), struck.getStepY(), struck.getStepZ(),
				(float) (block.getX() - camera.x), (float) (block.getY() - camera.y), (float) (block.getZ() - camera.z));
		BlockShellFaces.emit(buffer.getBuffer(veins), poseStack.last(), positions, camera, color);
		buffer.endBatch(veins);
	}

	/** The face of {@code pos} that {@code origin} lies on: the one whose plane it is nearest. */
	static Direction struckFace(BlockPos pos, Vec3 origin) {
		Direction nearest = Direction.UP;
		double best = Double.MAX_VALUE;
		for (Direction direction : Direction.values()) {
			double plane = direction.getAxisDirection() == Direction.AxisDirection.POSITIVE ? 1.0D : 0.0D;
			double local = direction.getAxis().choose(origin.x - pos.getX(), origin.y - pos.getY(),
					origin.z - pos.getZ());
			double gap = Math.abs(local - plane);
			if (gap < best) {
				best = gap;
				nearest = direction;
			}
		}
		return nearest;
	}

	private static int rotColor(float progress) {
		float rot = Math.max(0.0F, Math.min(1.0F, progress));
		rot = rot * rot * (3.0F - 2.0F * rot);
		int red = Math.round(216.0F + (25.0F - 216.0F) * rot);
		int green = Math.round(24.0F + (61.0F - 24.0F) * rot);
		int blue = 24;
		return red << 16 | green << 8 | blue;
	}

}
