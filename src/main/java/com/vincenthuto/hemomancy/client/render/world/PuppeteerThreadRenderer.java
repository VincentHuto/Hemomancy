package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.common.entity.summon.BoundPuppeteerSummon;
import com.vincenthuto.hemomancy.common.summon.PuppeteerSummonRules;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public final class PuppeteerThreadRenderer {
	private static final int SEGMENTS = 28;

	private PuppeteerThreadRenderer() {
	}

	public static void render(PoseStack poseStack, float partialTick) {
		Minecraft mc = Minecraft.getInstance();
		ClientLevel level = mc.level;
		if (level == null) {
			return;
		}
		MultiBufferSource.BufferSource buffer = mc.renderBuffers().bufferSource();
		VertexConsumer consumer = buffer.getBuffer(RenderType.lines());
		Vec3 camera = mc.gameRenderer.getMainCamera().getPosition();

		for (Entity entity : level.entitiesForRendering()) {
			if (entity instanceof LivingEntity summon && entity instanceof BoundPuppeteerSummon bound) {
				renderThreadToSummon(poseStack, consumer, summon, bound, partialTick, camera);
			}
		}

		buffer.endBatch(RenderType.lines());
	}

	private static void renderThreadToSummon(PoseStack poseStack, VertexConsumer consumer,
											 LivingEntity summon, BoundPuppeteerSummon bound,
											 float partialTick, Vec3 camera) {
		Player owner = summon.level().getPlayerByUUID(bound.hemomancy$getOwnerUUID());
		if (owner == null) {
			return;
		}
		int dismissalTicks = bound.hemomancy$getDismissalTicks();
		if (!PuppeteerSummonRules.shouldRenderDismissingSummon(summon.tickCount, dismissalTicks)) {
			return;
		}
		float fade = (float) PuppeteerSummonRules.dismissalAlpha(dismissalTicks, partialTick);
		if (fade <= 0.01F) {
			return;
		}

		Vec3 start = ownerAnchor(owner, partialTick).subtract(camera);
		Vec3 end = summonAnchor(summon, partialTick).subtract(camera);
		Vec3 delta = end.subtract(start);
		float time = summon.tickCount + partialTick;

		poseStack.pushPose();
		PoseStack.Pose pose = poseStack.last();
		for (int i = 0; i < SEGMENTS; i++) {
			float t = (float) i / SEGMENTS;
			float nextT = (float) (i + 1) / SEGMENTS;
			Vec3 point = threadPoint(start, delta, t, time);
			Vec3 next = threadPoint(start, delta, nextT, time);
			Vec3 normal = next.subtract(point).normalize();
			if (normal.lengthSqr() < 0.00001) {
				normal = new Vec3(0.0, 1.0, 0.0);
			}

			float pulse = (float) (Math.sin(time * 0.18F + t * 9.0F) * 0.5F + 0.5F);
			int red = (int) (150 + pulse * 85);
			int green = (int) (8 + pulse * 20);
			int blue = (int) (12 + pulse * 18);
			int alpha = (int) ((170 + 55 * Math.sin(t * Math.PI)) * fade);

			consumer.addVertex(pose.pose(), (float) point.x, (float) point.y, (float) point.z)
					.setColor(red, green, blue, alpha)
					.setNormal(pose, (float) normal.x, (float) normal.y, (float) normal.z);
			consumer.addVertex(pose.pose(), (float) next.x, (float) next.y, (float) next.z)
					.setColor(red, green, blue, alpha)
					.setNormal(pose, (float) normal.x, (float) normal.y, (float) normal.z);
		}
		poseStack.popPose();
	}

	private static Vec3 ownerAnchor(Player owner, float partialTick) {
		double x = Mth.lerp(partialTick, owner.xOld, owner.getX());
		double y = Mth.lerp(partialTick, owner.yOld, owner.getY()) + owner.getBbHeight() * 0.68;
		double z = Mth.lerp(partialTick, owner.zOld, owner.getZ());
		if (owner.isCrouching()) {
			y -= 0.18;
		}
		return new Vec3(x, y, z);
	}

	private static Vec3 summonAnchor(LivingEntity summon, float partialTick) {
		double x = Mth.lerp(partialTick, summon.xOld, summon.getX());
		double y = Mth.lerp(partialTick, summon.yOld, summon.getY()) + summon.getBbHeight() * 0.55;
		double z = Mth.lerp(partialTick, summon.zOld, summon.getZ());
		return new Vec3(x, y, z);
	}

	private static Vec3 threadPoint(Vec3 start, Vec3 delta, float t, float time) {
		double sag = -Math.sin(t * Math.PI) * 0.22;
		double wobbleX = Math.sin(time * 0.16 + t * 13.0) * 0.035;
		double wobbleZ = Math.cos(time * 0.13 + t * 10.0) * 0.035;
		return start.add(delta.x * t + wobbleX, delta.y * t + sag, delta.z * t + wobbleZ);
	}
}
