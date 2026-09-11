package com.vincenthuto.hemomancy.client.render.world;

import com.vincenthuto.hemomancy.common.vein.EarthenVeinFeedingVortexProfile;
import com.vincenthuto.hemomancy.common.vein.EarthenVeinTravelPhase;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/** Animated grasping strands that tighten from the air around a vein into its mouth. */
public final class EarthenVeinFeedingTendrilGeometry {
	private static final Vec3 MOUTH = new Vec3(0.5D, 0.72D, 0.5D);
	private static final int STRAND_COUNT = 6;
	private static final int SEGMENTS = 12;

	private EarthenVeinFeedingTendrilGeometry() {
	}

	public static List<Strand> strands(EarthenVeinTravelPhase phase, float intensity, float time) {
		EarthenVeinFeedingVortexProfile profile = EarthenVeinFeedingVortexProfile.forPhase(phase, intensity);
		float strength = Mth.clamp(intensity, 0.0F, 1.0F);
		boolean crimson = phase == EarthenVeinTravelPhase.READY;
		double motion = time * profile.angularSpeed();
		double writhe = crimson ? 0.032D : 0.082D;
		List<Strand> strands = new ArrayList<>(STRAND_COUNT);

		for (int strandIndex = 0; strandIndex < STRAND_COUNT; strandIndex++) {
			double rootAngle = strandIndex * Mth.TWO_PI / STRAND_COUNT + motion;
			double rootRadius = 0.70D + (strandIndex & 1) * 0.09D;
			double rootHeight = 1.20D + (strandIndex % 3) * 0.15D;
			List<Joint> joints = new ArrayList<>(SEGMENTS + 1);
			for (int segment = 0; segment <= SEGMENTS; segment++) {
				double progress = segment / (double) SEGMENTS;
				double pull = Mth.smoothstep(progress);
				double taper = 1.0D - pull;
				double angle = rootAngle + progress * (1.15D + strength * 0.55D)
						+ Math.sin(motion * 0.73D + strandIndex * 1.31D + progress * 8.0D)
						* writhe * Math.sin(progress * Math.PI);
				double radius = rootRadius * taper;
				double y = Mth.lerp(pull, rootHeight, MOUTH.y)
						+ Math.sin(motion + strandIndex + progress * 5.0D)
						* writhe * Math.sin(progress * Math.PI);
				Vec3 center = new Vec3(MOUTH.x + Math.cos(angle) * radius, y,
						MOUTH.z + Math.sin(angle) * radius);
				float halfWidth = Mth.lerp((float) pull, 0.032F + strength * 0.014F, 0.006F);
				float opacity = Mth.lerp((float) pull, 0.58F + strength * 0.25F, 0.28F);
				joints.add(new Joint(center, halfWidth, opacity));
			}
			strands.add(new Strand(strandIndex, List.copyOf(joints), crimson));
		}
		return List.copyOf(strands);
	}

	public record Strand(int index, List<Joint> joints, boolean crimsonVeins)
			implements SanguineTendrilRibbonRenderer.Strand {
	}

	public record Joint(Vec3 center, float halfWidth, float opacity)
			implements SanguineTendrilRibbonRenderer.Joint {
	}
}
