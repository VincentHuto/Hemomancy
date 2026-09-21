package com.vincenthuto.hemomancy.common.worldgen.structure;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Rotation;

/**
 * Code-driven assembly graph for the dynasty castle, the counterpart to {@code VigilModules}. A
 * layout names one keep piece and a set of slots; each slot carries a local offset from the keep
 * origin, a rotation, and a weighted-by-repetition list of candidate piece names. The sentinel
 * {@code "empty"} leaves the slot unfilled so ruined layouts have real gaps.
 *
 * <p>The castle is a square ring: a keep in the middle, four corner towers, and four full-side
 * curtain walls (one of which is a gatehouse). Wall pieces are authored along the X axis and
 * rotated onto each side, so a single slot per side yields a fully encompassing wall.
 */
public final class DynastyCastleLayouts {
	public record Slot(BlockPos offset, Rotation rotation, List<String> candidates) {}
	public record Layout(String keep, List<Slot> slots) {}

	private DynastyCastleLayouts() {}

	/** Distance from the keep center to the curtain-wall line and corner towers. */
	private static final int RING = 13;

	// Intact castles keep all their (varied-height) towers; ruined castles have broken/missing ones.
	private static final List<String> INTACT_TOWERS = List.of("tower_tall", "tower_short");
	private static final List<String> RUINED_TOWERS = List.of("tower_broken", "tower_short", "empty");

	private static List<Slot> corners(List<String> towers) {
		List<Slot> out = new ArrayList<>();
		out.add(new Slot(new BlockPos(-RING, 0, -RING), Rotation.NONE, towers));
		out.add(new Slot(new BlockPos(RING, 0, -RING), Rotation.NONE, towers));
		out.add(new Slot(new BlockPos(-RING, 0, RING), Rotation.NONE, towers));
		out.add(new Slot(new BlockPos(RING, 0, RING), Rotation.NONE, towers));
		return out;
	}

	// Four curtain-wall sides, all placed with Rotation.NONE. Walls are authored per orientation
	// (ns = along X, ew = along Z) so no rotation math is needed. `front` (-Z) takes the gatehouse.
	private static List<Slot> sides(List<String> wallsNs, List<String> wallsEw, List<String> gates) {
		List<Slot> out = new ArrayList<>();
		out.add(new Slot(new BlockPos(0, 0, -RING), Rotation.NONE, gates));    // front (-Z)
		out.add(new Slot(new BlockPos(0, 0, RING), Rotation.NONE, wallsNs));   // back (+Z)
		out.add(new Slot(new BlockPos(-RING, 0, 0), Rotation.NONE, wallsEw));  // left (-X)
		out.add(new Slot(new BlockPos(RING, 0, 0), Rotation.NONE, wallsEw));   // right (+X)
		return out;
	}

	private static Layout layout(String keep, List<String> towers,
			List<String> wallsNs, List<String> wallsEw, List<String> gates) {
		List<Slot> slots = new ArrayList<>();
		// Walls first, then corner towers, so a present tower overwrites the overlapping wall ends
		// (its solid shell + hollow interior swallow them) while a missing tower still leaves the
		// wall covering the corner. Piece order = list order during assembly.
		slots.addAll(sides(wallsNs, wallsEw, gates));
		slots.addAll(corners(towers));
		return new Layout(keep, List.copyOf(slots));
	}

	// Each layout is internally uniform: one decay level applied to every wall and the gate, so a
	// castle never mixes a crumbled side with pristine ones. Variety comes from choosing between
	// layouts (single-element candidate lists keep every side of one castle identical in condition).
	private static final Layout GRAND_WINDOWS = layout("keep_grand", INTACT_TOWERS,
			List.of("wall_ns_windows"), List.of("wall_ew_windows"), List.of("gate_side"));

	private static final Layout GRAND_PLAIN = layout("keep_grand", INTACT_TOWERS,
			List.of("wall_ns_plain"), List.of("wall_ew_plain"), List.of("gate_side"));

	private static final Layout RUINED = layout("keep_ruined", RUINED_TOWERS,
			List.of("wall_ns_breach"), List.of("wall_ew_breach"), List.of("gate_side_ruined"));

	private static final Layout SQUAT = layout("keep_squat", RUINED_TOWERS,
			List.of("wall_ns_breach"), List.of("wall_ew_breach"), List.of("gate_side_ruined"));

	private static final List<Layout> ALL = List.of(GRAND_WINDOWS, GRAND_PLAIN, RUINED, SQUAT);

	public static List<Layout> all() {
		return ALL;
	}

	public static Layout pick(RandomSource random) {
		return ALL.get(random.nextInt(ALL.size()));
	}
}
