package com.vincenthuto.hemomancy.client.render.layer.mob.endgame;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.VesperPhaseTransitionRules;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.VesperTheCrownedRefusalEntity;
import com.vincenthuto.hutoslib.client.particle.TendrilRenderer;
import com.vincenthuto.hutoslib.client.particle.data.TendrilEffectData;
import com.vincenthuto.hutoslib.common.tendril.TendrilAnchor;
import com.vincenthuto.hutoslib.common.tendril.TendrilEffectConfig;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Hemomancy tendril cocoon and dragon-ray star used for the Crowned Refusal handoff. */
public final class VesperTransitionCocoonRenderer {
	private static final int BEAM_COUNT = 14;
	private static final float STAR_HEIGHT = 1.58F;
	private static final float COCOON_OUTWARD_SAG = -1.25F;
	private static final int COCOON_QUEUED = 1;
	private static final int BURST_QUEUED = 1 << 1;
	private static final Map<UUID, Integer> QUEUED_TENDRILS = new HashMap<>();

	private VesperTransitionCocoonRenderer() {
	}

	public static void render(VesperTheCrownedRefusalEntity entity, float partialTick,
			PoseStack poseStack, MultiBufferSource buffers) {
		int transitionTick = entity.getTransitionTick();
		UUID entityId = entity.getUUID();
		if (transitionTick <= 0) {
			QUEUED_TENDRILS.remove(entityId);
			return;
		}
		if (!VesperPhaseTransitionRules.isCocoonActive(entity.getTransitionTick())) return;
		float transitionFrame = transitionTick + partialTick;
		float burst = VesperPhaseTransitionRules.cocoonBurstProgress(transitionFrame);
		Vec3 base = Vec3.directionFromRotation(0.0F, Mth.rotLerp(partialTick, entity.yRotO, entity.getYRot()))
				.multiply(1.5D, 0.0D, 1.5D);
		Vec3 entityWorld = new Vec3(Mth.lerp(partialTick, entity.xOld, entity.getX()),
				Mth.lerp(partialTick, entity.yOld, entity.getY()),
				Mth.lerp(partialTick, entity.zOld, entity.getZ()));
		long stableSeed = entity.getUUID().getMostSignificantBits()
				^ entity.getUUID().getLeastSignificantBits();
		int queued = QUEUED_TENDRILS.getOrDefault(entityId, 0);
		queued = queueCocoonTendrils(entity, entityWorld, stableSeed, transitionTick, queued);
		if (transitionTick >= VesperPhaseTransitionRules.COCOON_BURST_START_TICK
				&& (queued & BURST_QUEUED) == 0) {
			queueBurstTendrils(entity, entityWorld, stableSeed, transitionFrame);
			queued |= BURST_QUEUED;
		}
		QUEUED_TENDRILS.put(entityId, queued);

		float beam = VesperPhaseTransitionRules.cocoonBeamProgress(transitionFrame) * (1.0F - burst);
		if (beam <= 0.001F) return;
		Vec3 star = base.add(0.0D, STAR_HEIGHT, 0.0D);
		renderBeamStar(stableSeed, transitionFrame, poseStack,
				buffers.getBuffer(RenderType.dragonRaysDepth()), star, beam, true);
		renderBeamStar(stableSeed, transitionFrame, poseStack,
				buffers.getBuffer(RenderType.dragonRays()), star, beam, false);
		renderCentralFlare(poseStack, buffers.getBuffer(RenderType.dragonRays()), star, beam);
	}

	private static int queueCocoonTendrils(VesperTheCrownedRefusalEntity entity, Vec3 entityWorld,
			long stableSeed, int transitionTick, int queued) {
		if ((queued & COCOON_QUEUED) != 0) {
			return queued;
		}
		Vec3 worldBase = worldBase(entity, entityWorld);
		var strands = VesperTransitionCocoonGeometry.strands(Vec3.ZERO,
				VesperPhaseTransitionRules.COCOON_START_TICK, 1.0F, 0.0F);
		int elapsedFormation = Math.max(0, transitionTick - VesperPhaseTransitionRules.COCOON_START_TICK);
		int growthTicks = Math.max(1,
				VesperPhaseTransitionRules.COCOON_FORMATION_TICKS - elapsedFormation);
		int remainingUntilBurst = Math.max(1, VesperPhaseTransitionRules.TOTAL_TICKS - transitionTick);
		int holdTicks = Math.max(1, remainingUntilBurst - growthTicks
				- VesperPhaseTransitionRules.COCOON_BURST_TICKS);
		if (transitionTick < VesperPhaseTransitionRules.COCOON_BURST_START_TICK) {
			for (var strand : strands) {
				Vec3 start = worldBase.add(strand.joints().get(0).center());
				Vec3 end = worldBase.add(strand.joints().get(strand.joints().size() - 1).center());
				Vec3 cocoonOutwardDirection = cocoonOutwardDirection(strand.joints().get(0).center());
				long seed = stableSeed ^ (long) strand.index() * 0x9E3779B97F4A7C15L;
				TendrilRenderer.INSTANCE.add(new TendrilEffectData(new TendrilAnchor.Point(start),
						new TendrilAnchor.Point(end), cocoonTendrilConfig(strand.index(), seed,
								growthTicks, holdTicks), seed), 0.0F, cocoonOutwardDirection);
			}
		}
		return queued | COCOON_QUEUED;
	}

	private static void queueBurstTendrils(VesperTheCrownedRefusalEntity entity, Vec3 entityWorld,
			long stableSeed, float transitionFrame) {
		Vec3 worldBase = worldBase(entity, entityWorld);
		var sealed = VesperTransitionCocoonGeometry.strands(Vec3.ZERO, transitionFrame, 1.0F, 0.0F);
		var bursting = VesperTransitionCocoonGeometry.strands(Vec3.ZERO, transitionFrame, 1.0F, 1.0F);
		for (int index = 0; index < sealed.size(); index++) {
			Vec3 start = worldBase.add(sealed.get(index).joints().get(0).center());
			Vec3 end = worldBase.add(bursting.get(index).joints().get(bursting.get(index).joints().size() - 1).center());
			Vec3 cocoonOutwardDirection = cocoonOutwardDirection(sealed.get(index).joints().get(0).center());
			long seed = stableSeed ^ 0xD6E8FEB86659FD93L ^ (long) index * 0xA0761D6478BD642FL;
			TendrilRenderer.INSTANCE.add(new TendrilEffectData(new TendrilAnchor.Point(start),
					new TendrilAnchor.Point(end), burstTendrilConfig(index, seed), seed), 0.0F,
					cocoonOutwardDirection);
		}
	}

	private static Vec3 cocoonOutwardDirection(Vec3 root) {
		Vec3 radial = new Vec3(root.x, 0.0D, root.z);
		return radial.lengthSqr() < 1.0E-8D ? new Vec3(0.0D, 0.0D, 1.0D) : radial.normalize();
	}

	private static Vec3 worldBase(VesperTheCrownedRefusalEntity entity, Vec3 entityWorld) {
		float yaw = Mth.rotLerp(0.0F, entity.yRotO, entity.getYRot());
		return entityWorld.add(Vec3.directionFromRotation(0.0F, yaw).scale(1.5D));
	}

	private static TendrilEffectConfig cocoonTendrilConfig(int index, long seed,
			int growthTicks, int holdTicks) {
		boolean crimson = (index & 1) == 0;
		return TendrilEffectConfig.defaults()
				.withColors(crimson ? 0xF8B40016 : 0xF8060109,
						crimson ? 0xB8E60018 : 0xB8200008)
				.withRange(10.0F)
				.withLifecycle(growthTicks, holdTicks,
						VesperPhaseTransitionRules.COCOON_BURST_TICKS)
				.withShape(16, 1, 0.22F, 0.055F)
				.withBranching(0, 0, 0.05F, 0.3F)
				.withWrithe(0.16F, 0.08F, 0.72F, COCOON_OUTWARD_SAG)
				.withBlendColors(false)
				.withFixedSeed(true, seed);
	}

	private static TendrilEffectConfig burstTendrilConfig(int index, long seed) {
		boolean crimson = (index & 1) == 0;
		return TendrilEffectConfig.defaults()
				.withColors(crimson ? 0xF8D0001C : 0xF8060109,
						crimson ? 0xB8FF001A : 0xB82A000A)
				.withRange(12.0F)
				.withLifecycle(1, 1, VesperPhaseTransitionRules.COCOON_BURST_TICKS)
				.withShape(10, 1, 0.12F, 0.02F)
				.withBranching(0, 0, 0.05F, 0.4F)
				.withWrithe(0.08F, 0.12F, 0.55F, 0.0F)
				.withBlendColors(false)
				.withFixedSeed(true, seed);
	}

	private static void renderBeamStar(long stableSeed, float transitionFrame,
			PoseStack poseStack, VertexConsumer consumer, Vec3 center, float intensity, boolean darkPass) {
		float age = transitionFrame;
		float pulse = intensity * (0.78F + 0.22F * Mth.sin(age * 0.31F));
		Matrix4f matrix = poseStack.last().pose();
		float stablePhase = (stableSeed & 0xFFFFL) * 0.0000958738F;
		for (int index = 0; index < BEAM_COUNT; index++) {
			float phase = stablePhase + index * (Mth.TWO_PI / BEAM_COUNT) + age * 0.026F;
			float vertical = -0.82F + 1.64F * ((index * 7 % BEAM_COUNT) / (float) (BEAM_COUNT - 1));
			Vec3 direction = new Vec3(Mth.cos(phase), vertical, Mth.sin(phase)).normalize();
			float flutter = 0.72F + 0.28F * Mth.sin(age * 0.23F + index * 1.61F);
			float length = (0.65F + pulse * 1.65F) * flutter;
			float width = (0.055F + pulse * 0.12F) * (darkPass ? 1.65F : 1.0F);
			Vec3 side = new Vec3(-direction.z, 0.0D, direction.x).normalize().scale(width);
			Vec3 tip = center.add(direction.scale(length));
			boolean blackRay = darkPass || (index & 1) != 0;
			float red = blackRay ? 0.012F : 0.62F;
			float green = blackRay ? 0.0F : 0.004F;
			float blue = blackRay ? 0.008F : 0.014F;
			float alpha = pulse * (darkPass ? 0.64F : 0.86F);
			addTriangle(consumer, matrix, center.add(side), center.subtract(side), tip,
					red, green, blue, alpha);
		}
	}

	private static void renderCentralFlare(PoseStack poseStack, VertexConsumer consumer,
			Vec3 center, float intensity) {
		float radius = 0.11F + intensity * 0.27F;
		Matrix4f matrix = poseStack.last().pose();
		addTriangle(consumer, matrix, center.add(-radius, 0.0D, 0.0D),
				center.add(radius, 0.0D, 0.0D), center.add(0.0D, radius, 0.0D),
				0.72F, 0.006F, 0.018F, 0.92F * intensity);
		addTriangle(consumer, matrix, center.add(-radius, 0.0D, 0.0D),
				center.add(radius, 0.0D, 0.0D), center.add(0.0D, -radius, 0.0D),
				0.72F, 0.006F, 0.018F, 0.92F * intensity);
	}

	private static void addTriangle(VertexConsumer consumer, Matrix4f matrix, Vec3 first, Vec3 second,
			Vec3 third, float red, float green, float blue, float alpha) {
		consumer.addVertex(matrix, (float) first.x, (float) first.y, (float) first.z)
				.setColor(red, green, blue, alpha);
		consumer.addVertex(matrix, (float) second.x, (float) second.y, (float) second.z)
				.setColor(red, green, blue, alpha);
		consumer.addVertex(matrix, (float) third.x, (float) third.y, (float) third.z)
				.setColor(red, green, blue, 0.0F);
	}
}
