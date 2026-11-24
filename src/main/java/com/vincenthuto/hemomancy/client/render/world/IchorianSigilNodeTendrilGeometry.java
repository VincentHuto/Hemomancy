package com.vincenthuto.hemomancy.client.render.world;

import com.vincenthuto.hemomancy.common.rite.harbinger.ActiveRiteData;
import com.vincenthuto.hemomancy.common.rite.sigil.AwakenedIchorianSigilMotion;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The growths that lift from an awakened sigil's nodes toward its living form. Each node puts up one trunk, fleshy
 * where it leaves the gouge and drawn out to a hair, with two thinner offshoots that peel away from it partway up.
 * A trunk leans its own way, writhes as two slow waves travel up it, swells where a pulse passes and hooks over at
 * the tip; no two are the same length. Every offset from the straight line to the hover point is kept square to
 * that line, so nothing gets closer to the form than the share of the way it was given.
 *
 * <p>A growth belongs to its gouge. It is rooted on the cut's own surface; a root runs down the middle of every link
 * that meets the node, a vessel lying in the blood that thickens as it nears the node and lifts into the trunk there;
 * and root and trunk leave the gouge in the blood's own colour, turning to the form's crimson only as the trunk climbs.
 */
public final class IchorianSigilNodeTendrilGeometry {
	/** A trunk and its two offshoots. */
	public static final int TENDRILS_PER_NODE = 3;
	public static final int MAX_NODES = 12;
	/** Share of the way from a node to the hover point that the longest fully risen trunk reaches. */
	public static final float REACH = 0.6F;
	/** A trunk's half-width where it leaves the gouge. */
	public static final float ROOT_HALF_WIDTH = 0.05F;

	private static final int TRUNK_SEGMENTS = 18;
	private static final int OFFSHOOT_SEGMENTS = 9;
	private static final float ROOT_OPACITY = 0.78F;
	/** The shortest trunk is this share of the longest. */
	private static final double SHORTEST = 0.68D;
	private static final double BOW = 0.16D;
	private static final double BOW_VARIETY = 0.16D;
	/** How far the travelling waves throw a trunk's tip, in blocks. */
	private static final double WRITHE = 0.085D;
	private static final double WRITHE_RATE = 0.085D;
	private static final double LEAN = 0.045D;
	private static final double HOOK = 0.11D;
	/** How much a passing pulse swells the trunk. */
	private static final double SWELL = 0.24D;
	private static final double SWELL_RATE = 0.13D;
	/** A root runs this far down a link from its node, or 0.45 of the link if that is shorter. */
	private static final double ROOT_RUN = 1.1D;
	private static final int ROOT_SEGMENTS = 10;
	/** A root lies this far over the ground its gouge is cut into, just over the gouge's own strip. */
	private static final double ROOT_LIFT = 0.012D;
	/** How far a root wanders off the link's centre line; the cut is 0.16 to either side. */
	private static final double ROOT_WANDER = 0.045D;
	/** A trunk has turned from the blood's colour to the form's crimson by this share of the way up. */
	private static final double TINT_TURN = 0.6D;
	private static final int CRIMSON = 0x99030A;
	private static final Vec3 UP = new Vec3(0.0D, 1.0D, 0.0D);

	private IchorianSigilNodeTendrilGeometry() {
	}

	/** Unique link endpoints of one awakened sigil, at most {@link #MAX_NODES}, picked evenly when there are more. */
	public static List<Vec3> nodes(List<ActiveRiteData.SigilSegment> sigilSegments) {
		Map<NodeKey, Vec3> unique = new LinkedHashMap<>();
		for (ActiveRiteData.SigilSegment segment : sigilSegments) {
			add(unique, new Vec3(segment.startX(), segment.startY(), segment.startZ()));
			add(unique, new Vec3(segment.endX(), segment.endY(), segment.endZ()));
		}
		List<Vec3> all = new ArrayList<>(unique.values());
		if (all.size() <= MAX_NODES) return List.copyOf(all);
		List<Vec3> picked = new ArrayList<>(MAX_NODES);
		for (int index = 0; index < MAX_NODES; index++) {
			picked.add(all.get(index * all.size() / MAX_NODES));
		}
		return List.copyOf(picked);
	}

	/** The point the tendrils lean toward: the nodes' centroid, the form's hover height above their mean height. */
	public static Vec3 hoverPoint(List<Vec3> nodes) {
		if (nodes.isEmpty()) return Vec3.ZERO;
		double x = 0.0D;
		double y = 0.0D;
		double z = 0.0D;
		for (Vec3 node : nodes) {
			x += node.x;
			y += node.y;
			z += node.z;
		}
		int count = nodes.size();
		return new Vec3(x / count, y / count + AwakenedIchorianSigilMotion.HOVER_HEIGHT, z / count);
	}

	public static List<Strand> strands(List<Vec3> nodes, Vec3 hoverPoint, long seed, float time, float rise) {
		return strands(nodes, hoverPoint, seed, time, rise, -1);
	}

	/** The growths, leaving the gouge in bloodColor (packed RGB; -1 for the ribbon's own crimsons throughout). */
	public static List<Strand> strands(List<Vec3> nodes, Vec3 hoverPoint, long seed, float time, float rise,
			int bloodColor) {
		if (rise <= 0.0F || nodes.isEmpty()) return List.of();
		double risen = Math.min(1.0F, rise);
		List<Strand> strands = new ArrayList<>(nodes.size() * TENDRILS_PER_NODE);
		for (int nodeIndex = 0; nodeIndex < nodes.size(); nodeIndex++) {
			Vec3 root = nodes.get(nodeIndex);
			Vec3 toHover = hoverPoint.subtract(root);
			double length = toHover.length();
			if (length < 1.0E-4D) continue;
			Trunk trunk = new Trunk(root, toHover, length, hoverPoint, seed, nodeIndex, time, risen, bloodColor);

			List<Joint> joints = new ArrayList<>(TRUNK_SEGMENTS + 1);
			for (int segment = 0; segment <= TRUNK_SEGMENTS; segment++) {
				double progress = segment / (double) TRUNK_SEGMENTS;
				joints.add(new Joint(trunk.center(progress), trunk.halfWidth(progress), trunk.opacity(progress),
						trunk.tint(progress, 1.0F), false));
			}
			// Even indices draw as bright crimson, odd as dark blood: the trunk bright, its offshoots darker.
			strands.add(new Strand(nodeIndex * 4, List.copyOf(joints)));
			for (int offshoot = 0; offshoot < TENDRILS_PER_NODE - 1; offshoot++) {
				strands.add(new Strand(nodeIndex * 4 + 1 + offshoot * 2, trunk.offshoot(offshoot)));
			}
		}
		return List.copyOf(strands);
	}

	/**
	 * The roots that feed the growths: for every link that meets a node, a vessel lying down the middle of that
	 * link's cut, nothing at its far end and as thick as the trunk's foot where it reaches the node. Nodes are the
	 * points the growths stand on; a link's own nodes are matched to them by position over the ground.
	 */
	public static List<Strand> roots(List<ActiveRiteData.SigilSegment> sigilSegments, List<Vec3> nodes, long seed,
			float time, float rise, int bloodColor) {
		if (rise <= 0.0F || nodes.isEmpty()) return List.of();
		double risen = Math.min(1.0F, rise);
		List<Strand> roots = new ArrayList<>();
		for (int nodeIndex = 0; nodeIndex < nodes.size(); nodeIndex++) {
			Vec3 node = nodes.get(nodeIndex);
			int link = 0;
			for (ActiveRiteData.SigilSegment segment : sigilSegments) {
				boolean atStart = over(node, segment.startX(), segment.startZ());
				if (!atStart && !over(node, segment.endX(), segment.endZ())) continue;
				double runX = atStart ? segment.endX() - segment.startX() : segment.startX() - segment.endX();
				double climb = atStart ? segment.endY() - segment.startY() : segment.startY() - segment.endY();
				double runZ = atStart ? segment.endZ() - segment.startZ() : segment.startZ() - segment.endZ();
				double linkLength = Math.hypot(runX, runZ);
				if (linkLength < 0.05D) continue;
				runX /= linkLength;
				runZ /= linkLength;
				double run = Math.min(ROOT_RUN, linkLength * 0.45D) * risen;
				double phase = unit(seed, nodeIndex, 60 + link) * Math.PI * 2.0D;
				List<Joint> joints = new ArrayList<>(ROOT_SEGMENTS + 1);
				for (int step = 0; step <= ROOT_SEGMENTS; step++) {
					// From the root's far end, out in the link, in to the node.
					double gathered = step / (double) ROOT_SEGMENTS;
					double out = run * (1.0D - gathered);
					// It wanders within the cut and comes true to the centre line at the node.
					double wander = Math.sin(out * 6.5D + phase) * ROOT_WANDER * Math.sin(Math.PI * gathered);
					Vec3 center = new Vec3(node.x + runX * out - runZ * wander,
							node.y + climb * (out / linkLength) + ROOT_LIFT,
							node.z + runZ * out + runX * wander);
					// The same pulse that swells the trunk runs in along the root toward it.
					double pulse = Math.sin(out * 8.0D + time * SWELL_RATE + phase);
					double width = ROOT_HALF_WIDTH * Math.pow(gathered, 0.8D)
							* (1.0D + SWELL * pulse * Math.sin(Math.PI * gathered));
					joints.add(new Joint(center, (float) Math.min(ROOT_HALF_WIDTH, width),
							(float) (ROOT_OPACITY * gathered), bloodColor, true));
				}
				roots.add(new Strand(nodeIndex * 16 + link * 2, List.copyOf(joints)));
				link++;
			}
		}
		return List.copyOf(roots);
	}

	private static boolean over(Vec3 node, double x, double z) {
		return Math.abs(node.x - x) < 1.0E-3D && Math.abs(node.z - z) < 1.0E-3D;
	}

	/** One node's trunk: where it is, how wide and how solid, at any share of the way up it. */
	private static final class Trunk {
		private final Vec3 root;
		private final Vec3 toHover;
		private final Vec3 sway;
		private final Vec3 lift;
		private final Vec3 bowDirection;
		private final Vec3 hookDirection;
		private final long seed;
		private final int nodeIndex;
		private final double time;
		private final double risen;
		private final double reach;
		private final double bow;
		private final double reachBlocks;
		private final int bloodColor;

		Trunk(Vec3 root, Vec3 toHover, double length, Vec3 hoverPoint, long seed, int nodeIndex, double time,
				double risen, int bloodColor) {
			this.bloodColor = bloodColor;
			this.root = root;
			this.toHover = toHover;
			this.seed = seed;
			this.nodeIndex = nodeIndex;
			this.time = time;
			this.risen = risen;
			Vec3 along = toHover.scale(1.0D / length);
			this.sway = square(along.cross(UP), new Vec3(1.0D, 0.0D, 0.0D));
			this.lift = along.cross(sway).normalize();
			Vec3 outward = new Vec3(root.x - hoverPoint.x, 0.0D, root.z - hoverPoint.z);
			outward = square(outward.subtract(along.scale(outward.dot(along))), lift.scale(-1.0D));
			// Each trunk bows mostly outward but off to its own side, and hooks over its own way.
			this.bowDirection = turned(outward, along.cross(outward), (unit(seed, nodeIndex, 1) - 0.5D) * 1.9D);
			this.hookDirection = turned(sway, lift, unit(seed, nodeIndex, 2) * Math.PI * 2.0D);
			this.reach = REACH * risen * (SHORTEST + (1.0D - SHORTEST) * unit(seed, nodeIndex, 3));
			this.bow = (BOW + BOW_VARIETY * unit(seed, nodeIndex, 4)) * Math.min(1.0D, length / 3.0D);
			this.reachBlocks = reach * length;
		}

		Vec3 center(double progress) {
			return root.add(toHover.scale(reach * progress)).add(offset(progress));
		}

		/** The trunk's drift off the straight line to the hover point, always square to that line. */
		private Vec3 offset(double progress) {
			double phase = unit(seed, nodeIndex, 5) * Math.PI * 2.0D;
			double second = unit(seed, nodeIndex, 6) * Math.PI * 2.0D;
			// Anchored in the gouge: nothing moves at the root, and the tip moves most.
			double free = Math.pow(progress, 1.3D);
			double writhe = WRITHE * free * Math.min(1.0D, reachBlocks / 1.2D);
			Vec3 offset = bowDirection.scale(Math.sin(Math.PI * progress) * bow * risen)
					.add(sway.scale(Math.sin(progress * 5.0D - time * WRITHE_RATE + phase) * writhe))
					.add(lift.scale(Math.cos(progress * 3.4D - time * WRITHE_RATE * 0.7D + second) * writhe * 0.8D))
					.add(sway.scale(Math.sin(time * 0.021D + second) * LEAN * progress));
			if (progress > 0.68D) {
				// The tip hooks over, tighter while the trunk is still unfurling.
				double curl = (progress - 0.68D) / 0.32D;
				offset = offset.add(hookDirection.scale(curl * curl * HOOK * (1.0D + 2.0D * (1.0D - risen))));
			}
			return offset;
		}

		float halfWidth(double progress) {
			double remaining = 1.0D - progress;
			// A fleshy flare where it leaves the gouge, then a long thinning to a hair.
			double shape = Math.pow(remaining, 1.25D) * 0.6D + 0.4D * Math.exp(-progress * 9.0D) * remaining;
			double pulse = Math.sin(progress * 9.0D - time * SWELL_RATE + unit(seed, nodeIndex, 7) * Math.PI * 2.0D);
			// No swell at the root itself, so it sits steady in the gouge.
			double swell = 1.0D + SWELL * pulse * Math.min(1.0D, progress * 5.0D);
			return (float) Math.min(ROOT_HALF_WIDTH, ROOT_HALF_WIDTH * shape * swell);
		}

		float opacity(double progress) {
			return (float) (ROOT_OPACITY * Math.pow(1.0D - progress, 0.8D));
		}

		/** The blood's colour at the gouge, turned to crimson by TINT_TURN of the way up; -1 if no blood colour. */
		int tint(double progress, float brightness) {
			if (bloodColor < 0) return -1;
			double turn = Math.min(1.0D, progress / TINT_TURN);
			turn = turn * turn * (3.0D - 2.0D * turn);
			return mix(bloodColor, CRIMSON, turn, brightness);
		}

		/** A thinner growth that leaves the trunk partway up and peels off to one side, curling as it goes. */
		List<Joint> offshoot(int which) {
			double from = (which == 0 ? 0.2D : 0.42D) + 0.2D * unit(seed, nodeIndex, 10 + which);
			double span = Math.min(1.0D - from, 0.3D + 0.16D * unit(seed, nodeIndex, 20 + which));
			double angle = unit(seed, nodeIndex, 30) * Math.PI * 2.0D + which * (Math.PI * 0.8D + 0.5D);
			Vec3 peel = turned(sway, lift, angle);
			Vec3 curlAxis = turned(sway, lift, angle + Math.PI * 0.5D);
			double spread = (0.2D + 0.16D * unit(seed, nodeIndex, 40 + which)) * Math.min(1.0D, reachBlocks / 1.2D);
			double phase = unit(seed, nodeIndex, 50 + which) * Math.PI * 2.0D;
			float rootWidth = halfWidth(from) * 0.7F;
			float rootOpacity = opacity(from);
			List<Joint> joints = new ArrayList<>(OFFSHOOT_SEGMENTS + 1);
			for (int segment = 0; segment <= OFFSHOOT_SEGMENTS; segment++) {
				double grown = segment / (double) OFFSHOOT_SEGMENTS;
				// It leaves along the trunk and only then swings away, so the fork has no elbow.
				Vec3 center = center(from + grown * span)
						.add(peel.scale(grown * grown * spread))
						.add(curlAxis.scale(Math.sin(grown * Math.PI * 1.2D + time * WRITHE_RATE + phase)
								* 0.05D * grown));
				float remaining = (float) (1.0D - grown);
				joints.add(new Joint(center, rootWidth * (float) Math.pow(remaining, 1.1D), rootOpacity * remaining,
						tint(from + grown * span, 0.62F), false));
			}
			return List.copyOf(joints);
		}
	}

	private static int mix(int from, int to, double share, float brightness) {
		int red = channel(from >> 16, to >> 16, share, brightness);
		int green = channel(from >> 8, to >> 8, share, brightness);
		int blue = channel(from, to, share, brightness);
		return red << 16 | green << 8 | blue;
	}

	private static int channel(int from, int to, double share, float brightness) {
		double value = ((from & 255) + ((to & 255) - (from & 255)) * share) * brightness;
		return (int) Math.max(0.0D, Math.min(255.0D, Math.round(value)));
	}

	/** The unit vector first turned by angle toward second (both unit, square to each other). */
	private static Vec3 turned(Vec3 first, Vec3 second, double angle) {
		return first.scale(Math.cos(angle)).add(second.scale(Math.sin(angle)));
	}

	private static void add(Map<NodeKey, Vec3> unique, Vec3 point) {
		unique.putIfAbsent(new NodeKey(Math.round(point.x * 1000.0D), Math.round(point.y * 1000.0D),
				Math.round(point.z * 1000.0D)), point);
	}

	private static Vec3 square(Vec3 vector, Vec3 fallback) {
		return vector.lengthSqr() < 1.0E-8D ? fallback : vector.normalize();
	}

	/** 0..1, steady for one trait of one node's growth of one sigil. */
	private static double unit(long seed, int nodeIndex, int trait) {
		long hash = seed * 0x9E3779B97F4A7C15L + nodeIndex * 31L + trait * 0x632BE59BD9B4E019L;
		hash ^= hash >>> 29;
		hash *= 0xBF58476D1CE4E5B9L;
		hash ^= hash >>> 32;
		return (hash & 0xFFFFL) / 65536.0D;
	}

	private record NodeKey(long x, long y, long z) {
	}

	public record Strand(int index, List<Joint> joints) implements SanguineTendrilRibbonRenderer.Strand {
		@Override
		public boolean crimsonVeins() {
			return true;
		}
	}

	/** Tint is the joint's own packed RGB, or -1 for the ribbon's crimson; a flat joint lies on the ground. */
	public record Joint(Vec3 center, float halfWidth, float opacity, int tint, boolean groundAligned)
			implements SanguineTendrilRibbonRenderer.Joint {
	}
}
