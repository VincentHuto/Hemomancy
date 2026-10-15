package com.vincenthuto.hemomancy.common.tile.harbinger.crafting.engram;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * One engram as laid out around a loom: what lies on each node of the star, the fragments recalled at its tips and
 * the lane threaded from each, the wheel's rotation, fungal pressure, and the generator's reference solution with its
 * span cost. Colours are {@code EnumBloodTendency} ordinals; a grey fragment's colour is -1 until a prism dyes its lane.
 * Plain data: the rules live in {@link EngramWeaveRules}.
 */
public final class EngramBoard {
	/** What lies on a node: open flesh, a dead scar, a valve passing one colour, or a prism dyeing grey lanes. */
	public enum NodeKind { OPEN, SCAR, MEMBRANE, PRISM }

	/**
	 * A recalled fragment. An intrusion is a failed Harbinger's memory dressed in a real colour; its decoy names the
	 * manipulation its echo replays. A premarked intrusion is one the weaver's bloodline already knows to be false.
	 */
	public record Fragment(int node, int colour, int trueColour, boolean intrusion, String decoy, boolean premarked) {
		public boolean grey() {
			return colour < 0;
		}
	}

	private final NodeKind[] kinds = new NodeKind[EngramSigilGeometry.NODES];
	private final int[] tints = new int[EngramSigilGeometry.NODES];
	private final List<Fragment> fragments = new ArrayList<>();
	private final List<int[]> lanes = new ArrayList<>();
	private final List<int[]> solution = new ArrayList<>();
	private final boolean keyed;
	private final int rotation;
	private int pressure;
	private int[] reference = new int[8];

	private EngramBoard(boolean keyed, int rotation) {
		this.keyed = keyed;
		this.rotation = Math.floorMod(rotation, EngramSigilGeometry.FACES);
		Arrays.fill(kinds, NodeKind.OPEN);
		Arrays.fill(tints, -1);
	}

	/** An empty star. Keyed faces take only their tendency; `rotation` is the face Animus is keyed to. */
	public static EngramBoard blank(boolean keyed, int rotation) {
		return new EngramBoard(keyed, rotation);
	}

	public int addFragment(int node, int colour, int trueColour, boolean intrusion, String decoy) {
		fragments.add(new Fragment(node, colour, trueColour, intrusion, decoy == null ? "" : decoy, false));
		lanes.add(new int[]{node});
		solution.add(null);
		return fragments.size() - 1;
	}

	public void setNode(int node, NodeKind kind, int tint) {
		kinds[node] = kind;
		tints[node] = kind == NodeKind.OPEN || kind == NodeKind.SCAR ? -1 : tint;
	}

	public NodeKind kind(int node) { return kinds[node]; }
	public int tint(int node) { return tints[node]; }
	public boolean keyed() { return keyed; }
	public int rotation() { return rotation; }
	public int pressure() { return pressure; }
	public void setPressure(int pressure) { this.pressure = Math.max(0, pressure); }
	public int[] reference() { return reference.clone(); }
	public void setReference(int[] spans) { reference = Arrays.copyOf(spans, 8); }
	public int fragmentCount() { return fragments.size(); }
	public Fragment fragment(int index) { return fragments.get(index); }
	public List<Fragment> fragments() { return List.copyOf(fragments); }

	/** The lane threaded from a fragment, starting on the fragment's own node. */
	public int[] lane(int fragment) {
		return lanes.get(fragment).clone();
	}

	public void setLane(int fragment, int[] path) {
		lanes.set(fragment, path.clone());
	}

	/** The generator's path for a fragment (null for intrusions or boards without one). */
	public int[] solution(int fragment) {
		int[] path = solution.get(fragment);
		return path == null ? null : path.clone();
	}

	public void setSolution(int fragment, int[] path) {
		solution.set(fragment, path == null ? null : path.clone());
	}

	/** Whether any fragment's reference path crosses a node; pressure never lands on one. */
	public boolean onSolution(int node) {
		for (int[] path : solution) if (path != null) for (int n : path) if (n == node) return true;
		return false;
	}

	/** The fragment standing on a node, or -1. */
	public int fragmentAt(int node) {
		for (int i = 0; i < fragments.size(); i++) if (fragments.get(i).node() == node) return i;
		return -1;
	}

	/** Names the memory an intrusion's echo replays. */
	public void setDecoy(int fragment, String decoy) {
		Fragment f = fragments.get(fragment);
		fragments.set(fragment, new Fragment(f.node(), f.colour(), f.trueColour(), f.intrusion(), decoy == null ? "" : decoy, f.premarked()));
	}

	/** The bloodline already remembers these memories: every intrusion shows as a grey husk. */
	public void premarkIntrusions() {
		for (int i = 0; i < fragments.size(); i++) {
			Fragment f = fragments.get(i);
			if (f.intrusion() && !f.premarked())
				fragments.set(i, new Fragment(f.node(), f.colour(), f.trueColour(), true, f.decoy(), true));
		}
	}

	public CompoundTag save() {
		CompoundTag tag = new CompoundTag();
		tag.putBoolean("keyed", keyed);
		tag.putInt("rotation", rotation);
		tag.putInt("pressure", pressure);
		tag.putIntArray("reference", reference);
		int[] kindOrdinals = new int[kinds.length];
		for (int i = 0; i < kinds.length; i++) kindOrdinals[i] = kinds[i].ordinal();
		tag.putIntArray("kinds", kindOrdinals);
		tag.putIntArray("tints", tints);
		ListTag list = new ListTag();
		for (int i = 0; i < fragments.size(); i++) {
			Fragment f = fragments.get(i);
			CompoundTag entry = new CompoundTag();
			entry.putInt("node", f.node());
			entry.putInt("colour", f.colour());
			entry.putInt("true", f.trueColour());
			entry.putBoolean("intrusion", f.intrusion());
			entry.putString("decoy", f.decoy());
			entry.putBoolean("premarked", f.premarked());
			entry.putIntArray("lane", lanes.get(i));
			if (solution.get(i) != null) entry.put("solution", new IntArrayTag(solution.get(i)));
			list.add(entry);
		}
		tag.put("fragments", list);
		return tag;
	}

	public static EngramBoard load(CompoundTag tag) {
		EngramBoard board = new EngramBoard(tag.getBoolean("keyed"), tag.getInt("rotation"));
		board.pressure = tag.getInt("pressure");
		board.reference = Arrays.copyOf(tag.getIntArray("reference"), 8);
		int[] kindOrdinals = tag.getIntArray("kinds");
		int[] tints = tag.getIntArray("tints");
		NodeKind[] values = NodeKind.values();
		for (int i = 0; i < EngramSigilGeometry.NODES; i++) {
			if (i < kindOrdinals.length && kindOrdinals[i] >= 0 && kindOrdinals[i] < values.length) board.kinds[i] = values[kindOrdinals[i]];
			if (i < tints.length) board.tints[i] = tints[i];
		}
		ListTag list = tag.getList("fragments", Tag.TAG_COMPOUND);
		for (int i = 0; i < list.size(); i++) {
			CompoundTag entry = list.getCompound(i);
			int node = entry.getInt("node");
			board.fragments.add(new Fragment(node, entry.getInt("colour"), entry.getInt("true"),
					entry.getBoolean("intrusion"), entry.getString("decoy"), entry.getBoolean("premarked")));
			int[] lane = entry.getIntArray("lane");
			board.lanes.add(lane.length == 0 || lane[0] != node ? new int[]{node} : lane);
			board.solution.add(entry.contains("solution") ? entry.getIntArray("solution") : null);
		}
		return board;
	}
}
