package com.vincenthuto.hemomancy.common.tile.harbinger.crafting.engram;

import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationRank;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.engram.EngramBoard.NodeKind;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import static com.vincenthuto.hemomancy.common.tile.harbinger.crafting.engram.EngramSigilGeometry.*;

/**
 * Lays an engram for a weaver: the solution first, then the board dressed around it. Fragments are placed and each
 * colour is threaded home as a tree (later fragments ply into the colour's earlier lanes), grey fragments through a
 * prism of their true colour; only then do scars, valves, decoy prisms and intrusions fill what the solution left
 * free. The solution and its span cost are kept on the board, so a weave can be refused before it starts, and fungal
 * pressure can avoid the reference path. Deterministic per seed.
 */
public final class EngramBoardGenerator {
	private static final int ATTEMPTS = 80;

	private EngramBoardGenerator() {
	}

	/** Each weaver recalls their own engram of a memory: the world, the recipe and the weaver choose it. */
	public static long seed(long worldSeed, String recipeId, UUID weaver) {
		long h = mix(worldSeed ^ 0x9E3779B97F4A7C15L);
		h = mix(h ^ recipeId.hashCode() * 0xC2B2AE3D27D4EB4FL);
		h = mix(h ^ weaver.getMostSignificantBits());
		return mix(h ^ weaver.getLeastSignificantBits());
	}

	private static long mix(long z) {
		z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
		z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
		return z ^ (z >>> 31);
	}

	/** How many false fragments an engram of this rank recalls, {fewest, most}. */
	public static int[] intrusionRange(EnumManipulationRank rank) {
		return switch (rank) {
			case HUMILIS -> new int[]{0, 0};
			case MEDIOCRITAS, SUMMA -> new int[]{1, 1};
			case MAGISTER -> new int[]{1, 2};
			case PERFECTUS -> new int[]{2, 3};
		};
	}

	private static int[] scarRange(EnumManipulationRank rank) {
		return switch (rank) {
			case HUMILIS -> new int[]{1, 3};
			case MEDIOCRITAS, SUMMA -> new int[]{2, 4};
			case MAGISTER, PERFECTUS -> new int[]{3, 5};
		};
	}

	private static boolean atLeast(EnumManipulationRank rank, EnumManipulationRank floor) {
		return rank.ordinal() >= floor.ordinal();
	}

	public static EngramBoard generate(long seed, int[] counts, EnumManipulationRank rank) {
		Random random = new Random(seed);
		int total = 0;
		for (int c : counts) total += c;
		for (int attempt = 0; attempt < ATTEMPTS; attempt++) {
			// Each round of failures relaxes one constraint: notches open first, then the wheel unkeys.
			boolean keyed = atLeast(rank, EnumManipulationRank.SUMMA) && attempt < ATTEMPTS * 3 / 4;
			boolean notches = total > TIPS || rank == EnumManipulationRank.PERFECTUS || attempt >= ATTEMPTS / 2;
			boolean strictIntrusions = attempt < ATTEMPTS - 10;
			EngramBoard board = attempt(random, counts, rank, keyed, notches, true, strictIntrusions);
			if (board != null) return board;
		}
		EngramBoard board = attempt(random, counts, rank, false, true, false, false);
		if (board == null) throw new IllegalStateException("No engram for " + Arrays.toString(counts) + " at " + rank);
		return board;
	}

	private static EngramBoard attempt(Random random, int[] counts, EnumManipulationRank rank, boolean keyed,
			boolean notches, boolean greysAllowed, boolean strictIntrusions) {
		EngramBoard board = EngramBoard.blank(keyed, random.nextInt(FACES));
		int total = 0;
		for (int c : counts) total += c;
		boolean crowded = total > TIPS;
		// A Humilis engram is a first lesson: its lanes are the shortest the board allows.
		boolean shortest = rank == EnumManipulationRank.HUMILIS;
		boolean[] used = new boolean[NODES];
		int tendencies = 0;
		for (int c : counts) if (c > 0) tendencies++;
		// With six or more tendencies nearly every face is claimed and each needs its own notch: walking them in turn
		// around the ring, each clockwise, always leaves the next one a way out.
		boolean tight = tendencies >= 6;
		used[VESSEL] = true;
		List<Integer> order = new ArrayList<>();
		for (int t = 0; t < 8; t++) if (counts[t] > 0) order.add(t);
		Collections.shuffle(order, random);

		// Each colour's way into the Vessel is claimed before any walk, so no walk wanders across another's face.
		int[] entries = new int[8];
		Arrays.fill(entries, -1);
		List<Integer> inners = new ArrayList<>();
		for (int n = 16; n < 24; n++) inners.add(n);
		Collections.shuffle(inners, random);
		for (int colour : order) {
			int inner = keyed ? spokeInner(colour + board.rotation()) : inners.removeFirst();
			if (keyed) inners.remove(Integer.valueOf(inner));
			entries[colour] = inner;
			used[inner] = true;
		}
		if (tight) {
			int start = random.nextInt(FACES);
			order.sort((a, b) -> Integer.compare(Math.floorMod(entries[a] - 16 - start, FACES), Math.floorMod(entries[b] - 16 - start, FACES)));
		}

		// A grey fragment is never the first of a tendency that has others: they ply into the coloured tree instead.
		boolean[][] grey = new boolean[8][];
		List<int[]> greyCandidates = new ArrayList<>();
		for (int t = 0; t < 8; t++) {
			grey[t] = new boolean[counts[t]];
			for (int k = counts[t] == 1 ? 0 : 1; k < counts[t]; k++) greyCandidates.add(new int[]{t, k});
		}
		if (greysAllowed && atLeast(rank, EnumManipulationRank.MAGISTER) && !greyCandidates.isEmpty()) {
			Collections.shuffle(greyCandidates, random);
			int greys = Math.min(greyCandidates.size(), rank == EnumManipulationRank.MAGISTER ? 1 : 1 + random.nextInt(2));
			for (int i = 0; i < greys; i++) grey[greyCandidates.get(i)[0]][greyCandidates.get(i)[1]] = true;
		}

		List<Set<Integer>> trees = new ArrayList<>();
		for (int t = 0; t < 8; t++) trees.add(new HashSet<>());
		Set<Integer> prisms = new HashSet<>();
		Set<Integer> greyNodes = new HashSet<>();
		for (int colour : order) {
			for (int k = 0; k < counts[colour]; k++) {
				boolean isGrey = grey[colour][k];
				int origin;
				boolean intoVessel;
				List<Integer> walk = null;
				if (k == 0) {
					origin = entries[colour];
					intoVessel = true;
					walk = walk(origin, used, random, notches, crowded || tight, tight, shortest, isGrey ? 2 : 1);
				} else if (!keyed && !inners.isEmpty() && random.nextInt(3) == 0) {
					// An open board lets a second lane of a tendency find its own face instead of plying.
					origin = inners.removeFirst();
					used[origin] = true;
					intoVessel = true;
					walk = walk(origin, used, random, notches, crowded || tight, tight, shortest, isGrey ? 2 : 1);
				} else {
					intoVessel = false;
					List<Integer> branches = new ArrayList<>(trees.get(colour));
					branches.sort(Integer::compare);
					Collections.shuffle(branches, random);
					origin = -1;
					for (int branch : branches) {
						walk = walk(branch, used, random, notches, crowded || tight, tight, shortest, isGrey ? 2 : 1);
						if (walk != null) {
							origin = branch;
							break;
						}
					}
				}
				if (walk == null) return null;
				int[] path = new int[walk.size() + 1 + (intoVessel ? 1 : 0)];
				for (int i = 0; i < walk.size(); i++) path[i] = walk.get(walk.size() - 1 - i);
				path[walk.size()] = origin;
				if (intoVessel) path[path.length - 1] = VESSEL;
				int fragment = board.addFragment(path[0], isGrey ? -1 : colour, colour, false, "");
				board.setSolution(fragment, path);
				if (isGrey) {
					board.setNode(path[1], NodeKind.PRISM, colour);
					prisms.add(path[1]);
				}
				for (int node : walk) {
					used[node] = true;
					if (isGrey) greyNodes.add(node);
					else trees.get(colour).add(node);
				}
				if (!isGrey) trees.get(colour).add(origin);
			}
		}

		int[] reference = new int[8];
		for (int i = 0; i < board.fragmentCount(); i++)
			reference[board.fragment(i).trueColour()] += board.solution(i).length - 1;
		board.setReference(reference);
		if (shortest)
			for (int t = 0; t < 8; t++) if (reference[t] > counts[t] * EngramWeaveRules.SPANS_PER_ENZYME) return null;

		if (!placeIntrusions(board, random, rank, counts, used, notches) && strictIntrusions) return null;
		dress(board, random, rank, used, trees, prisms, greyNodes);
		tighten(board);
		return replays(board, counts) ? board : null;
	}

	/**
	 * The reference gates payment, so once the board is dressed each lane no other lane rests on is re-routed along
	 * its shortest legal way home through the finished board; a shorter way replaces it. The walks laid the puzzle,
	 * this only keeps its price honest.
	 */
	private static void tighten(EngramBoard board) {
		int[] plenty = new int[8];
		Arrays.fill(plenty, EngramWeaveRules.SPAN_CAPACITY * 4);
		for (int pass = 0; pass < 3; pass++) {
			boolean shorter = false;
			for (int fragment = 0; fragment < board.fragmentCount(); fragment++) {
				int[] path = board.solution(fragment);
				if (path == null || board.fragment(fragment).intrusion() || carriesAnother(board, fragment)) continue;
				EngramBoard trial = EngramBoard.load(board.save());
				for (int other = 0; other < trial.fragmentCount(); other++) {
					int[] lane = trial.solution(other);
					trial.setLane(other, other == fragment || lane == null ? new int[]{trial.fragment(other).node()} : lane);
				}
				int[] best = shortestHome(trial, fragment, plenty, path.length);
				if (best != null && best.length < path.length) {
					board.setSolution(fragment, best);
					shorter = true;
				}
			}
			if (!shorter) break;
		}
		int[] reference = new int[8];
		for (int i = 0; i < board.fragmentCount(); i++) {
			int[] path = board.solution(i);
			if (path != null) reference[board.fragment(i).trueColour()] += path.length - 1;
		}
		board.setReference(reference);
	}

	/** Whether another reference lane ends on this one (it carries that lane home), so it must keep its course. */
	private static boolean carriesAnother(EngramBoard board, int fragment) {
		int[] path = board.solution(fragment);
		for (int other = 0; other < board.fragmentCount(); other++) {
			int[] theirs = board.solution(other);
			if (other == fragment || theirs == null) continue;
			int end = theirs[theirs.length - 1];
			for (int node : path) if (node == end) return true;
		}
		return false;
	}

	/** Breadth-first over legal steps from a fragment until its lane is home; null if no way shorter than `limit`. */
	private static int[] shortestHome(EngramBoard trial, int fragment, int[] stock, int limit) {
		int start = trial.fragment(fragment).node();
		ArrayDeque<int[]> queue = new ArrayDeque<>();
		queue.add(new int[]{start});
		boolean[] seen = new boolean[NODES];
		seen[start] = true;
		while (!queue.isEmpty()) {
			int[] path = queue.poll();
			if (path.length >= limit) continue;
			for (int next : neighbours(path[path.length - 1])) {
				if (seen[next]) continue;
				trial.setLane(fragment, path);
				if (EngramWeaveRules.canExtend(trial, fragment, next, stock) != EngramWeaveRules.Step.OK) continue;
				int[] longer = Arrays.copyOf(path, path.length + 1);
				longer[path.length] = next;
				trial.setLane(fragment, longer);
				if (EngramWeaveRules.connected(trial, fragment)) return longer;
				if (EngramWeaveRules.landed(trial, fragment)) continue;
				seen[next] = true;
				queue.add(longer);
			}
		}
		return null;
	}

	private static final int MAX_WALK = 6;
	/** Chance a walk ends at the first tip it may end on; otherwise it wanders on, making a longer reference lane. */
	private static final double WALK_STOP = 0.8D;

	/**
	 * A random self-avoiding walk out from `origin` over free nodes to a free tip (or notch) where a fragment can be
	 * recalled, at least `minimum` steps long. Returns the nodes after the origin, nearest first, or null.
	 */
	private static List<Integer> walk(int origin, boolean[] used, Random random, boolean notches, boolean crowded,
			boolean clockwise, boolean shortest, int minimum) {
		if (shortest) return shortestWalk(origin, used, random, notches, minimum);
		List<Integer> path = new ArrayList<>();
		boolean[] seen = used.clone();
		seen[origin] = true;
		return walkFrom(origin, 0, path, seen, random, notches, crowded, clockwise, minimum) ? path : null;
	}

	private static boolean walkFrom(int at, int depth, List<Integer> path, boolean[] seen, Random random, boolean notches,
			boolean crowded, boolean clockwise, int minimum) {
		if (depth >= minimum && (isTip(at) || notches && isNotch(at))
				&& (crowded || depth >= MAX_WALK || random.nextDouble() < WALK_STOP)) return true;
		if (depth >= MAX_WALK) return false;
		List<Integer> next = new ArrayList<>();
		for (int n : neighbours(at)) if (n != VESSEL && !seen[n]) next.add(n);
		Collections.shuffle(next, random);
		// Mostly outward: a walk that circles the inner octagon makes a long, costly reference lane.
		if (random.nextDouble() < 0.8D) next.sort((a, b) -> Boolean.compare(isInner(a), isInner(b)));
		if (clockwise) next.sort((a, b) -> Double.compare(turn(at, a), turn(at, b)));
		for (int n : next) {
			seen[n] = true;
			path.add(n);
			if (walkFrom(n, depth + 1, path, seen, random, notches, crowded, clockwise, minimum)) return true;
			path.removeLast();
			seen[n] = false;
		}
		// Nothing further out: end here after all, if this node may hold a fragment.
		return depth >= minimum && (isTip(at) || notches && isNotch(at));
	}

	/** The nearest free tip (or notch) to `origin` by free nodes, ties broken at random. */
	private static List<Integer> shortestWalk(int origin, boolean[] used, Random random, boolean notches, int minimum) {
		int[] previous = new int[NODES];
		int[] depth = new int[NODES];
		Arrays.fill(previous, -2);
		java.util.ArrayDeque<Integer> queue = new java.util.ArrayDeque<>();
		previous[origin] = -1;
		queue.add(origin);
		while (!queue.isEmpty()) {
			int at = queue.poll();
			if (at != origin && depth[at] >= minimum && (isTip(at) || notches && isNotch(at))) {
				List<Integer> path = new ArrayList<>();
				for (int n = at; n != origin; n = previous[n]) path.add(0, n);
				return path;
			}
			List<Integer> next = new ArrayList<>();
			for (int n : neighbours(at)) if (n != VESSEL && !used[n] && previous[n] == -2) next.add(n);
			Collections.shuffle(next, random);
			for (int n : next) {
				previous[n] = at;
				depth[n] = depth[at] + 1;
				queue.add(n);
			}
		}
		return null;
	}

	/** How far clockwise, seen from the Vessel, `to` lies from `from`, in [0, 2π). */
	private static double turn(int from, int to) {
		double[] a = unit(from), b = unit(to);
		double delta = Math.atan2(b[0], -b[1]) - Math.atan2(a[0], -a[1]);
		return Math.floorMod((long) Math.round(delta * 1e6), Math.round(Math.PI * 2e6)) / 1e6;
	}

	/** False fragments on free tips (then notches), each dressed in one of the recipe's colours. */
	private static boolean placeIntrusions(EngramBoard board, Random random, EnumManipulationRank rank, int[] counts,
			boolean[] used, boolean notches) {
		int[] range = intrusionRange(rank);
		int wanted = range[0] + (range[1] > range[0] ? random.nextInt(range[1] - range[0] + 1) : 0);
		List<Integer> present = new ArrayList<>();
		for (int t = 0; t < 8; t++) if (counts[t] > 0) present.add(t);
		if (present.isEmpty()) return wanted == 0;
		List<Integer> free = new ArrayList<>();
		List<Integer> tips = new ArrayList<>(), notchFree = new ArrayList<>();
		for (int n = 0; n < TIPS; n++) if (!used[n]) tips.add(n);
		for (int n = 8; n < 16; n++) if (!used[n]) notchFree.add(n);
		Collections.shuffle(tips, random);
		Collections.shuffle(notchFree, random);
		free.addAll(tips);
		if (notches || tips.size() < wanted) free.addAll(notchFree);
		int placed = 0;
		for (int node : free) {
			if (placed >= wanted) break;
			int colour = present.get(random.nextInt(present.size()));
			board.addFragment(node, colour, colour, true, "");
			used[node] = true;
			placed++;
		}
		return placed >= range[0];
	}

	/** Scars, valves and a decoy prism, all on what the solution and the fragments left free (valves on its own lanes). */
	private static void dress(EngramBoard board, Random random, EnumManipulationRank rank, boolean[] used,
			List<Set<Integer>> trees, Set<Integer> prisms, Set<Integer> greyNodes) {
		List<Integer> free = new ArrayList<>();
		for (int n = 0; n < VESSEL; n++) if (!used[n]) free.add(n);
		Collections.shuffle(free, random);
		int[] scars = scarRange(rank);
		int scarCount = scars[0] + random.nextInt(scars[1] - scars[0] + 1);
		int taken = 0;
		while (taken < scarCount && taken < free.size()) board.setNode(free.get(taken++), NodeKind.SCAR, -1);
		if (atLeast(rank, EnumManipulationRank.SUMMA)) {
			// Valves on the reference lanes pass their own colour, and one more off them stands in another colour's way.
			List<int[]> onPath = new ArrayList<>();
			for (int t = 0; t < 8; t++)
				for (int node : trees.get(t)) {
					if (board.fragmentAt(node) >= 0 || prisms.contains(node) || greyNodes.contains(node)) continue;
					onPath.add(new int[]{node, t});
				}
			onPath.sort((a, b) -> Integer.compare(a[0], b[0]));
			Collections.shuffle(onPath, random);
			int valves = rank == EnumManipulationRank.SUMMA ? 1 + random.nextInt(2) : 2 + random.nextInt(2);
			for (int i = 0; i < valves && i < onPath.size(); i++) board.setNode(onPath.get(i)[0], NodeKind.MEMBRANE, onPath.get(i)[1]);
			if (taken < free.size() && random.nextBoolean())
				board.setNode(free.get(taken++), NodeKind.MEMBRANE, random.nextInt(8));
		}
		if (atLeast(rank, EnumManipulationRank.MAGISTER) && taken < free.size()) {
			Set<Integer> greyColours = new HashSet<>();
			for (EngramBoard.Fragment f : board.fragments()) if (f.grey()) greyColours.add(f.trueColour());
			int tint = random.nextInt(8);
			for (int i = 0; i < 8 && greyColours.contains(tint); i++) tint = (tint + 1) % 8;
			board.setNode(free.get(taken), NodeKind.PRISM, tint);
		}
	}

	/** The reference must thread on a fresh copy with exactly its own spans, and never through a false fragment. */
	private static boolean replays(EngramBoard board, int[] counts) {
		EngramBoard copy = EngramBoard.load(board.save());
		int[] stock = board.reference();
		for (int i = 0; i < copy.fragmentCount(); i++) {
			int[] path = copy.solution(i);
			if (path == null) continue;
			for (int k = 1; k < path.length; k++)
				if (EngramWeaveRules.extend(copy, i, path[k], stock) != EngramWeaveRules.Step.OK) return false;
		}
		return EngramWeaveRules.complete(copy, counts) && EngramWeaveRules.connectedIntrusions(copy).isEmpty();
	}
}
