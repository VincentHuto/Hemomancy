package com.vincenthuto.hemomancy.common.tile.harbinger.crafting.engram;

import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.engram.EngramWeaveRules.Step;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A loom's live engram: the board, the lane the weaver's staff holds, and a short log of what just happened on it
 * (a lane landing, a false lane snapping, flesh scarring, a step refused) so clients can play each beat once. The
 * loom owns it, persists it, and applies what a step costs; this only threads lanes.
 */
public final class EngramWeaveSession {
	public enum Event { LAND, SNAP, REFUSE, SCAR, INTRUDE, CUT }

	/** One logged beat. `serial` only grows, so a client replays those it has not yet seen. */
	public record Beat(int serial, Event event, int node, int fragment) {}

	/** What a staff step did: the rule's answer, and whether the lanes changed. */
	public record Result(Step step, boolean changed, int fragment) {}

	private static final int LOG = 6;

	private EngramBoard board;
	private int active = -1;
	private int serial;
	private final Deque<Beat> log = new ArrayDeque<>();
	/** Refusals are logged once per node, so holding the staff over a closed node does not spam the log. */
	private int lastRefused = -1;

	public boolean live() {
		return board != null;
	}

	public EngramBoard board() {
		return board;
	}

	public int active() {
		return active;
	}

	public void start(EngramBoard board) {
		this.board = board;
		active = -1;
		lastRefused = -1;
	}

	public void clear() {
		board = null;
		active = -1;
		lastRefused = -1;
		log.clear();
	}

	/** The staff let go: no lane is held until the next press. */
	public void release() {
		active = -1;
		lastRefused = -1;
	}

	/**
	 * One look of the weaver's staff at `node`. A press on a fragment or a lane's head takes that lane; a press
	 * elsewhere, or holding, threads the held lane toward the node. A sneaking press cuts the lane back there (or,
	 * on its fragment, clears it).
	 */
	public Result step(int node, boolean sneaking, boolean press, int[] stock) {
		if (board == null || node < 0) return new Result(Step.IDLE, false, -1);
		Set<Integer> before = connected();
		if (sneaking) {
			if (!press) return new Result(Step.IDLE, false, -1);
			int lane = EngramWeaveRules.laneAt(board, node);
			if (lane < 0) return new Result(Step.IDLE, false, -1);
			int length = board.lane(lane).length;
			if (board.fragment(lane).node() == node) EngramWeaveRules.clear(board, lane);
			else EngramWeaveRules.cut(board, lane, node);
			active = lane;
			boolean changed = board.lane(lane).length != length;
			if (changed) log(Event.CUT, node, lane);
			return new Result(Step.RETRACTED, changed, lane);
		}
		if (press) {
			int standing = board.fragmentAt(node);
			if (standing >= 0) {
				active = standing;
				lastRefused = -1;
				return new Result(Step.IDLE, false, standing);
			}
			for (int i = 0; i < board.fragmentCount(); i++) {
				int[] lane = board.lane(i);
				if (lane[lane.length - 1] == node && node != EngramSigilGeometry.VESSEL) {
					active = i;
					lastRefused = -1;
					return new Result(Step.IDLE, false, i);
				}
			}
		}
		if (active < 0 || active >= board.fragmentCount()) return new Result(Step.IDLE, false, -1);
		Step step = EngramWeaveRules.sweep(board, active, node, stock);
		if (step.moved()) {
			lastRefused = -1;
			for (int fragment : connected())
				if (!before.contains(fragment) && !board.fragment(fragment).intrusion()) log(Event.LAND, landing(fragment), fragment);
		} else if (step != Step.IDLE && step != Step.LANDED && node != lastRefused) {
			lastRefused = node;
			log(Event.REFUSE, node, active);
		}
		return new Result(step, step.moved(), active);
	}

	/** A false lane threaded home tears loose: its lane is cleared and the tear is logged at its fragment. */
	public int snap(int fragment) {
		int spans = EngramWeaveRules.laneSpans(board, fragment);
		EngramWeaveRules.clear(board, fragment);
		if (active == fragment) active = -1;
		log(Event.SNAP, board.fragment(fragment).node(), fragment);
		return spans;
	}

	public void logPressure(int node, boolean intrusion) {
		log(intrusion ? Event.INTRUDE : Event.SCAR, node, intrusion ? board.fragmentAt(node) : -1);
	}

	private Set<Integer> connected() {
		Set<Integer> out = new HashSet<>();
		for (int i = 0; i < board.fragmentCount(); i++) if (EngramWeaveRules.connected(board, i)) out.add(i);
		return out;
	}

	/** Where a landing lane meets home: the Vessel, or the node where it is plied in. */
	private int landing(int fragment) {
		int[] lane = board.lane(fragment);
		return lane[lane.length - 1];
	}

	private void log(Event event, int node, int fragment) {
		log.addLast(new Beat(++serial, event, node, fragment));
		while (log.size() > LOG) log.removeFirst();
	}

	public List<Beat> beats() {
		return new ArrayList<>(log);
	}

	public int serial() {
		return serial;
	}

	public CompoundTag save() {
		CompoundTag tag = new CompoundTag();
		if (board != null) tag.put("board", board.save());
		tag.putInt("active", active);
		tag.putInt("serial", serial);
		ListTag beats = new ListTag();
		for (Beat beat : log) {
			CompoundTag entry = new CompoundTag();
			entry.putInt("serial", beat.serial());
			entry.putString("event", beat.event().name());
			entry.putInt("node", beat.node());
			entry.putInt("fragment", beat.fragment());
			beats.add(entry);
		}
		tag.put("log", beats);
		return tag;
	}

	public void load(CompoundTag tag) {
		board = tag.contains("board") ? EngramBoard.load(tag.getCompound("board")) : null;
		active = tag.contains("active") ? tag.getInt("active") : -1;
		serial = tag.getInt("serial");
		log.clear();
		ListTag beats = tag.getList("log", Tag.TAG_COMPOUND);
		for (int i = 0; i < beats.size(); i++) {
			CompoundTag entry = beats.getCompound(i);
			try {
				log.addLast(new Beat(entry.getInt("serial"), Event.valueOf(entry.getString("event")), entry.getInt("node"),
						entry.getInt("fragment")));
			} catch (IllegalArgumentException ignored) {
				// An unknown beat from a newer save is skipped, not fatal.
			}
		}
	}
}
