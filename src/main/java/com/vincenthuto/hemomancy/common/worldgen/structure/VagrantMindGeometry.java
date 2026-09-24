package com.vincenthuto.hemomancy.common.worldgen.structure;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Pure geometry for the Vagrant Mind: a metaball union of ellipsoid lobes, hollowed to a shell.
 * Deliberately free of Minecraft types so it can be unit tested, matching FerricDuctilisGeometry.
 */
public final class VagrantMindGeometry {
	public static final double OUTER_RADIUS = 100.0;
	private static final double TERRAIN_CLEARANCE_THRESHOLD = 0.72;

	public record Lobe(double cx, double cy, double cz, double rx, double ry, double rz) {}
	public enum SurfaceMaterial {
		NONE, TISSUE, FOLDS
	}

	private VagrantMindGeometry() {}

	public static List<Lobe> lobes(long seed) {
		Random random = new Random(seed);
		int count = 10 + random.nextInt(7);
		List<Lobe> lobes = new ArrayList<>(count + 4);
		for (int i = 0; i < count; i++) {
			// Keep the random cerebral mass inside the eight-chunk structure envelope. Metaball fields
			// extend beyond each nominal ellipsoid, so the centre spread and radii deliberately leave a
			// guard band instead of relying on the old, clipped centre+radius estimate.
			double angle = random.nextDouble() * Math.PI * 2.0;
			double spread = 18.0 + random.nextDouble() * 20.0;
			lobes.add(new Lobe(
					Math.cos(angle) * spread,
					(random.nextDouble() - 0.5) * 30.0,
					Math.sin(angle) * spread,
					34.0 + random.nextDouble() * 18.0,
					25.0 + random.nextDouble() * 20.0,
					34.0 + random.nextDouble() * 18.0));
		}
		// Stable posterior anatomy gives every generated Mind the same readable landmarks beneath the
		// random cerebral lobes. The tighter, wider-set cerebellar halves leave a clear median cleft;
		// the narrow lower brainstem continues well below them instead of ending as a short knob.
		lobes.add(new Lobe(-29.0, -34.0, 57.0, 27.0, 19.0, 25.0));
		lobes.add(new Lobe(29.0, -34.0, 57.0, 27.0, 19.0, 25.0));
		lobes.add(new Lobe(0.0, -58.0, 43.0, 14.0, 32.0, 15.0));
		lobes.add(new Lobe(0.0, -89.0, 42.0, 9.0, 30.0, 10.0));
		return lobes;
	}

	/** Summed inverse-square falloff. Values at or above 1.0 are inside the organ. */
	public static double field(List<Lobe> lobes, double x, double y, double z) {
		double total = 0.0;
		for (Lobe lobe : lobes) {
			double dx = (x - lobe.cx()) / lobe.rx();
			double dy = (y - lobe.cy()) / lobe.ry();
			double dz = (z - lobe.cz()) / lobe.rz();
			double squared = dx * dx + dy * dy + dz * dz;
			if (squared < 4.0) {
				total += 1.0 / (0.35 + squared);
			}
		}
		return total;
	}

	public static boolean isShell(List<Lobe> lobes, double x, double y, double z, double thickness) {
		if (field(lobes, x, y, z) < 1.0) {
			return false;
		}
		// Inside the field but close enough to the boundary that stepping out along any axis leaves it.
		return field(lobes, x + thickness, y, z) < 1.0
				|| field(lobes, x - thickness, y, z) < 1.0
				|| field(lobes, x, y + thickness, z) < 1.0
				|| field(lobes, x, y - thickness, z) < 1.0
				|| field(lobes, x, y, z + thickness) < 1.0
				|| field(lobes, x, y, z - thickness) < 1.0;
	}

	/** The shell face visible from the hollow cavity. */
	public static boolean isInnerWall(List<Lobe> lobes, int x, int y, int z, double thickness) {
		if (!isShell(lobes, x, y, z, thickness)) return false;
		return isInterior(lobes, x + 1, y, z, thickness)
				|| isInterior(lobes, x - 1, y, z, thickness)
				|| isInterior(lobes, x, y + 1, z, thickness)
				|| isInterior(lobes, x, y - 1, z, thickness)
				|| isInterior(lobes, x, y, z + 1, thickness)
				|| isInterior(lobes, x, y, z - 1, thickness);
	}

	private static boolean isInterior(List<Lobe> lobes, int x, int y, int z, double thickness) {
		return field(lobes, x, y, z) >= 1.0 && !isShell(lobes, x, y, z, thickness);
	}

	/** Sparse, seed-stable ellipsoids; coordinates are local to the organ, not the chunk. */
	public static boolean isSynapseBlob(long seed, int x, int y, int z) {
		int cellX = Math.floorDiv(x, 14);
		int cellY = Math.floorDiv(y, 14);
		int cellZ = Math.floorDiv(z, 14);
		for (int cx = cellX - 1; cx <= cellX + 1; cx++) {
			for (int cy = cellY - 1; cy <= cellY + 1; cy++) {
				for (int cz = cellZ - 1; cz <= cellZ + 1; cz++) {
					long hash = seed ^ (cx * 0x9E3779B97F4A7C15L)
							^ (cy * 0xC2B2AE3D27D4EB4FL) ^ (cz * 0x165667B19E3779F9L);
					hash = mix(hash);
					if (Math.floorMod(hash, 5) != 0) continue;
					double bx = cx * 14 + 7 + (int) Math.floorMod(hash >>> 8, 5) - 2;
					double by = cy * 14 + 7 + (int) Math.floorMod(hash >>> 16, 5) - 2;
					double bz = cz * 14 + 7 + (int) Math.floorMod(hash >>> 24, 5) - 2;
					double radius = 2.7 + Math.floorMod(hash >>> 32, 16) / 10.0;
					double dx = (x - bx) / radius;
					double dy = (y - by) / (radius * 0.8);
					double dz = (z - bz) / radius;
					if (dx * dx + dy * dy + dz * dz <= 1.0) return true;
				}
			}
		}
		return false;
	}

	private static long mix(long value) {
		value = (value ^ (value >>> 30)) * 0xBF58476D1CE4E5B9L;
		value = (value ^ (value >>> 27)) * 0x94D049BB133111EBL;
		return value ^ (value >>> 31);
	}

	/**
	 * Organic terrain-removal volume surrounding the organ. Lowering the metaball threshold expands
	 * the same joined lobe silhouette beyond the shell instead of introducing a spherical or boxed
	 * excavation boundary.
	 */
	public static boolean clearsTerrain(List<Lobe> lobes, double x, double y, double z) {
		return field(lobes, x, y, z) >= TERRAIN_CLEARANCE_THRESHOLD;
	}

	/**
	 * Chooses the visible skin material without punching a hole through the hollow organ. The
	 * sagittal division is a plain-tissue sulcus between the gyri, not an air trench into the cavity.
	 */
	public static SurfaceMaterial surfaceMaterial(List<Lobe> lobes, double x, double y, double z,
			double shellThickness, double fissureHalfWidth) {
		if (!isShell(lobes, x, y, z, shellThickness)) {
			return SurfaceMaterial.NONE;
		}
		if (y > 0.0 && isFissure(x, z, fissureHalfWidth)) {
			return SurfaceMaterial.TISSUE;
		}
		if (isCerebellarRegion(x, y, z)) {
			if (isCerebellarSulcus(x, y, z)) {
				return SurfaceMaterial.TISSUE;
			}
			// Cerebellar folia run across the rear lobes, perpendicular to the crown's broad folds.
			double folium = z + Math.sin(x * 0.13) * 2.5 + Math.cos(y * 0.14) * 1.5;
			return Math.floorMod((int) Math.floor(folium), 5) < 3
					? SurfaceMaterial.FOLDS : SurfaceMaterial.TISSUE;
		}
		boolean ridge = Math.floorMod(
				(int) Math.floor(y + Math.sin(x * 0.18) * 3.0 + Math.cos(z * 0.18) * 3.0), 5) < 2;
		return ridge ? SurfaceMaterial.FOLDS : SurfaceMaterial.TISSUE;
	}

	/** Rear, inferior surface occupied by the paired cerebellar lobes. */
	private static boolean isCerebellarRegion(double x, double y, double z) {
		return y >= -54.0 && y <= -12.0 && z >= 30.0 && z <= 92.0 && Math.abs(x) <= 60.0;
	}

	/** A narrow exposed median groove that keeps the two cerebellar hemispheres visually distinct. */
	private static boolean isCerebellarSulcus(double x, double y, double z) {
		double centre = Math.sin((y + 34.0) * 0.08) * 1.0;
		return z >= 48.0 && Math.abs(x - centre) <= 1.5;
	}

	/**
	 * The y range a column can possibly contain organ in, or {@code null} when the column is empty.
	 *
	 * <p>{@link #field} only accumulates from a lobe while the normalised squared distance is under
	 * {@code 4.0}, so a point outside every lobe's {@code 2r} ellipsoid scores exactly zero and can
	 * never be shell. For one lobe, a column contributes only while {@code dx*dx + dz*dz < 4}, and
	 * within that column only while {@code dy*dy} fits in the remaining budget -- which bounds y to
	 * {@code cy +- ry * sqrt(4 - dx*dx - dz*dz)}. The union of those per-lobe spans is returned, so
	 * the caller can skip rows and whole columns that {@link #isShell} would have rejected anyway.
	 * This is a bound on where the field is non-zero, not on where it reaches 1.0, so it never clips
	 * real geometry.
	 */
	public static double[] columnSpan(List<Lobe> lobes, double x, double z) {
		double min = Double.POSITIVE_INFINITY;
		double max = Double.NEGATIVE_INFINITY;
		for (Lobe lobe : lobes) {
			double dx = (x - lobe.cx()) / lobe.rx();
			double dz = (z - lobe.cz()) / lobe.rz();
			double remaining = 4.0 - (dx * dx + dz * dz);
			if (remaining <= 0.0) {
				continue;
			}
			double reach = lobe.ry() * Math.sqrt(remaining);
			min = Math.min(min, lobe.cy() - reach);
			max = Math.max(max, lobe.cy() + reach);
		}
		return min > max ? null : new double[] { min, max };
	}

	/** The gently wandering sagittal seam, narrowing toward the front and back. */
	public static boolean isFissure(double x, double z, double halfWidth) {
		double taper = 1.0 - Math.min(1.0, Math.abs(z) / OUTER_RADIUS);
		double centre = Math.sin((z - 40.0) * 0.055) * 4.0;
		return Math.abs(x - centre) <= halfWidth * taper;
	}
}
