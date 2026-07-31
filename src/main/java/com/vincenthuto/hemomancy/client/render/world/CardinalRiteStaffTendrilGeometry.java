package com.vincenthuto.hemomancy.client.render.world;

import com.vincenthuto.hemomancy.common.rite.CardinalRiteCancellationRules;
import com.vincenthuto.hemomancy.common.rite.harbinger.CardinalRitePlantingSequence;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * Deterministic animated curves for the strands binding a planted Living Staff
 * to its Cardinal Focus. The roots are deliberately time-independent so the
 * writhing never appears to slide across the focus.
 */
public final class CardinalRiteStaffTendrilGeometry {
	private static final int STRAND_COUNT = 6;
	private static final int SEGMENTS_PER_STRAND = 16;
	private static final double ROOT_Y_OFFSET = 0.86D;
	private static final double GOLDEN_ANGLE = Math.PI * (3.0D - Math.sqrt(5.0D));

	private CardinalRiteStaffTendrilGeometry() {
	}

	public static float visibilityProgress(float staffPlantingTicks, float cancellationTicks) {
		float formation = staffPlantingTicks < 0.0F ? 1.0F : clamp(
				(staffPlantingTicks - CardinalRitePlantingSequence.IMPACT_TICK)
						/ (CardinalRitePlantingSequence.DURATION_TICKS
								- CardinalRitePlantingSequence.IMPACT_TICK));
		float retraction = 1.0F - clamp(
				cancellationTicks / CardinalRiteCancellationRules.TOTAL_TICKS);
		return Math.min(formation, retraction);
	}

	public static List<Joint> visibleJoints(List<Joint> joints, float visibilityProgress) {
		if (joints == null || joints.size() < 2 || visibilityProgress <= 0.0F) {
			return List.of();
		}
		float visibleSegments = clamp(visibilityProgress) * (joints.size() - 1);
		int completeSegments = Math.min(joints.size() - 1, (int) Math.floor(visibleSegments));
		List<Joint> visible = new ArrayList<>(completeSegments + 2);
		for (int index = 0; index <= completeSegments; index++) {
			visible.add(joints.get(index));
		}
		float partialSegment = visibleSegments - completeSegments;
		if (partialSegment > 0.0001F && completeSegments < joints.size() - 1) {
			Joint first = joints.get(completeSegments);
			Joint second = joints.get(completeSegments + 1);
			visible.add(new Joint(
					first.center().lerp(second.center(), partialSegment),
					Mth.lerp(partialSegment, first.halfWidth(), second.halfWidth()),
					Mth.lerp(partialSegment, first.opacity(), second.opacity())));
		}
		return List.copyOf(visible);
	}

	public static List<Strand> strands(BlockPos focus, float time) {
		List<Strand> strands = new ArrayList<>(STRAND_COUNT);
		double centerX = focus.getX() + 0.5D;
		double centerZ = focus.getZ() + 0.5D;
		double rootY = focus.getY() + ROOT_Y_OFFSET;
		double focusPhase = ((focus.hashCode() & 0x7fffffff) % 4096) * 0.0017D;

		for (int strandIndex = 0; strandIndex < STRAND_COUNT; strandIndex++) {
			double baseAngle = focusPhase + strandIndex * GOLDEN_ANGLE;
			double rootRadius = 0.245D + (strandIndex % 3) * 0.018D;
			double climb = 0.82D + (strandIndex % 4) * 0.105D;
			double turns = 1.08D + (strandIndex % 3) * 0.16D;
			double direction = (strandIndex & 1) == 0 ? 1.0D : -1.0D;
			List<Joint> joints = new ArrayList<>(SEGMENTS_PER_STRAND + 1);

			for (int segment = 0; segment <= SEGMENTS_PER_STRAND; segment++) {
				double progress = segment / (double) SEGMENTS_PER_STRAND;
				double animatedWeight = Mth.smoothstep(progress);
				double writhePhase = time * (0.105D + strandIndex * 0.004D)
						+ strandIndex * 1.73D;
				double radialWrithe = Math.sin(writhePhase + progress * 8.4D)
						* 0.018D * animatedWeight;
				double angularWrithe = Math.sin(writhePhase * 0.77D + progress * 6.1D)
						* 0.115D * animatedWeight;
				double verticalWrithe = Math.sin(writhePhase * 1.19D + progress * 9.2D)
						* 0.022D * animatedWeight;

				double radius = Mth.lerp(animatedWeight, rootRadius, 0.062D) + radialWrithe;
				double angle = baseAngle + direction * turns * Math.PI * 2.0D * progress
						+ angularWrithe;
				double y = rootY + climb * progress + verticalWrithe;
				float halfWidth = Mth.lerp((float) animatedWeight, 0.038F, 0.010F);
				float opacity = Mth.lerp((float) animatedWeight, 0.78F, 0.42F);

				joints.add(new Joint(new Vec3(
						centerX + Math.cos(angle) * radius,
						y,
						centerZ + Math.sin(angle) * radius),
						halfWidth, opacity));
			}
			strands.add(new Strand(strandIndex, List.copyOf(joints)));
		}
		return List.copyOf(strands);
	}

	private static float clamp(float value) {
		return Math.max(0.0F, Math.min(1.0F, value));
	}

	public record Strand(int index, List<Joint> joints) {
	}

	public record Joint(Vec3 center, float halfWidth, float opacity) {
	}
}
