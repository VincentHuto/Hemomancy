package com.vincenthuto.hemomancy.common.tile.harbinger.crafting.engram;

/**
 * Where a Living Staff points on an engram. Sigil space is the north-facing loom's frame in blocks, centred on the
 * loom: x east, z south, the loom's front toward -z. A loom turned to face another way turns its engram with it, one
 * quarter turn clockwise per step from north (east 1, south 2, west 3).
 */
public final class EngramAim {
	/** Aiming at the loom itself, within this share of the tip radius, means the Vessel. */
	private static final double VESSEL_SHARE = 0.25D;

	private EngramAim() {
	}

	/** The node nearest a point in sigil space, if the point lies within `snap` blocks of it; the loom is the Vessel. */
	public static int nearestNode(double x, double z, double tipRadius, double snap) {
		if (Math.hypot(x, z) <= tipRadius * VESSEL_SHARE) return EngramSigilGeometry.VESSEL;
		int best = -1;
		double bestDistance = snap;
		for (int node = 0; node < EngramSigilGeometry.VESSEL; node++) {
			double[] at = EngramSigilGeometry.unit(node);
			double distance = Math.hypot(x - at[0] * tipRadius, z - at[1] * tipRadius);
			if (distance <= bestDistance) {
				bestDistance = distance;
				best = node;
			}
		}
		return best;
	}

	/** Where an eye ray meets the horizontal plane at `planeY`, as {x, z}, or null if it never does within reach. */
	public static double[] planeHit(double eyeX, double eyeY, double eyeZ, double lookX, double lookY, double lookZ,
			double planeY, double reach) {
		if (lookY > -1.0E-6D) return null;
		double length = Math.sqrt(lookX * lookX + lookY * lookY + lookZ * lookZ);
		double t = (planeY - eyeY) / lookY;
		if (t < 0 || t * length > reach) return null;
		return new double[]{eyeX + lookX * t, eyeZ + lookZ * t};
	}

	/** A sigil-space offset turned into a world offset from the loom's centre. */
	public static double[] toWorld(double x, double z, int turns) {
		for (int i = 0; i < Math.floorMod(turns, 4); i++) {
			double next = -z;
			z = x;
			x = next;
		}
		return new double[]{x, z};
	}

	public static double[] toSigil(double x, double z, int turns) {
		return toWorld(x, z, 4 - Math.floorMod(turns, 4));
	}

	/**
	 * Where a point the staff touches on a block face shows: the centre of the sixteenth of the face it fell in, a
	 * thirty-second proud of the face, on the same bit grid HutosLib's BitLocation chisels to. (nx, ny, nz) is the
	 * face's outward step.
	 */
	public static double[] onPixel(double x, double y, double z, int nx, int ny, int nz) {
		return new double[]{pixel(x, nx), pixel(y, ny), pixel(z, nz)};
	}

	private static double pixel(double v, int step) {
		if (step != 0) return v + step / 32.0D;
		return (Math.floor(v * 16.0D) + 0.5D) / 16.0D;
	}
}
