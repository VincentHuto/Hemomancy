package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.client.data.SutureLinkClientData;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.routing.BloodRoutingMode;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public final class HematicSutureLinkRenderer {
	private HematicSutureLinkRenderer() {
	}

	public static void render(PoseStack poseStack, float partialTick) {
		Minecraft mc = Minecraft.getInstance();
		ClientLevel level = mc.level;
		Player player = mc.player;
		if (level == null || player == null || !isNeedleHeld(player)) {
			return;
		}
		List<SutureLinkClientData.Entry> entries = SutureLinkClientData.entries();
		if (entries.isEmpty()) {
			return;
		}

		MultiBufferSource.BufferSource buffer = mc.renderBuffers().bufferSource();
		VertexConsumer consumer = buffer.getBuffer(RenderType.lines());
		Vec3 camera = mc.gameRenderer.getMainCamera().getPosition();
		Vec3 start = playerAnchor(player, partialTick).subtract(camera);
		PoseStack.Pose pose = poseStack.last();

		for (SutureLinkClientData.Entry entry : entries) {
			Vec3 end = Vec3.atCenterOf(entry.pos()).subtract(camera);
			drawSuture(pose, consumer, start, end, entry, level.getGameTime() + partialTick);
			drawMarker(pose, consumer, end, entry);
		}

		buffer.endBatch(RenderType.lines());
	}

	private static boolean isNeedleHeld(Player player) {
		return player.getMainHandItem().getItem() == ItemInit.hematic_suture_needle.get()
				|| player.getOffhandItem().getItem() == ItemInit.hematic_suture_needle.get();
	}

	private static void drawSuture(PoseStack.Pose pose, VertexConsumer consumer, Vec3 start, Vec3 end,
			SutureLinkClientData.Entry entry, float time) {
		Vec3 delta = end.subtract(start);
		for (int i = 0; i < 18; i++) {
			float t0 = i / 18.0F;
			float t1 = (i + 1) / 18.0F;
			Vec3 p0 = suturePoint(start, delta, t0, time);
			Vec3 p1 = suturePoint(start, delta, t1, time);
			Vec3 normal = p1.subtract(p0).normalize();
			if (normal.lengthSqr() < 0.00001) {
				normal = new Vec3(0.0, 1.0, 0.0);
			}
			int alpha = entry.mode() == BloodRoutingMode.FANE ? 230 : 185;
			int green = entry.bloodlineEnabled() ? 128 : 36;
			consumer.addVertex(pose.pose(), (float) p0.x, (float) p0.y, (float) p0.z)
					.setColor(235, green, 36, alpha)
					.setNormal(pose, (float) normal.x, (float) normal.y, (float) normal.z);
			consumer.addVertex(pose.pose(), (float) p1.x, (float) p1.y, (float) p1.z)
					.setColor(255, Math.min(190, green + 42), 72, alpha)
					.setNormal(pose, (float) normal.x, (float) normal.y, (float) normal.z);
		}
	}

	private static void drawMarker(PoseStack.Pose pose, VertexConsumer consumer, Vec3 center,
			SutureLinkClientData.Entry entry) {
		float size = entry.mode() == BloodRoutingMode.FANE ? 0.34F : 0.24F;
		int green = entry.bloodlineEnabled() ? 150 : 42;
		line(pose, consumer, center.add(-size, 0, 0), center.add(size, 0, 0), green);
		line(pose, consumer, center.add(0, -size, 0), center.add(0, size, 0), green);
		line(pose, consumer, center.add(0, 0, -size), center.add(0, 0, size), green);
	}

	private static void line(PoseStack.Pose pose, VertexConsumer consumer, Vec3 a, Vec3 b, int green) {
		Vec3 normal = b.subtract(a).normalize();
		consumer.addVertex(pose.pose(), (float) a.x, (float) a.y, (float) a.z)
				.setColor(255, green, 64, 220)
				.setNormal(pose, (float) normal.x, (float) normal.y, (float) normal.z);
		consumer.addVertex(pose.pose(), (float) b.x, (float) b.y, (float) b.z)
				.setColor(255, green, 64, 220)
				.setNormal(pose, (float) normal.x, (float) normal.y, (float) normal.z);
	}

	private static Vec3 playerAnchor(Player player, float partialTick) {
		double x = Mth.lerp(partialTick, player.xOld, player.getX());
		double y = Mth.lerp(partialTick, player.yOld, player.getY()) + player.getBbHeight() * 0.62;
		double z = Mth.lerp(partialTick, player.zOld, player.getZ());
		return new Vec3(x, y, z);
	}

	private static Vec3 suturePoint(Vec3 start, Vec3 delta, float t, float time) {
		double pulse = Math.sin(time * 0.18 + t * 9.0) * 0.035;
		double sag = -Math.sin(t * Math.PI) * 0.12;
		return start.add(delta.x * t, delta.y * t + sag + pulse, delta.z * t);
	}
}
