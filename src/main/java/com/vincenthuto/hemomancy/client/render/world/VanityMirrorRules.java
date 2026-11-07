package com.vincenthuto.hemomancy.client.render.world;

/**
 * Where a Scarlet Vanity's glass is and what it reflects, as plain arithmetic. The glass is a vertical plane facing out
 * of the vanity's front; points are world positions, and the facing is given as its horizontal normal (x, z).
 *
 * <p>The glass rectangle is in block pixels for a north-facing vanity, the desk's frame: x {@link #GLASS_X0} to
 * {@link #GLASS_X1}, y {@link #GLASS_Y0} up to at most {@link #GLASS_Y1} (the shader cuts its arched top), at depth
 * {@link #GLASS_Z} from the front. tools/model_export/scarlet_vanity_mirror.py builds the frame around it.
 */
public final class VanityMirrorRules {
	public static final float GLASS_X0 = 3.0F;
	public static final float GLASS_X1 = 13.0F;
	public static final float GLASS_Y0 = 17.0F;
	public static final float GLASS_Y1 = 30.5F;
	public static final float GLASS_Z = 14.35F;
	/** How far in front of the glass the eye must be to see itself in it. */
	public static final double MIN_IN_FRONT = 0.05D;

	private VanityMirrorRules() {
	}

	/** A point on the glass plane: the vanity's centre moved back to the glass. */
	public static double[] glassPoint(int blockX, int blockY, int blockZ, int normalX, int normalZ) {
		double back = GLASS_Z / 16.0D - 0.5D;
		return new double[] { blockX + 0.5D - normalX * back, blockY + 1.5D, blockZ + 0.5D - normalZ * back };
	}

	/** How far {@code point} stands in front of the glass, negative behind it. */
	public static double inFront(double[] point, double[] glass, int normalX, int normalZ) {
		return (point[0] - glass[0]) * normalX + (point[2] - glass[2]) * normalZ;
	}

	/**
	 * The reflection through the glass for positions relative to {@code camera}, as a column-major 4x4 matrix: put it
	 * after the camera's view rotation and the scene is drawn as the glass shows it.
	 */
	public static float[] reflection(double[] camera, double[] glass, int normalX, int normalZ) {
		double nx = normalX;
		double nz = normalZ;
		// The plane's offset from the camera along its normal: p' = p - 2 (p . n - d) n.
		double d = (glass[0] - camera[0]) * nx + (glass[2] - camera[2]) * nz;
		return new float[] {
				(float) (1 - 2 * nx * nx), 0, (float) (-2 * nx * nz), 0,
				0, 1, 0, 0,
				(float) (-2 * nz * nx), 0, (float) (1 - 2 * nz * nz), 0,
				(float) (2 * d * nx), 0, (float) (2 * d * nz), 1 };
	}

	/** Where {@code matrix} (column-major) puts {@code point}. */
	static double[] apply(float[] matrix, double[] point) {
		double[] out = new double[3];
		for (int row = 0; row < 3; row++) {
			out[row] = matrix[row] * point[0] + matrix[4 + row] * point[1] + matrix[8 + row] * point[2] + matrix[12 + row];
		}
		return out;
	}
}
