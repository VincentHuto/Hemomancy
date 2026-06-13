package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.item.harbinger.memory.LastDeathMemory;
import com.vincenthuto.hemomancy.common.item.harbinger.memory.MemoryThreadCurveRules;
import com.vincenthuto.hemomancy.common.item.harbinger.memory.MemoryThreadItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public final class MemoryThreadLineRenderer {
	private static final int MAX_RENDERED_POINTS = 72;
	private static final int SAMPLES_PER_SEGMENT = 5;
	private static final double HAND_SIDE_OFFSET = 0.58D;
	private static final double HAND_FORWARD_OFFSET = 0.34D;
	private static final double HAND_DOWN_OFFSET = 1.0D;

	private MemoryThreadLineRenderer() {
	}

	public static void render(PoseStack poseStack, float partialTick) {
		Minecraft mc = Minecraft.getInstance();
		LocalPlayer player = mc.player;
		if (mc.level == null || player == null) {
			return;
		}

		HeldThread heldThread = findHeldThread(player);
		if (heldThread == null || !MemoryThreadItem.isActive(heldThread.stack())) {
			return;
		}
		if (!mc.level.dimension().location().toString().equals(MemoryThreadItem.dimension(heldThread.stack()))) {
			return;
		}

		List<Long> points = MemoryThreadItem.readPoints(heldThread.stack());
		if (points.size() < 2) {
			return;
		}

		Vec3 cam = mc.gameRenderer.getMainCamera().getPosition();
		MultiBufferSource.BufferSource buffer = mc.renderBuffers().bufferSource();
		VertexConsumer consumer = buffer.getBuffer(RenderType.lineStrip());
		PoseStack.Pose pose = poseStack.last();
		float time = player.tickCount + partialTick;

		int start = Math.max(0, points.size() - MAX_RENDERED_POINTS);
		List<MemoryThreadCurveRules.Point> controlPoints = points.subList(start, points.size()).stream()
				.map(MemoryThreadLineRenderer::unpackPoint)
				.toList();
		List<MemoryThreadCurveRules.Point> renderedPoints = MemoryThreadCurveRules.smooth(controlPoints, SAMPLES_PER_SEGMENT);
		for (int index = 0; index < renderedPoints.size(); index++) {
			float t = (float) index / Math.max(1, renderedPoints.size() - 1);
			Vec3 point = toVec3(renderedPoints.get(index)).subtract(cam);
			double pulse = Math.sin(time * 0.28F + index * 0.24F) * 0.035D;
			int alpha = (int) Mth.lerp(t, 85.0F, 230.0F);
			consumer.addVertex(pose.pose(), (float) point.x, (float) (point.y + 0.12D + pulse), (float) point.z)
					.setColor(168, 18, 28, alpha)
					.setNormal(pose, 0.0F, 1.0F, 0.0F);
		}
		buffer.endBatch(RenderType.lineStrip());

		renderHandLead(poseStack, partialTick, mc, player, heldThread, cam);
	}

	private static void renderHandLead(PoseStack poseStack, float partialTick, Minecraft mc, LocalPlayer player,
									   HeldThread heldThread, Vec3 cam) {
		List<Long> points = MemoryThreadItem.readPoints(heldThread.stack());
		Vec3 start = handPosition(mc, player, heldThread.hand(), partialTick);
		Vec3 end = unpack(points.get(points.size() - 1));

		MultiBufferSource.BufferSource buffer = mc.renderBuffers().bufferSource();
		VertexConsumer consumer = buffer.getBuffer(RenderType.lineStrip());
		PoseStack.Pose pose = poseStack.last();
		float time = player.tickCount + partialTick;

		for (int i = 0; i <= 10; i++) {
			float t = i / 10.0F;
			Vec3 point = start.lerp(end, t);
			double sag = -Math.sin(t * Math.PI) * 0.10D;
			double pulse = Math.sin(time * 0.36F + t * 7.0F) * 0.026D;
			Vec3 rendered = point.add(0.0D, sag + pulse, 0.0D).subtract(cam);
			consumer.addVertex(pose.pose(), (float) rendered.x, (float) rendered.y, (float) rendered.z)
					.setColor(210, 34, 42, 165)
					.setNormal(pose, 0.0F, 1.0F, 0.0F);
		}
		buffer.endBatch(RenderType.lineStrip());
	}

	private static Vec3 unpack(long packed) {
		return new Vec3(LastDeathMemory.unpackX(packed) + 0.5D,
				LastDeathMemory.unpackY(packed) + 0.18D,
				LastDeathMemory.unpackZ(packed) + 0.5D);
	}

	private static MemoryThreadCurveRules.Point unpackPoint(long packed) {
		return new MemoryThreadCurveRules.Point(LastDeathMemory.unpackX(packed) + 0.5D,
				LastDeathMemory.unpackY(packed) + 0.18D,
				LastDeathMemory.unpackZ(packed) + 0.5D);
	}

	private static Vec3 toVec3(MemoryThreadCurveRules.Point point) {
		return new Vec3(point.x(), point.y(), point.z());
	}

	private static HeldThread findHeldThread(LocalPlayer player) {
		ItemStack mainHand = player.getMainHandItem();
		if (mainHand.is(ItemInit.memory_thread.get())) {
			return new HeldThread(mainHand, InteractionHand.MAIN_HAND);
		}
		ItemStack offHand = player.getOffhandItem();
		if (offHand.is(ItemInit.memory_thread.get())) {
			return new HeldThread(offHand, InteractionHand.OFF_HAND);
		}
		return null;
	}

	private static Vec3 handPosition(Minecraft mc, LocalPlayer player, InteractionHand hand, float partialTick) {
		double x = Mth.lerp(partialTick, player.xOld, player.getX());
		double y = Mth.lerp(partialTick, player.yOld, player.getY()) + player.getEyeHeight();
		double z = Mth.lerp(partialTick, player.zOld, player.getZ());
		float yaw = Mth.lerp(partialTick, player.yRotO, player.getYRot()) * Mth.DEG_TO_RAD;
		double side = hand == InteractionHand.MAIN_HAND ? -1.0D : 1.0D;
		if (player.getMainArm() == net.minecraft.world.entity.HumanoidArm.LEFT) {
			side *= -1.0D;
		}
		Vec3 forward = player.getViewVector(partialTick).normalize();
		Vec3 right = new Vec3(Mth.cos(yaw), 0.0D, Mth.sin(yaw));
		double drop = mc.options.getCameraType().isFirstPerson() ? HAND_DOWN_OFFSET : HAND_DOWN_OFFSET + 0.24D;
		return new Vec3(x, y, z)
				.add(right.scale(HAND_SIDE_OFFSET * side))
				.add(forward.scale(HAND_FORWARD_OFFSET))
				.add(0.0D, -drop, 0.0D);
	}

	private record HeldThread(ItemStack stack, InteractionHand hand) {
	}
}
