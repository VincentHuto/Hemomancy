package com.vincenthuto.hemomancy.client.render.world;

import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.function.DoubleBinaryOperator;

public final class BloodBindingTendrilGeometry {
	public static final int GROUND_FORM_TICKS = 8;
	public static final int COIL_FORM_TICKS = 8;
	public static final int RETRACTION_TICKS = 16;
	private static final int GROUND_SEGMENTS = 12;
	private static final int COIL_SEGMENTS = 12;
	private static final double GROUND_LAYER_OFFSET = 0.003D;

	private BloodBindingTendrilGeometry() {
	}

	public static int strandCount(long seed) {
		return 3 + Math.floorMod(Long.hashCode(seed), 3);
	}

	public static List<Strand> strands(Vec3 casterFeet, Vec3 targetFeet, double targetHeight,
			long seed, float time, float ageTicks, float retractionTicks,
			DoubleBinaryOperator groundY) {
		float groundVisibility = groundVisibility(ageTicks, retractionTicks);
		float coilVisibility = coilVisibility(ageTicks, retractionTicks);
		if (groundVisibility <= 0.0F) return List.of();

		int count = strandCount(seed);
		List<Strand> strands = new ArrayList<>(count);
		for (int strandIndex = 0; strandIndex < count; strandIndex++) {
			List<Joint> ground = visibleJoints(groundJoints(casterFeet, targetFeet, seed,
					strandIndex, time, groundY), groundVisibility);
			if (ground.isEmpty()) continue;
			List<Joint> joints = new ArrayList<>(ground);
			if (groundVisibility >= 1.0F && coilVisibility > 0.0F) {
				List<Joint> coil = visibleJoints(coilJoints(targetFeet, targetHeight, seed,
						strandIndex, time), coilVisibility);
				joints.addAll(coil);
			}
			strands.add(new Strand(strandIndex, List.copyOf(joints)));
		}
		return List.copyOf(strands);
	}

	private static float groundVisibility(float ageTicks, float retractionTicks) {
		if (retractionTicks <= 0.0F) return clamp(ageTicks / GROUND_FORM_TICKS);
		if (retractionTicks <= COIL_FORM_TICKS) return 1.0F;
		return 1.0F - clamp((retractionTicks - COIL_FORM_TICKS) / GROUND_FORM_TICKS);
	}

	private static float coilVisibility(float ageTicks, float retractionTicks) {
		float formed = clamp((ageTicks - GROUND_FORM_TICKS) / COIL_FORM_TICKS);
		return retractionTicks <= 0.0F
				? formed
				: formed * (1.0F - clamp(retractionTicks / COIL_FORM_TICKS));
	}

	private static List<Joint> groundJoints(Vec3 casterFeet, Vec3 targetFeet, long seed,
			int strandIndex, float time, DoubleBinaryOperator groundY) {
		List<Joint> joints = new ArrayList<>(GROUND_SEGMENTS + 1);
		Vec3 direction = targetFeet.subtract(casterFeet);
		Vec3 side = new Vec3(-direction.z, 0.0D, direction.x);
		if (side.lengthSqr() > 1.0E-7D) side = side.normalize();
		double phase = phase(seed, strandIndex);
		Vec3 rootOffset = new Vec3(Math.cos(phase) * 0.24D, 0.0D,
				Math.sin(phase) * 0.24D);
		for (int segment = 0; segment <= GROUND_SEGMENTS; segment++) {
			double progress = segment / (double) GROUND_SEGMENTS;
			Vec3 center = casterFeet.add(rootOffset).lerp(targetFeet, progress);
			double anchorFade = Math.sin(Math.PI * progress);
			double writhe = Math.sin(progress * Math.PI * 3.0D + phase + time * 0.055D)
					* 0.12D * anchorFade;
			center = center.add(side.scale(writhe));
			double surface = groundY.applyAsDouble(center.x, center.z);
			double ground = Double.isFinite(surface) && surface <= center.y + 0.25D
					? surface : center.y;
			center = new Vec3(center.x,
					ground + 0.035D + strandIndex * GROUND_LAYER_OFFSET, center.z);
			float rootTaper = Mth.clamp((float) progress * 4.0F, 0.0F, 1.0F);
			joints.add(new Joint(center,
					Mth.lerp((float) progress, 0.065F, 0.055F) * rootTaper,
					Mth.lerp((float) progress, 0.98F, 0.92F), true));
		}
		return joints;
	}

	private static List<Joint> coilJoints(Vec3 targetFeet, double targetHeight, long seed,
			int strandIndex, float time) {
		List<Joint> joints = new ArrayList<>(COIL_SEGMENTS + 1);
		double phase = phase(seed, strandIndex);
		double direction = (strandIndex & 1) == 0 ? 1.0D : -1.0D;
		for (int segment = 0; segment <= COIL_SEGMENTS; segment++) {
			double progress = segment / (double) COIL_SEGMENTS;
			double radius = Mth.lerp(progress, 0.38D, 0.045D)
					+ Math.sin(time * 0.08D + progress * 7.0D + phase)
					* 0.012D * Math.sin(Math.PI * progress);
			double angle = phase + direction * progress * Math.PI * 2.5D
					+ Math.sin(time * 0.065D + progress * 5.0D) * 0.08D;
			double y = targetFeet.y + 0.04D + progress * targetHeight * 0.90D;
			joints.add(new Joint(new Vec3(targetFeet.x + Math.cos(angle) * radius,
					y, targetFeet.z + Math.sin(angle) * radius),
					Mth.lerp((float) progress, 0.060F, 0.016F),
					Mth.lerp((float) progress, 0.94F, 0.86F), false));
		}
		return joints;
	}

	private static List<Joint> visibleJoints(List<Joint> joints, float visibility) {
		if (visibility <= 0.0F || joints.size() < 2) return List.of();
		float visibleSegments = clamp(visibility) * (joints.size() - 1);
		int completeSegments = Math.min(joints.size() - 1, (int) Math.floor(visibleSegments));
		List<Joint> visible = new ArrayList<>(completeSegments + 2);
		for (int index = 0; index <= completeSegments; index++) visible.add(joints.get(index));
		float partial = visibleSegments - completeSegments;
		if (partial > 0.0001F && completeSegments < joints.size() - 1) {
			Joint first = joints.get(completeSegments);
			Joint second = joints.get(completeSegments + 1);
			visible.add(new Joint(first.center().lerp(second.center(), partial),
					Mth.lerp(partial, first.halfWidth(), second.halfWidth()),
					Mth.lerp(partial, first.opacity(), second.opacity()),
					first.groundAligned() && second.groundAligned()));
		}
		return List.copyOf(visible);
	}

	private static double phase(long seed, int strandIndex) {
		long mixed = seed ^ (strandIndex * 0x9E3779B97F4A7C15L);
		return ((mixed >>> 12) & 0xffffL) / 65535.0D * Math.PI * 2.0D
				+ strandIndex * Math.PI * 2.0D / strandCount(seed);
	}

	private static float clamp(float value) {
		return Math.max(0.0F, Math.min(1.0F, value));
	}

	public record Strand(int index, List<Joint> joints) implements SanguineTendrilRibbonRenderer.Strand {
		@Override
		public boolean crimsonVeins() {
			return true;
		}
	}

	public record Joint(Vec3 center, float halfWidth, float opacity, boolean groundAligned)
			implements SanguineTendrilRibbonRenderer.Joint {
	}
}
