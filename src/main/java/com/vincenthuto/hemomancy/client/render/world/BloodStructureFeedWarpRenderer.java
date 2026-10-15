package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.client.data.ActiveBloodStructureFeedClientData;
import com.vincenthuto.hemomancy.client.render.HemoRenderTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
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
		RenderType renderType = HemoRenderTypes.bloodStructureWarp(time, progress, bounds.seed(), wiggleAmp,
				bounds.centerX(camera), bounds.centerY(camera), bounds.centerZ(camera),
				feed.getFinalizeProgress(), bounds.bottomY(camera), (float) bounds.height());
		VertexConsumer consumer = buffer.getBuffer(renderType);
		PoseStack.Pose pose = poseStack.last();

		BlockShellFaces.emit(consumer, pose, positions, camera, color);

		buffer.endBatch(renderType);
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
