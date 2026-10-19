package com.vincenthuto.hemomancy.common.worldgen;

/**
 * The Morphic Nursery's shape, as a pure function of a block offset from the arena centre (y = 0 is the arena's base
 * layer; the floor surface is y = 1, so creatures stand at y = 2).
 *
 * <p>A round flesh hollow rather than a fenced square: a flat mycelium floor around a bone-rimmed nectar pool, a few
 * organic nectar puddles, fleshy walls that curve up from a soft fillet into a low vault, bone ribs running up the walls
 * and across the vault toward an open oculus above the pool, shroomlight drips hanging from the vault and set into the
 * walls, and fungal growth gathered at the foot of the walls. The barrier shell of the old arena stays outside it.
 */
public final class MycophantNurseryLayout {
	public enum Material { AIR, BARRIER, SOIL, FLOOR, NECTAR, BONE, FLESH, CAP, LIGHT, GROWTH }

	public static final int HALF = 25;
	public static final int CEILING_Y = 18;
	public static final int POOL_RADIUS = 4;
	/** The fight's flat floor reaches at least this far; the walls start a little beyond it. */
	public static final int PLAY_RADIUS = 18;
	public static final double WALL_RADIUS = 21.0D;
	/** The vault's crown, over the pool, and the radius of its open oculus. */
	public static final int VAULT_CROWN = 16;
	public static final double OCULUS_RADIUS = 6.5D;
	/** Nothing hangs below this height over the floor: she rears to about seven blocks above it. */
	public static final int LOWEST_DRIP = 10;
	/** Where the candidate wakes, behind her: kept clear of puddles and growth. */
	public static final double ARRIVAL_Z = -11.5D;
	private static final int RIBS = 8;
	private static final int PUDDLES = 6;

	private MycophantNurseryLayout() {
	}

	public static Material at(int x, int y, int z) {
		if (y >= CEILING_Y) return Material.BARRIER;
		if (x <= -(HALF - 1) || x >= HALF - 1 || z <= -(HALF - 1) || z >= HALF - 1) return Material.BARRIER;
		double r = Math.sqrt(x * x + z * z);
		double angle = Math.atan2(z, x);
		double wall = wallRadius(angle);
		if (y <= 0) return r <= wall + 4.0D ? Material.SOIL : Material.BARRIER;
		int vault = vaultY(r);
		if (r > wall) {
			// inside the wall: flesh up through the vault's shell, a cap skin on top, open above
			if (y <= vault) return Material.FLESH;
			return y == vault + 1 ? Material.CAP : Material.AIR;
		}
		if (y == 1) return floor(x, z, r);
		// the vault shell over the hollow, ringed with bone at the oculus
		if (r >= OCULUS_RADIUS && y >= vault) {
			if (y > vault + 1) return Material.AIR;
			if (y == vault + 1) return Material.CAP;
			return r < OCULUS_RADIUS + 1.3D ? Material.BONE : Material.FLESH;
		}
		boolean rib = ribAt(angle, r);
		// ribs run along the underside of the vault and down the wall face
		if (rib && r >= OCULUS_RADIUS + 1.0D && y == vault - 1) return Material.BONE;
		boolean face = r > wall - 1.2D;
		if (face && rib && y < vault) return Material.BONE;
		// a soft fillet where floor meets wall, then the wall face with shroomlights set into it
		double fillet = wall - 2.6D;
		if (r > fillet && y - 1 <= Math.pow((r - fillet) / 2.6D, 2.0D) * 3.2D) return Material.FLESH;
		if (face && (y == 4 || y == 8) && hash(x, z, y) < 0.14D) return Material.LIGHT;
		if (face && y < vault) return Material.FLESH;
		// shroomlight drips hanging from the vault, never low enough to meet her crest
		if (r >= OCULUS_RADIUS + 2.0D && r < wall - 2.0D && y < vault) {
			int drip = dripLength(x, z);
			if (drip > 0 && y >= vault - drip && y >= LOWEST_DRIP)
				return y == Math.max(LOWEST_DRIP, vault - drip) && hash(x, z, 31) < 0.6D ? Material.LIGHT : Material.FLESH;
		}
		if (y == 2 && growthAt(x, z, r, wall)) return Material.GROWTH;
		return Material.AIR;
	}

	/** The vault's height over a radius: a low dome, highest over the pool. */
	public static int vaultY(double r) {
		double fall = r / (WALL_RADIUS + 2.0D);
		return VAULT_CROWN - (int) Math.round(fall * fall * 6.0D);
	}

	/** The wall's radius at an angle: it bulges and recedes so the hollow never reads as a circle drawn with a compass. */
	public static double wallRadius(double angle) {
		return WALL_RADIUS + Math.sin(angle * 5.0D + 1.3D) * 0.9D + Math.sin(angle * 11.0D + 0.4D) * 0.45D;
	}

	private static Material floor(int x, int z, double r) {
		if (r <= POOL_RADIUS + 0.35D) return Material.NECTAR;
		if (r <= POOL_RADIUS + 1.45D) return Material.BONE;
		return puddle(x, z) ? Material.NECTAR : Material.FLOOR;
	}

	/** A few soft-edged nectar puddles around the pool, never under the arrival point. */
	public static boolean puddle(int x, int z) {
		for (int i = 0; i < PUDDLES; i++) {
			double angle = Math.PI * 2.0D * i / PUDDLES + hash(i, 7, 3) * 0.6D;
			double distance = 10.0D + hash(i, 9, 5) * 5.5D;
			double px = Math.cos(angle) * distance, pz = Math.sin(angle) * distance;
			if (Math.hypot(px, pz - ARRIVAL_Z) < 5.0D) continue;
			double radius = 1.4D + hash(i, 11, 2) * 1.1D;
			double dx = x + 0.5D - px, dz = z + 0.5D - pz;
			// a little lumpy so it reads as a spill, not a disc
			double edge = radius + Math.sin(Math.atan2(dz, dx) * 3.0D + i) * 0.35D;
			if (dx * dx + dz * dz <= edge * edge) return true;
		}
		return false;
	}

	private static boolean ribAt(double angle, double r) {
		double step = Math.PI * 2.0D / RIBS;
		double offset = angle - step * 0.5D;
		double nearest = offset - Math.round(offset / step) * step;
		return Math.abs(nearest) * r < 0.75D;
	}

	private static int dripLength(int x, int z) {
		if (hash(x, z, 17) >= 0.045D) return 0;
		return 1 + (int) (hash(x, z, 19) * 4.0D);
	}

	/** Fungal growth gathers at the foot of the walls and thins out toward the open floor. */
	private static boolean growthAt(int x, int z, double r, double wall) {
		if (Math.hypot(x + 0.5D, z + 0.5D - ARRIVAL_Z) < 3.0D || puddle(x, z) || r <= POOL_RADIUS + 1.5D) return false;
		double nearWall = Math.max(0.0D, 1.0D - (wall - 2.6D - r) / 5.0D);
		return hash(x, z, 23) < 0.04D + nearWall * 0.32D;
	}

	/** Deterministic 0..1 noise for a block. */
	public static double hash(int x, int z, int salt) {
		int h = x * 374761393 + z * 668265263 + salt * 2147483629;
		h = (h ^ (h >>> 13)) * 1274126177;
		return ((h ^ (h >>> 16)) & 0xFFFF) / 65535.0D;
	}
}
