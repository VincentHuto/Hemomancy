package com.vincenthuto.hemomancy.common.rite.sigil;

/**
 * Deterministic capillary deformation shared by grounded and awakened sigils.
 * Authored nodes remain fixed while the tissue between them flexes.
 */
public final class IchorianSigilOrganicGeometry {
	private IchorianSigilOrganicGeometry() {
	}

	public static Sample sample(double startX, double startY, double startZ,
			double endX, double endY, double endZ,
			float time, long seed, int step, int steps, float baseHalfWidth) {
		int safeSteps = Math.max(1, steps);
		double progress = Math.max(0.0D, Math.min(1.0D, step / (double) safeSteps));
		double x = lerp(startX, endX, progress);
		double y = lerp(startY, endY, progress);
		double z = lerp(startZ, endZ, progress);
		double dx = endX - startX;
		double dz = endZ - startZ;
		double horizontalLength = Math.hypot(dx, dz);
		double envelope = Math.sin(Math.PI * progress);
		double phase = phase(seed);
		double primary = Math.sin(progress * Math.PI * 2.0D + time * 0.075D + phase);
		double secondary = Math.sin(progress * Math.PI * 5.0D - time * 0.043D + phase * 1.7D);
		double lateral = envelope * (primary * 0.075D + secondary * 0.025D);
		if (horizontalLength > 1.0E-6D) {
			x += -dz / horizontalLength * lateral;
			z += dx / horizontalLength * lateral;
		}
		y += envelope * Math.sin(progress * Math.PI * 3.0D
				+ time * 0.061D + phase * 0.73D) * 0.025D;
		float beat = 0.90F + 0.10F * (float) Math.sin(
				time * 0.14D - progress * 2.5D + phase);
		float taper = 0.78F + 0.22F * (float) envelope;
		return new Sample(x, y, z, Math.max(0.001F, baseHalfWidth * beat * taper));
	}

	public static float nodePulse(float time, long seed, int nodeIndex) {
		double phase = phase(seed + nodeIndex * 0x9E3779B97F4A7C15L);
		return 1.0F + 0.10F * (float) Math.sin(time * 0.14D + phase);
	}

	private static double phase(long seed) {
		long mixed = seed ^ (seed >>> 33);
		mixed *= 0xff51afd7ed558ccdL;
		mixed ^= mixed >>> 33;
		return (mixed & 0xFFFFL) / 65535.0D * Math.PI * 2.0D;
	}

	private static double lerp(double start, double end, double progress) {
		return start + (end - start) * progress;
	}

	public record Sample(double x, double y, double z, float halfWidth) {
	}
}
