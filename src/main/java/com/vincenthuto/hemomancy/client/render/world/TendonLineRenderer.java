package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.item.shared.TendonLineItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public final class TendonLineRenderer {
	private static final int SEGMENTS = 18;
	private static final double HAND_SIDE_OFFSET = 0.62D;
	private static final double HAND_FORWARD_OFFSET = 0.42D;
	private static final double HAND_DOWN_OFFSET = 1.05D;
	private static final double THIRD_PERSON_HAND_EXTRA_DROP = 0.24D;

	private TendonLineRenderer() {
	}

	public static void render(PoseStack poseStack, float partialTick) {
		Minecraft mc = Minecraft.getInstance();
		LocalPlayer player = mc.player;
		if (mc.level == null || player == null) {
			return;
		}

		HeldLine heldLine = findHeldLine(player);
		if (heldLine == null) {
			return;
		}

		Optional<TendonLineItem.Anchor> anchor = TendonLineItem.readAnchor(heldLine.stack());
		if (anchor.isEmpty() || !mc.level.dimension().location().toString().equals(anchor.get().dimension())) {
			return;
		}

		Vec3 start = handPosition(mc, player, heldLine.hand(), partialTick);
		Vec3 end = anchor.get().target();
		Vec3 cam = mc.gameRenderer.getMainCamera().getPosition();

		MultiBufferSource.BufferSource buffer = mc.renderBuffers().bufferSource();
		VertexConsumer consumer = buffer.getBuffer(RenderType.lineStrip());
		PoseStack.Pose pose = poseStack.last();
		float time = player.tickCount + partialTick;

		for (int i = 0; i <= SEGMENTS; i++) {
			float t = (float) i / SEGMENTS;
			Vec3 point = start.lerp(end, t);
			double sag = -Math.sin(t * Math.PI) * 0.16D;
			double pulse = Math.sin(time * 0.32F + t * 8.0F) * 0.035D;
			Vec3 rendered = point.add(0.0D, sag + pulse, 0.0D).subtract(cam);
			int alpha = (int) (185 + 45 * Math.sin(t * Math.PI));
			consumer.addVertex(pose.pose(), (float) rendered.x, (float) rendered.y, (float) rendered.z)
					.setColor(122, 14, 25, alpha)
					.setNormal(pose, 0.0F, 1.0F, 0.0F);
		}

		buffer.endBatch(RenderType.lineStrip());
	}

	private static HeldLine findHeldLine(LocalPlayer player) {
		ItemStack mainHand = player.getMainHandItem();
		if (mainHand.is(ItemInit.tendon_line.get())) {
			return new HeldLine(mainHand, InteractionHand.MAIN_HAND);
		}
		ItemStack offHand = player.getOffhandItem();
		if (offHand.is(ItemInit.tendon_line.get())) {
			return new HeldLine(offHand, InteractionHand.OFF_HAND);
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
		double drop = HAND_DOWN_OFFSET;
		if (!mc.options.getCameraType().isFirstPerson()) {
			drop += THIRD_PERSON_HAND_EXTRA_DROP;
		}
		return new Vec3(x, y, z)
				.add(right.scale(HAND_SIDE_OFFSET * side))
				.add(forward.scale(HAND_FORWARD_OFFSET))
				.add(0.0D, -drop, 0.0D);
	}

	private record HeldLine(ItemStack stack, InteractionHand hand) {
	}
}
