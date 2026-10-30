package com.vincenthuto.hemomancy.client.render.world;

import com.vincenthuto.hemomancy.common.world.HeartNodeFootprint;
import com.vincenthuto.hemomancy.common.world.HeartNodeRelation;
import com.vincenthuto.hemomancy.common.world.HeartNodeRules;
import com.vincenthuto.hemomancy.common.world.HeartNodeSettings;
import com.vincenthuto.hemomancy.common.world.LobeEnvelope;
import net.minecraft.core.BlockPos;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The CPU half of the heart-node spread ({@link HeartNodeSpreadRenderer}): what each corner of a face inside a sanctum
 * carries to the {@code heart_node_spread} shader, and how far the growth has reached. Pure: block positions and the
 * footprint only, so it is tested without a client.
 *
 * <p>A corner's vertex colour packs four values: red the body strength (Track 1's strength stretched to 1 at the heart
 * and 0 at the envelope's widest reach), green how deep the corner sits inside the envelope (blocks to the nearest lobe
 * edge, over {@link #EDGE_SPAN}), blue the signed distance to the face's nearest synapse thread (over
 * {@link #THREAD_SPAN}, 0.5 on the thread), and alpha the face's orientation weight: full on floors, fading up walls
 * within {@link #WALL_REACH} of the floor, and only frond tips on low ceilings beside a wall.
 */
public final class HeartNodeSpreadGeometry {
	/** Blocks of depth that green covers; the frond fray lives in the outer half of it. */
	public static final double EDGE_SPAN = 3.0D;
	/** Blocks either side of a thread that blue covers. */
	public static final double THREAD_SPAN = 2.0D;
	/** A corner far from every thread. */
	public static final double NO_THREAD = THREAD_SPAN;
	/** How far up a wall the layer climbs from the floor below it. */
	public static final double WALL_REACH = 1.5D;
	/** A ceiling carries tips only while the floor is this close below it. */
	public static final int CEILING_REACH = 3;
	/** The tips' weight at a ceiling corner touching a wall. */
	public static final double CEILING_TIP = 0.5D;
	/** A heartbeat: the pulse leaves the heart this often, on the node's own beat. */
	public static final int HEARTBEAT_TICKS = HeartNodeRules.HEARTBEAT_TICKS;
	/** Blocks the pulse travels in one tick (six a second). */
	public static final double PULSE_SPEED = 0.3D;
	/** A newly raised synapse spreads its lobe over this long. */
	public static final int APPEAR_TICKS = 200;
	/** A withered synapse's lobe recedes and greys over this long. */
	public static final int WITHER_TICKS = 60;
	/** The heart's growth front starts this wide at planting. */
	public static final double FRONT_START = 1.5D;
	/** A front this wide or more is grown: no leading rim. */
	public static final double GROWN = 1024.0D;
	/** Lobe slots the shader holds: the heart, then synapses in raised order, then receding ones. */
	public static final int LOBE_SLOTS = 8;
	/** An appearance time that reads as grown on any clock: synapses already standing when a sanctum comes in sight. */
	public static final long LONG_AGO = Long.MIN_VALUE / 4;

	private HeartNodeSpreadGeometry() {}

	/** Whether a viewer standing as {@code relation} sees this sanctum's spread at all. */
	public static boolean drawsFor(HeartNodeSettings settings, HeartNodeRelation relation) {
		if (!settings.has(HeartNodeSettings.SPREAD)) return false;
		return relation == HeartNodeRelation.OWNER || settings.has(HeartNodeSettings.SHOW_STRANGERS);
	}

	/** Track 1's strength stretched from 1 at the heart to 0 at the envelope's widest reach. */
	public static double bodyStrength(HeartNodeFootprint footprint, double x, double z) {
		BlockPos heart = footprint.heart();
		if (heart == null) return 0.0D;
		double max = footprint.maxRadius();
		if (max <= 0.0D) return 1.0D;
		double dx = x - (heart.getX() + 0.5D);
		double dz = z - (heart.getZ() + 0.5D);
		return 1.0D - Math.min(1.0D, Math.sqrt(dx * dx + dz * dz) / max);
	}

	/**
	 * Blocks from {@code (x, z)} to the nearest edge of the lobes whose band holds {@code cellY}, the deepest of them;
	 * negative outside every lobe.
	 */
	public static double edgeDepth(HeartNodeFootprint footprint, double x, double z, int cellY) {
		double best = Double.NEGATIVE_INFINITY;
		BlockPos heart = footprint.heart();
		if (heart != null) best = depthIn(heart, HeartNodeRules.HEART_RADIUS, x, z, cellY);
		for (BlockPos synapse : footprint.synapses()) {
			best = Math.max(best, depthIn(synapse, HeartNodeRules.SYNAPSE_RADIUS, x, z, cellY));
		}
		return best;
	}

	private static double depthIn(BlockPos centre, double radius, double x, double z, int cellY) {
		if (Math.abs(cellY - centre.getY()) > HeartNodeRules.VERTICAL_BAND) return Double.NEGATIVE_INFINITY;
		double dx = x - (centre.getX() + 0.5D);
		double dz = z - (centre.getZ() + 0.5D);
		return radius - Math.sqrt(dx * dx + dz * dz);
	}

	// ------------------------------------------------------------------ threads

	/** One leg of a synapse thread across the ground, and how far the pulse has run when it reaches the leg's start. */
	public record Thread(double fromX, double fromZ, double toX, double toZ, double startLength) {
		public double length() {
			return Math.hypot(toX - fromX, toZ - fromZ);
		}

		/** How far along the leg {@code (x, z)} projects, 0 at its start and 1 at its synapse. */
		public double along(double x, double z) {
			double lx = toX - fromX, lz = toZ - fromZ;
			double lengthSqr = lx * lx + lz * lz;
			if (lengthSqr <= 1.0E-9D) return 0.0D;
			return ((x - fromX) * lx + (z - fromZ) * lz) / lengthSqr;
		}

		/**
		 * The signed distance from the leg, positive to its left. Between the ends it is the perpendicular distance, so
		 * it runs linearly across a face and the shader finds the zero line inside the face; past either end it is the
		 * distance to that end, keeping the side's sign.
		 */
		public double signedDistance(double x, double z) {
			double length = length();
			if (length <= 1.0E-9D) return Math.hypot(x - fromX, z - fromZ);
			double perpendicular = ((toX - fromX) * (z - fromZ) - (toZ - fromZ) * (x - fromX)) / length;
			double t = along(x, z);
			if (t >= 0.0D && t <= 1.0D) return perpendicular;
			double endX = t < 0.0D ? fromX : toX, endZ = t < 0.0D ? fromZ : toZ;
			double toEnd = Math.hypot(x - endX, z - endZ);
			return perpendicular < 0.0D ? -toEnd : toEnd;
		}

		public double distance(double x, double z) {
			return Math.abs(signedDistance(x, z));
		}

		/** The pulse's run at {@code (x, z)}: the run at the leg's start plus how far along the leg it projects. */
		public double pathLength(double x, double z) {
			return startLength + Math.max(0.0D, Math.min(1.0D, along(x, z))) * length();
		}
	}

	/**
	 * The thread legs of every connected synapse, each from the lobe centre it chains from (the heart or an earlier
	 * synapse) to the synapse, between block centres on the ground plane, in raised order.
	 */
	public static List<Thread> threads(HeartNodeFootprint footprint) {
		BlockPos heart = footprint.heart();
		if (heart == null) return List.of();
		Map<BlockPos, Double> arrivals = arrivals(footprint);
		List<Thread> threads = new ArrayList<>();
		for (BlockPos synapse : footprint.connectedSynapses()) {
			BlockPos from = footprint.chainedFrom(synapse);
			if (from == null) continue;
			threads.add(new Thread(from.getX() + 0.5D, from.getZ() + 0.5D, synapse.getX() + 0.5D, synapse.getZ() + 0.5D,
					arrivals.getOrDefault(from, 0.0D)));
		}
		return threads;
	}

	/**
	 * How far the pulse runs from the heart to each connected synapse, along the chain of threads; the heart's own run
	 * is 0. A cut-off synapse has none.
	 */
	public static Map<BlockPos, Double> arrivals(HeartNodeFootprint footprint) {
		Map<BlockPos, Double> arrivals = new HashMap<>();
		BlockPos heart = footprint.heart();
		if (heart == null) return arrivals;
		arrivals.put(heart, 0.0D);
		List<BlockPos> connected = footprint.connectedSynapses();
		// Each pass settles the synapses whose lobe centre is settled; a chain of n needs at most n passes.
		for (int pass = 0; pass < connected.size() && arrivals.size() <= connected.size(); pass++) {
			for (BlockPos synapse : connected) {
				if (arrivals.containsKey(synapse)) continue;
				BlockPos from = footprint.chainedFrom(synapse);
				if (from == null || !arrivals.containsKey(from)) continue;
				arrivals.put(synapse, arrivals.get(from) + Math.sqrt(LobeEnvelope.distanceSqr(synapse, from)));
			}
		}
		return arrivals;
	}

	/** The leg nearest {@code (x, z)}, or null with none within {@link #THREAD_SPAN}. */
	@Nullable
	public static Thread nearestThread(List<Thread> threads, double x, double z) {
		Thread nearest = null;
		double best = THREAD_SPAN;
		for (Thread thread : threads) {
			double distance = thread.distance(x, z);
			if (distance < best) {
				best = distance;
				nearest = thread;
			}
		}
		return nearest;
	}

	// ------------------------------------------------------------------ orientation weights

	/** A wall corner {@code height} blocks above the floor below it: full at the floor, nothing past the reach. */
	public static double wallWeight(double height) {
		return Math.max(0.0D, Math.min(1.0D, 1.0D - height / WALL_REACH));
	}

	/**
	 * A ceiling corner: tips only beside a wall, and only while the floor is within {@link #CEILING_REACH} below (the
	 * number of open cells under the ceiling, {@code floorGap}).
	 */
	public static double ceilingWeight(boolean besideWall, int floorGap) {
		return besideWall && floorGap <= CEILING_REACH ? CEILING_TIP : 0.0D;
	}

	// ------------------------------------------------------------------ growth

	/** The heart's growth front: {@link #FRONT_START} at planting, the heart radius at maturity, then grown. */
	public static double heartFront(long plantedAt, double gameTime) {
		double age = gameTime - plantedAt;
		if (age >= HeartNodeRules.MATURE_TICKS) return GROWN;
		double t = Math.max(0.0D, age) / HeartNodeRules.MATURE_TICKS;
		// Fast at first, slowing as it nears the lobe's edge.
		double eased = 1.0D - (1.0D - t) * (1.0D - t);
		return FRONT_START + (HeartNodeRules.HEART_RADIUS + 1.0D - FRONT_START) * eased;
	}

	/** A synapse lobe's front: nothing when raised, its full radius after {@link #APPEAR_TICKS}, then grown. */
	public static double synapseFront(long appearedAt, double gameTime) {
		double age = gameTime - appearedAt;
		if (age >= APPEAR_TICKS) return GROWN;
		double t = Math.max(0.0D, age) / APPEAR_TICKS;
		double eased = 1.0D - (1.0D - t) * (1.0D - t);
		return (HeartNodeRules.SYNAPSE_RADIUS + 1.0D) * eased;
	}

	/** A withered lobe's life, from 1 as it withers to 0 after {@link #WITHER_TICKS}. */
	public static double witherLife(long witheredAt, double gameTime) {
		return Math.max(0.0D, Math.min(1.0D, 1.0D - (gameTime - witheredAt) / WITHER_TICKS));
	}

	public static boolean withered(long witheredAt, long gameTime) {
		return gameTime - witheredAt >= WITHER_TICKS;
	}

	// ------------------------------------------------------------------ pulse

	/** Ticks into a heartbeat at which a synapse's flare peaks, and how wide it is (heart_node_spread.fsh). */
	public static final double FLARE_PEAK = 4.5D;
	public static final double FLARE_WIDTH = 3.0D;

	/** A soft bump {@code width} ticks wide, {@code centre} ticks into a heartbeat: the shader's {@code beatBump}. */
	public static double beatBump(double phase, double centre, double width) {
		double x = (phase - centre) / width;
		return Math.exp(-x * x);
	}

	/**
	 * How brightly a synapse flares at {@code time} when the pulse runs {@code arrive} blocks from the heart to reach it:
	 * the shader's flare at the synapse itself, so the crystal lights as the ground around it does.
	 */
	public static double flare(double time, double arrive) {
		double since = time - arrive / PULSE_SPEED;
		double phase = since - Math.floor(since / HEARTBEAT_TICKS) * HEARTBEAT_TICKS;
		return beatBump(phase, FLARE_PEAK, FLARE_WIDTH);
	}

	// ------------------------------------------------------------------ packing

	/** One corner's vertex colour, as ARGB. */
	public static int pack(double strength, double edgeDepth, double threadDistance, double weight) {
		int red = channel(strength);
		int green = channel(edgeDepth / EDGE_SPAN);
		int blue = channel(Math.max(-1.0D, Math.min(1.0D, threadDistance / THREAD_SPAN)) * 0.5D + 0.5D);
		int alpha = channel(weight);
		return alpha << 24 | red << 16 | green << 8 | blue;
	}

	private static int channel(double value) {
		return (int) Math.round(Math.max(0.0D, Math.min(1.0D, value)) * 255.0D);
	}
}
