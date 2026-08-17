package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.VesperTheCrownedRefusalEntity;
import com.vincenthuto.hemomancy.common.entity.mob.monster.BloodDrunkPuppeteerEntity;
import com.vincenthuto.hemomancy.common.entity.mob.monster.EnthralledDollEntity;
import com.vincenthuto.hemomancy.common.entity.summon.BoundPuppeteerSummon;
import com.vincenthuto.hemomancy.common.entity.summon.BoundSummonBehavior;
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


import java.util.UUID;

public final class PuppeteerThreadRenderer {
	private static final int SEGMENTS = 28;
	private static final double WILD_PUPPETEER_ANCHOR_SCALE = 0.25;
	private static final double WILD_DOLL_ANCHOR_SCALE = -0.5;
	private static final double VESPER_ANCHOR_SCALE = 0.72;

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
				renderThreadToSummon(poseStack, consumer, summon, bound, partialTick, camera, level);
			} else if (entity instanceof EnthralledDollEntity doll && doll.isSummonedByPuppeteer()) {
				renderThreadToWildDoll(poseStack, consumer, doll, partialTick, camera, level);
			}
		}

		buffer.endBatch(RenderType.lines());
	}

	private static void renderThreadToWildDoll(PoseStack poseStack, VertexConsumer consumer,
											   EnthralledDollEntity doll, float partialTick,
											   Vec3 camera, ClientLevel level) {
		BloodDrunkPuppeteerEntity puppeteer = findPuppeteerOwner(level, doll.getOwnerUUID());
		if (puppeteer == null) {
			return;
		}
		Vec3 start = entityAnchor(puppeteer, partialTick, WILD_PUPPETEER_ANCHOR_SCALE).subtract(camera);
		Vec3 end = entityAnchor(doll, partialTick, WILD_DOLL_ANCHOR_SCALE).subtract(camera);
		renderThread(poseStack, consumer, start, end, doll.tickCount + partialTick, 1.0F, false);
	}

	private static void renderThreadToSummon(PoseStack poseStack, VertexConsumer consumer,
											 LivingEntity summon, BoundPuppeteerSummon bound,
											 float partialTick, Vec3 camera, ClientLevel level) {
		UUID ownerId = bound.hemomancy$getOwnerUUID();
		if (ownerId == null) {
			return;
		}
		LivingEntity controller = findThreadController(level, ownerId);
		if (controller == null) {
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

		Vec3 start = (controller instanceof Player owner
				? ownerAnchor(owner, partialTick)
				: entityAnchor(controller, partialTick, VESPER_ANCHOR_SCALE)).subtract(camera);

		Vec3 end = summonAnchor(summon, partialTick).subtract(camera);
		boolean gnawed = controller instanceof Player owner
				&& bound.hemomancy$getCrossbarUUID() != null
				&& !bound.hemomancy$isTrialSummon()
				&& BoundSummonBehavior.hasEquippedMorphling(owner);
		renderThread(poseStack, consumer, start, end, summon.tickCount + partialTick, fade, gnawed);
	}

	private static void renderThread(PoseStack poseStack, VertexConsumer consumer,
									 Vec3 start, Vec3 end, float time, float fade, boolean gnawed) {
		Vec3 delta = end.subtract(start);

		poseStack.pushPose();
		PoseStack.Pose pose = poseStack.last();
		for (int i = 0; i < SEGMENTS; i++) {
			if (gnawed && (i + (int) (time / 12.0F)) % 9 == 4) continue;
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
			if (gnawed && i % 4 != 0) {
				red = (int) (red * 0.55F);
				green = (int) (green * 0.4F);
				blue = (int) (blue * 0.4F);
			}
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

	private static BloodDrunkPuppeteerEntity findPuppeteerOwner(ClientLevel level, UUID ownerId) {
		if (ownerId == null) {
			return null;
		}
		for (Entity entity : level.entitiesForRendering()) {
			if (entity instanceof BloodDrunkPuppeteerEntity puppeteer
					&& puppeteer.isAlive()
					&& ownerId.equals(puppeteer.getUUID())) {
				return puppeteer;
			}
		}
		return null;
	}

	private static LivingEntity findThreadController(ClientLevel level, UUID ownerId) {
		Player player = level.getPlayerByUUID(ownerId);
		if (player != null) {
			return player;
		}
		for (Entity entity : level.entitiesForRendering()) {
			if (entity instanceof VesperTheCrownedRefusalEntity vesper
					&& vesper.isAlive()
					&& ownerId.equals(vesper.getUUID())) {
				return vesper;
			}
		}
		return null;
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
		return PuppeteerThreadEndpointRules.summonEndpoint(
				summon.xOld, summon.yOld, summon.zOld,
				summon.getX(), summon.getY(), summon.getZ(), summon.getBbHeight(), partialTick);
	}

	private static Vec3 entityAnchor(LivingEntity entity, float partialTick, double heightScale) {
		double x = Mth.lerp(partialTick, entity.xOld, entity.getX());
		double y = Mth.lerp(partialTick, entity.yOld, entity.getY()) + entity.getBbHeight() * heightScale;
		double z = Mth.lerp(partialTick, entity.zOld, entity.getZ());
		return new Vec3(x, y, z);
	}

	private static Vec3 threadPoint(Vec3 start, Vec3 delta, float t, float time) {
		double sag = -Math.sin(t * Math.PI) * 0.22;
		double wobbleX = Math.sin(time * 0.16 + t * 13.0) * 0.035;
		double wobbleZ = Math.cos(time * 0.13 + t * 10.0) * 0.035;
		return start.add(delta.x * t + wobbleX, delta.y * t + sag, delta.z * t + wobbleZ);
	}
}
