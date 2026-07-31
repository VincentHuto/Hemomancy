package com.vincenthuto.hemomancy.client.render.world;

import com.vincenthuto.hemomancy.common.rite.CardinalRiteCancellationRules;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

/** World-space pose for the Living Staff planted into a Cardinal Focus. */
public final class CardinalRitePlantedStaffGeometry {
	private static final double EMBEDDED_ORIGIN_OFFSET = 1.95D;
	private static final float PLANTED_SCALE = 1.75F;
	private static final float MELT_SILHOUETTE_CONTRACTION = 0.18F;
	private static final float SHADER_PUDDLE_THRESHOLD = 0.08F;
	private static final float MELT_HEIGHT = 3.0F;

	private CardinalRitePlantedStaffGeometry() {
	}

	public static Pose pose(BlockPos focus) {
		return new Pose(new Vec3(
				focus.getX() + 0.425D,
				focus.getY() + EMBEDDED_ORIGIN_OFFSET,
				focus.getZ() + 0.725D),
				0.0F, PLANTED_SCALE);
	}

	public static DissolvePose dissolvePose(BlockPos focus, float cancellationScale, float opacity) {
		Pose planted = pose(focus);
		float clampedScale = Math.max(0.0F, Math.min(1.0F, cancellationScale));
		Vec3 focusAnchor = new Vec3(
				focus.getX() + 0.5D,
				focus.getY() + 0.95D,
				focus.getZ() + 0.5D);
		return new DissolvePose(
				focusAnchor.lerp(planted.position(), clampedScale),
				planted.pitchDegrees(),
				planted.scale() * clampedScale,
				Math.max(0.0F, Math.min(1.0F, opacity)));
	}

	public static AbsorptionPose absorptionPose(BlockPos focus, float cancellationTicks) {
		Pose planted = pose(focus);
		float meltProgress = clamp(cancellationTicks
				/ CardinalRiteCancellationRules.TOTAL_TICKS);
		float easedMelt = smoothstep(meltProgress);
		float opacity = 1.0F - easedMelt;
		float silhouetteScale = 1.0F - easedMelt * MELT_SILHOUETTE_CONTRACTION;
		Vec3 poolCenter = new Vec3(
				focus.getX() + 0.5D,
				focus.getY() + 0.95D,
				focus.getZ() + 0.5D);
		boolean active = meltProgress > 0.0F;
		float shaderProgress = active
				? SHADER_PUDDLE_THRESHOLD + (1.0F - SHADER_PUDDLE_THRESHOLD) * meltProgress
				: 0.0F;
		float seed = meltSeed(focus);
		float wiggleAmplitude = 0.025F + meltProgress * 0.035F;
		return new AbsorptionPose(
				planted.position(),
				planted.pitchDegrees(),
				planted.scale() * silhouetteScale,
				opacity,
				new MeltStyle(active, meltProgress, shaderProgress, poolCenter,
						MELT_HEIGHT, seed, wiggleAmplitude));
	}

	private static float meltSeed(BlockPos focus) {
		long value = focus.asLong();
		value ^= value >>> 33;
		value *= 0xff51afd7ed558ccdL;
		value ^= value >>> 33;
		return (float) ((value & 0xFFFFL) / 65535.0D);
	}

	private static float smoothstep(float value) {
		float clamped = clamp(value);
		return clamped * clamped * (3.0F - 2.0F * clamped);
	}

	private static float clamp(float value) {
		return Math.max(0.0F, Math.min(1.0F, value));
	}

	public record Pose(Vec3 position, float pitchDegrees, float scale) {
	}

	public record DissolvePose(Vec3 position, float pitchDegrees, float scale, float opacity) {
	}

	public record AbsorptionPose(
			Vec3 position,
			float pitchDegrees,
			float scale,
			float opacity,
			MeltStyle melt) {
	}

	public record MeltStyle(
			boolean active,
			float progress,
			float shaderProgress,
			Vec3 poolCenter,
			float height,
			float seed,
			float wiggleAmplitude) {
	}
}
