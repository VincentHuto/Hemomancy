package com.vincenthuto.hemomancy.common.tile.harbinger.crafting.engram;

import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationRank;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.engram.EngramBoard.NodeKind;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.vincenthuto.hemomancy.common.tile.harbinger.crafting.engram.EngramSigilGeometry.*;

/**
 * How a lane may be threaded through the engram, what it costs, when the engram is complete, and what a mistake
 * costs by rank. A lane moves node to node along the star's lines. A node holds one colour: a lane may end on a lane
 * of its own colour (it is plied in and carried home by it), never cross another. The Vessel is entered by a spoke,
 * through a face that, on keyed boards, takes only its tendency. Every edge spins one span of the lane's colour from
 * the loom's stock; nothing is spent until the engram completes.
 */
public final class EngramWeaveRules {
	public static final int SPANS_PER_ENZYME = 3;
	public static final int SPAN_CAPACITY = 64 * SPANS_PER_ENZYME;
	/** Mistakes a Summa or Magister engram absorbs before its flesh scars over. */
	public static final int PRESSURE_LIMIT = 3;
	/** Blood a burnt span takes from the weaver when the vials hold no surplus to burn. */
	public static final double BURN_BLOOD_PER_SPAN = 5.0D;
	/** Ticks a fragment must be held under the staff, unmoved, before its beat is heard. */
	public static final int LISTEN_TICKS = 10;

	public enum Step { OK, RETRACTED, IDLE, NOT_ADJACENT, OCCUPIED, SCARRED, MEMBRANE, WRONG_PRISM, WRONG_FACE, NO_STOCK, LANDED;
		public boolean moved() {
			return this == OK || this == RETRACTED;
		}
	}

	/** What threading a false fragment home costs, by the memory's rank. */
	public enum Stake { NONE, BURN, PRESSURE, BITE }

	private EngramWeaveRules() {
	}

	public static double tipRadius(int stage) {
		return switch (Math.max(0, Math.min(2, stage))) {
			case 0 -> 5.0D;
			case 1 -> 5.75D;
			default -> 6.5D;
		};
	}

	public static double traceReach(int stage) {
		return stage >= 1 ? 8.0D : 5.5D;
	}

	/** A Ganglionic loom's feelers listen anywhere on its engram; a base loom only within the staff's reach. */
	public static double listenReach(int stage) {
		return stage >= 1 ? 16.0D : traceReach(stage);
	}

	/** How near a node the staff must point to take it, in blocks. */
	public static double snapRadius(int stage) {
		return tipRadius(stage) * 0.12D;
	}

	public static Stake stakeFor(EnumManipulationRank rank) {
		return switch (rank) {
			case HUMILIS -> Stake.NONE;
			case MEDIOCRITAS -> Stake.BURN;
			case SUMMA, MAGISTER -> Stake.PRESSURE;
			case PERFECTUS -> Stake.BITE;
		};
	}

	/** The tendency a face takes on a keyed board: the wheel runs clockwise in enum order from the Animus face. */
	public static int faceTendency(EngramBoard board, int face) {
		return Math.floorMod(face - board.rotation(), FACES);
	}

	public static boolean faceAllowed(EngramBoard board, int face, int colour) {
		return colour >= 0 && (!board.keyed() || faceTendency(board, face) == colour);
	}

	/** A lane's colour: its fragment's, or for a grey fragment the true colour once a matching prism has dyed it. */
	public static int colour(EngramBoard board, int fragment) {
		EngramBoard.Fragment f = board.fragment(fragment);
		if (!f.grey()) return f.colour();
		for (int node : board.lane(fragment))
			if (board.kind(node) == NodeKind.PRISM && board.tint(node) == f.trueColour()) return f.trueColour();
		return -1;
	}

	/** The colour a lane's spans are spun from: a grey lane pays in its true colour even before it is dyed. */
	public static int payColour(EngramBoard board, int fragment) {
		EngramBoard.Fragment f = board.fragment(fragment);
		return f.grey() ? f.trueColour() : f.colour();
	}

	public static int laneSpans(EngramBoard board, int fragment) {
		return board.lane(fragment).length - 1;
	}

	/** Spans the board's lanes hold of one tendency. */
	public static int committed(EngramBoard board, int tendency) {
		int total = 0;
		for (int i = 0; i < board.fragmentCount(); i++) if (payColour(board, i) == tendency) total += laneSpans(board, i);
		return total;
	}

	public static int[] committed(EngramBoard board) {
		int[] spans = new int[8];
		for (int t = 0; t < 8; t++) spans[t] = committed(board, t);
		return spans;
	}

	/** Whether a lane has stopped: it entered the Vessel, or ended on another lane of its colour. */
	public static boolean landed(EngramBoard board, int fragment) {
		int[] lane = board.lane(fragment);
		return lane[lane.length - 1] == VESSEL || plied(board, fragment);
	}

	private static boolean plied(EngramBoard board, int fragment) {
		int[] lane = board.lane(fragment);
		if (lane.length < 2) return false;
		int end = lane[lane.length - 1];
		for (int other = 0; other < board.fragmentCount(); other++)
			if (other != fragment && carries(board.lane(other), end)) return true;
		return false;
	}

	/**
	 * Whether a lane carries whatever ends on `node`: it runs on past it, or rises from it as its fragment. Two lanes
	 * that merely end on the same node carry neither; whichever is threaded on becomes the carrier.
	 */
	private static boolean carries(int[] lane, int node) {
		for (int i = 0; i < lane.length; i++) if (lane[i] == node) return i == 0 || i < lane.length - 1;
		return false;
	}

	/** Whether a lane reaches the Vessel, itself or through the lanes it is plied into. */
	public static boolean connected(EngramBoard board, int fragment) {
		return connected(board, fragment, new HashSet<>());
	}

	private static boolean connected(EngramBoard board, int fragment, Set<Integer> seen) {
		if (!seen.add(fragment)) return false;
		int[] lane = board.lane(fragment);
		int end = lane[lane.length - 1];
		if (end == VESSEL) return true;
		if (lane.length < 2) return false;
		for (int other = 0; other < board.fragmentCount(); other++)
			if (other != fragment && carries(board.lane(other), end) && connected(board, other, seen)) return true;
		return false;
	}

	/** Every true fragment is home and each colour has exactly the recipe's count of them. */
	public static boolean complete(EngramBoard board, int[] counts) {
		int[] home = new int[8];
		for (int i = 0; i < board.fragmentCount(); i++) {
			EngramBoard.Fragment f = board.fragment(i);
			if (f.intrusion()) continue;
			if (!connected(board, i)) return false;
			home[f.trueColour()]++;
		}
		for (int t = 0; t < 8; t++) if (home[t] != (t < counts.length ? counts[t] : 0)) return false;
		return true;
	}

	public static List<Integer> connectedIntrusions(EngramBoard board) {
		List<Integer> out = new ArrayList<>();
		for (int i = 0; i < board.fragmentCount(); i++)
			if (board.fragment(i).intrusion() && connected(board, i)) out.add(i);
		return out;
	}

	/** Whether a step to `to` would be taken, without taking it. */
	public static Step canExtend(EngramBoard board, int fragment, int to, int[] stock) {
		int[] lane = board.lane(fragment);
		int head = lane[lane.length - 1];
		if (landed(board, fragment)) return Step.LANDED;
		if (!adjacent(head, to)) return Step.NOT_ADJACENT;
		if (contains(lane, to)) return Step.OCCUPIED;
		EngramBoard.Fragment f = board.fragment(fragment);
		int colour = colour(board, fragment);
		if (to == VESSEL) {
			if (!faceAllowed(board, face(head), colour)) return Step.WRONG_FACE;
		} else {
			NodeKind kind = board.kind(to);
			if (kind == NodeKind.SCAR) return Step.SCARRED;
			if (kind == NodeKind.MEMBRANE && board.tint(to) != colour) return Step.MEMBRANE;
			if (kind == NodeKind.PRISM) {
				if (f.grey() && colour < 0) {
					if (board.tint(to) != f.trueColour()) return Step.WRONG_PRISM;
					colour = f.trueColour();
				} else if (board.tint(to) != colour) {
					return Step.WRONG_PRISM;
				}
			}
			for (int other = 0; other < board.fragmentCount(); other++) {
				if (other == fragment || !contains(board.lane(other), to)) continue;
				int theirs = colour(board, other);
				if (colour < 0 || theirs < 0 || theirs != colour) return Step.OCCUPIED;
			}
		}
		int pay = payColour(board, fragment);
		if (committed(board, pay) + 1 > (pay < stock.length ? stock[pay] : 0)) return Step.NO_STOCK;
		return Step.OK;
	}

	public static Step extend(EngramBoard board, int fragment, int to, int[] stock) {
		Step step = canExtend(board, fragment, to, stock);
		if (step != Step.OK) return step;
		int[] lane = board.lane(fragment);
		int[] next = java.util.Arrays.copyOf(lane, lane.length + 1);
		next[lane.length] = to;
		board.setLane(fragment, next);
		return Step.OK;
	}

	/**
	 * Where the staff's sweep has moved to: back onto the lane's previous node retracts a step; ahead along one
	 * straight star line it fills every node passed, stopping at the first that refuses.
	 */
	public static Step sweep(EngramBoard board, int fragment, int target, int[] stock) {
		int[] lane = board.lane(fragment);
		int head = lane[lane.length - 1];
		if (target == head) return Step.IDLE;
		if (lane.length >= 2 && lane[lane.length - 2] == target) {
			board.setLane(fragment, java.util.Arrays.copyOf(lane, lane.length - 1));
			return Step.RETRACTED;
		}
		int[] run = collinearRun(head, target);
		if (run == null) return Step.NOT_ADJACENT;
		for (int node : run) {
			Step step = extend(board, fragment, node, stock);
			if (step != Step.OK) return step;
		}
		return extend(board, fragment, target, stock);
	}

	/** Cuts a lane back so it ends on `node`; a node not on the lane leaves it alone. */
	public static void cut(EngramBoard board, int fragment, int node) {
		int[] lane = board.lane(fragment);
		for (int i = 0; i < lane.length; i++)
			if (lane[i] == node) {
				board.setLane(fragment, java.util.Arrays.copyOf(lane, i + 1));
				return;
			}
	}

	public static void clear(EngramBoard board, int fragment) {
		board.setLane(fragment, new int[]{board.fragment(fragment).node()});
	}

	/** The lane whose path holds a node (its own fragment's lane first), or -1. */
	public static int laneAt(EngramBoard board, int node) {
		int standing = board.fragmentAt(node);
		if (standing >= 0) return standing;
		for (int i = 0; i < board.fragmentCount(); i++) if (contains(board.lane(i), node)) return i;
		return -1;
	}

	/** The first tendency whose stock does not cover its reserved spans, or -1 when every colour is covered. */
	public static int shortTendency(int[] stock, int[] reserved) {
		for (int t = 0; t < 8; t++) if ((t < stock.length ? stock[t] : 0) < (t < reserved.length ? reserved[t] : 0)) return t;
		return -1;
	}

	/** How many of `spans` a mistake may burn from stock without touching the reserve or spans lanes still hold. */
	public static int burnable(int spans, int stock, int reserved, int committed) {
		return Math.max(0, Math.min(spans, stock - Math.max(reserved, committed)));
	}

	/**
	 * Fungal pressure boiling over: a free node of open flesh, off the reference path and touched by no lane, scars
	 * over; once none is left, a free tip recalls a new false fragment instead. Returns the node changed, or -1.
	 */
	public static int applyPressure(EngramBoard board, java.util.Random random, int intrusionColour, String decoy) {
		List<Integer> flesh = new ArrayList<>();
		for (int node = 0; node < VESSEL; node++)
			if (board.kind(node) == NodeKind.OPEN && !board.onSolution(node) && laneAt(board, node) < 0) flesh.add(node);
		if (!flesh.isEmpty()) {
			int node = flesh.get(random.nextInt(flesh.size()));
			board.setNode(node, NodeKind.SCAR, -1);
			return node;
		}
		List<Integer> tips = new ArrayList<>();
		for (int node = 0; node < TIPS; node++)
			if (!board.onSolution(node) && laneAt(board, node) < 0 && board.kind(node) != NodeKind.SCAR) tips.add(node);
		if (tips.isEmpty()) {
			for (int node = 0; node < TIPS; node++)
				if (!board.onSolution(node) && laneAt(board, node) < 0) tips.add(node);
		}
		if (tips.isEmpty()) return -1;
		int node = tips.get(random.nextInt(tips.size()));
		board.setNode(node, NodeKind.OPEN, -1);
		board.addFragment(node, intrusionColour, intrusionColour, true, decoy);
		return node;
	}

	static boolean contains(int[] lane, int node) {
		for (int n : lane) if (n == node) return true;
		return false;
	}
}
