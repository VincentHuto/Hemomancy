package com.vincenthuto.hemomancy.client.render.layer.mob.endgame;

import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import com.vincenthuto.hemomancy.client.render.world.SanguineTendrilRibbonRenderer;

import java.util.ArrayList;
import java.util.List;

/** Deterministic, entity-local curves for Vesper's phase-transition cocoon. */
public final class VesperTransitionCocoonGeometry {
	private static final int STRAND_COUNT = 28;
	private static final int SEGMENTS_PER_STRAND = 24;
	private static final double BASE_Y_OFFSET = -1.0D;
	private static final double HEIGHT = 5.55D;
	private static final double RADIAL_EXPANSION = 1.0D;
	private static final double GOLDEN_ANGLE = Math.PI * (3.0D - Math.sqrt(5.0D));

	private VesperTransitionCocoonGeometry() {
	}

	public static List<Strand> strands(Vec3 base, float time, float formationProgress,
			float burstProgress) {
		float formation = clamp(formationProgress);
		float burst = clamp(burstProgress);
		List<Strand> strands = new ArrayList<>(STRAND_COUNT);
		for (int strandIndex = 0; strandIndex < STRAND_COUNT; strandIndex++) {
			double baseAngle = strandIndex * GOLDEN_ANGLE;
			// Keep every strand on the same rotational flow.  Alternating the sign
			// makes neighboring ribbons cross through the cocoon instead of reading
			// as one tight, orderly wrap around Vesper.
			double direction = 1.0D;
				double turns = 4.08D + (strandIndex % 4) * 0.07D;
			List<Joint> complete = new ArrayList<>(SEGMENTS_PER_STRAND + 1);
			for (int segment = 0; segment <= SEGMENTS_PER_STRAND; segment++) {
				double progress = segment / (double) SEGMENTS_PER_STRAND;
				double animatedWeight = Mth.smoothstep(progress);
				double writhePhase = time * (0.052D + strandIndex * 0.0008D) + strandIndex * 1.31D;
				double angularWrithe = Math.sin(writhePhase + progress * 8.2D)
						* 0.105D * animatedWeight;
				double radialWrithe = Math.sin(writhePhase * 1.19D + progress * 10.4D)
						* 0.055D * animatedWeight;
				double verticalWrithe = Math.sin(writhePhase * 0.83D + progress * 9.1D)
						* 0.035D * animatedWeight;
				double radialExpansion = RADIAL_EXPANSION * (1.0D - Math.pow(progress, 2.8D));
				double radius = Mth.lerp(progress, 1.16D, 0.08D)
						+ Math.pow(Math.sin(Math.PI * progress), 0.7D) * 0.72D
						+ radialExpansion + radialWrithe;
				double angle = baseAngle + direction * turns * Math.PI * 2.0D * progress
						+ angularWrithe;
				double burstDistance = burst * 3.8D * (0.35D + animatedWeight * 0.65D);
				double y = base.y + BASE_Y_OFFSET + 0.02D + HEIGHT * progress + verticalWrithe
						+ burst * (0.25D + progress * 1.15D);
				float halfWidth = Mth.lerp((float) progress, 0.14F, 0.065F);
				float opacity = Mth.lerp((float) progress, 0.94F, 0.68F) * (1.0F - burst);
				complete.add(new Joint(new Vec3(
						base.x + Math.cos(angle) * (radius + burstDistance),
						y,
						base.z + Math.sin(angle) * (radius + burstDistance)),
						halfWidth, opacity));
			}
			strands.add(new Strand(strandIndex, visibleJoints(complete, formation)));
		}
		return List.copyOf(strands);
	}

	private static List<Joint> visibleJoints(List<Joint> joints, float progress) {
		if (progress <= 0.0F) return List.of();
		float visibleSegments = progress * (joints.size() - 1);
		int completeSegments = Math.min(joints.size() - 1, (int) Math.floor(visibleSegments));
		List<Joint> visible = new ArrayList<>(completeSegments + 2);
		for (int index = 0; index <= completeSegments; index++) visible.add(joints.get(index));
		float partial = visibleSegments - completeSegments;
		if (partial > 0.0001F && completeSegments < joints.size() - 1) {
			Joint first = joints.get(completeSegments);
			Joint second = joints.get(completeSegments + 1);
			visible.add(new Joint(first.center().lerp(second.center(), partial),
					Mth.lerp(partial, first.halfWidth(), second.halfWidth()),
					Mth.lerp(partial, first.opacity(), second.opacity())));
		}
		return List.copyOf(visible);
	}

	private static float clamp(float value) {
		return Math.max(0.0F, Math.min(1.0F, value));
	}

	public record Strand(int index, List<Joint> joints) implements SanguineTendrilRibbonRenderer.Strand {
	}

	public record Joint(Vec3 center, float halfWidth, float opacity) implements SanguineTendrilRibbonRenderer.Joint {
	}
}
