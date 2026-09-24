package com.vincenthuto.hemomancy.common.worldgen;

/**
 * Pure, hash-based shape for a single Cortical Drift island: a flat plateau top with gentle bumps,
 * an angular-noise rim, and a tapered underside studded with stalactite spikes. Minecraft-free and
 * closed-form (no {@link java.util.Random} advanced per call) so any chunk sampling any offset of
 * the same island gets the identical answer, matching {@link CorticalArchipelagoPlan}'s contract.
 */
public final class CorticalIslandShape {
	private static final int SPIKE_COUNT_MIN = 3;
	private static final int SPIKE_COUNT_MAX = 6;

	public enum Layer {
		MYELIN, SLATE, TISSUE, GANGLION
	}

	private CorticalIslandShape() {}

	/** Block layer at offset (dx,dy,dz) from the island's top-centre (node.x, node.y, node.z); null = air. */
	public static Layer sample(CorticalArchipelagoPlan.Node island, int dx, int dy, int dz) {
		double horizontalDist = Math.sqrt((double) dx * dx + (double) dz * dz);
		double angle = Math.atan2(dz, dx);
		double edgeRadius = edgeRadius(island, angle);
		if (horizontalDist > edgeRadius) {
			return null;
		}
		double normalized = horizontalDist / edgeRadius;

		int top = topHeight(island, dx, dz, normalized);
		if (dy > top) {
			return null;
		}

		double maxBase = maxBaseDepth(island);
		double depth = underside(island, dx, dz, normalized, maxBase);
		int bottom = -(int) Math.floor(depth);
		if (dy < bottom) {
			return null;
		}

		int depthFromTop = top - dy;
		Layer layer;
		if (depthFromTop <= 0) {
			layer = Layer.MYELIN;
		} else if (depthFromTop <= 3) {
			layer = Layer.SLATE;
		} else {
			layer = Layer.TISSUE;
		}
		if (layer != Layer.MYELIN) {
			double vein = hash4(dx, dy, dz, island.shapeSeed());
			if (vein < 1.0 / 9.0) {
				layer = Layer.GANGLION;
			}
		}
		return layer;
	}

	/** Downward extent below node.y, including spikes. radius*(1.4..2.6) base, spikes up to 1.5x more. */
	public static int maxDepth(CorticalArchipelagoPlan.Node island) {
		double maxBase = maxBaseDepth(island);
		return (int) Math.ceil(maxBase * 1.5);
	}

	/** Horizontal reach including edge noise, for bounding boxes (<= radius*1.4). */
	public static int maxReach(CorticalArchipelagoPlan.Node island) {
		return (int) Math.ceil(island.radius() * 1.25);
	}

	private static double maxBaseDepth(CorticalArchipelagoPlan.Node island) {
		double t = hash1(island.shapeSeed(), 991L);
		return island.radius() * (1.4 + t * (2.6 - 1.4));
	}

	private static double edgeRadius(CorticalArchipelagoPlan.Node island, double angle) {
		double noise = angularNoise(island.shapeSeed(), angle);
		return island.radius() * (1.0 + 0.25 * noise);
	}

	/** Smooth pseudo-noise in angle, in [-1,1], built from a few hashed harmonics. */
	private static double angularNoise(long seed, double angle) {
		double value = 0.0;
		double weight = 1.0;
		double weightSum = 0.0;
		for (int harmonic = 1; harmonic <= 3; harmonic++) {
			double phase = hash1(seed, harmonic * 7919L) * Math.PI * 2.0;
			value += weight * Math.sin(angle * harmonic + phase);
			weightSum += weight;
			weight *= 0.5;
		}
		return value / weightSum;
	}

	private static int topHeight(CorticalArchipelagoPlan.Node island, int dx, int dz, double normalized) {
		if (normalized > 0.6) {
			return 0;
		}
		// Fades to exactly 0 at the very centre so top-centre is always flat, growing toward the 60%
		// ring where bumps are allowed to reach their full +2 amplitude.
		double falloff = Math.min(1.0, normalized / 0.6);
		double bump = hash3(dx, dz, island.shapeSeed()) * 2.0 * falloff;
		return (int) Math.round(bump);
	}

	private static double underside(CorticalArchipelagoPlan.Node island, int dx, int dz, double normalized,
			double maxBase) {
		double base = maxBase * Math.pow(Math.max(0.0, 1.0 - normalized), 1.6);
		double spikeBoost = spikeBoost(island, dx, dz);
		return base * (1.0 + spikeBoost * 0.5);
	}

	private static double spikeBoost(CorticalArchipelagoPlan.Node island, int dx, int dz) {
		int spikeCount = SPIKE_COUNT_MIN
				+ (int) (hash1(island.shapeSeed(), 313L) * (SPIKE_COUNT_MAX - SPIKE_COUNT_MIN + 1));
		double boost = 0.0;
		double reach = island.radius() * 1.25;
		for (int i = 0; i < spikeCount; i++) {
			double angle = hash1(island.shapeSeed(), i * 101L + 17L) * Math.PI * 2.0;
			double dist = hash1(island.shapeSeed(), i * 211L + 31L) * reach * 0.7;
			double sx = Math.cos(angle) * dist;
			double sz = Math.sin(angle) * dist;
			double d = Math.sqrt((dx - sx) * (dx - sx) + (dz - sz) * (dz - sz));
			if (d < 3.0) {
				boost = Math.max(boost, (1.0 - d / 3.0));
			}
		}
		return boost;
	}

	/** Deterministic hash of a seed plus a salt into [0,1). */
	private static double hash1(long seed, long salt) {
		long h = seed ^ (salt * 0x9E3779B97F4A7C15L);
		h ^= (h >>> 33);
		h *= 0xFF51AFD7ED558CCDL;
		h ^= (h >>> 33);
		h *= 0xC4CEB9FE1A85EC53L;
		h ^= (h >>> 33);
		return ((h >>> 11) & ((1L << 53) - 1)) / (double) (1L << 53);
	}

	/** Smooth hash over integer (a,b) plus seed, in [-1,1], bilinearly interpolated between lattice points. */
	private static double hash3(int a, int b, long seed) {
		int a0 = Math.floorDiv(a, 4);
		int b0 = Math.floorDiv(b, 4);
		double fa = (a - a0 * 4) / 4.0;
		double fb = (b - b0 * 4) / 4.0;
		double v00 = latticeValue(a0, b0, seed);
		double v10 = latticeValue(a0 + 1, b0, seed);
		double v01 = latticeValue(a0, b0 + 1, seed);
		double v11 = latticeValue(a0 + 1, b0 + 1, seed);
		double smoothA = fa * fa * (3.0 - 2.0 * fa);
		double smoothB = fb * fb * (3.0 - 2.0 * fb);
		double top = v00 + (v10 - v00) * smoothA;
		double bottom = v01 + (v11 - v01) * smoothA;
		return top + (bottom - top) * smoothB;
	}

	private static double latticeValue(int a, int b, long seed) {
		long salt = (long) a * 668265263L ^ (long) b * 374761393L;
		return hash1(seed, salt) * 2.0 - 1.0;
	}

	private static double hash4(int dx, int dy, int dz, long seed) {
		long salt = (long) dx * 92821L ^ (long) dy * 68917L ^ (long) dz * 19349663L;
		return hash1(seed, salt);
	}
}
