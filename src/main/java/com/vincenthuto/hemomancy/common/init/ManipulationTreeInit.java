package com.vincenthuto.hemomancy.common.init;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.vincenthuto.hemomancy.client.screen.skilltree.ManipulationTreeEntry;

/**
 * Defines the spatial layout and parent-child relationships of the
 * manipulation tree displayed on the Skill Tree screen.
 * <p>
 * <b>To change where a manipulation appears or which manipulations are
 * connected, edit the entries below.</b>
 * <p>
 * Coordinates are in <i>content-space pixels</i> (the screen applies its own
 * pan and zoom on top). The manipulation tree is placed to the right of the
 * skill tree at an offset of {@link #TREE_OFFSET_X}.
 */
public class ManipulationTreeInit {

	/** Horizontal offset (in content-space) from x=0 where the manip tree begins. */
	public static final int TREE_OFFSET_X = 350;
	/** Y position of the top row. */
	public static final int TREE_TOP_Y = 40;

	/** All registered manipulation tree entries. */
	public static final List<ManipulationTreeEntry> ENTRIES = new ArrayList<>();

	/** Quick lookup by name. */
	public static final Map<String, ManipulationTreeEntry> BY_NAME = new HashMap<>();

	// ────────────────────────────────────────────────────────────
	//  Init — call during commonSetup or before the screen opens
	// ────────────────────────────────────────────────────────────

	public static void init() {
		ENTRIES.clear();
		BY_NAME.clear();

		// Row spacing constants
		int ROW_GAP = 70;   // Y gap between tiers
		int COL_GAP = 80;   // X gap between sibling nodes

		// ═══════════════════════════════════════════
		//  ANIMUS tendency cluster  (red / aggressive)
		// ═══════════════════════════════════════════
		int ax = TREE_OFFSET_X;

		// Row 0 (top) — SUMMA
		register("blood_aneurysm", ax + 40,          TREE_TOP_Y,             "deadly_gaze", "blood_cloud");
		register("summon_avatar",  ax + 120,         TREE_TOP_Y,             "blood_cloud", "blood_rush");

		// Row 1 (middle) — MEDIOCRITAS
		register("deadly_gaze",    ax,               TREE_TOP_Y + ROW_GAP,   "blood_shot");
		register("blood_cloud",    ax + COL_GAP,     TREE_TOP_Y + ROW_GAP,   "blood_needle", "blood_shot");
		register("summon_thrall",  ax + COL_GAP * 2, TREE_TOP_Y + ROW_GAP,   "blood_rush", "crimson_flame_conjuration");

		// Row 2 (bottom) — HUMILIS
		register("blood_shot",     ax,               TREE_TOP_Y + ROW_GAP * 2);
		register("blood_needle",   ax + COL_GAP,     TREE_TOP_Y + ROW_GAP * 2);
		register("blood_rush",     ax + COL_GAP * 2, TREE_TOP_Y + ROW_GAP * 2);
		register("crimson_flame_conjuration", ax + COL_GAP * 3, TREE_TOP_Y + ROW_GAP * 2);

		// ═══════════════════════════════════════════
		//  FERRIC tendency cluster  (grey / metallic)
		// ═══════════════════════════════════════════
		int fx = ax + COL_GAP * 3 + 160;  // gap after ANIMUS

		// Row 0 (top) — SUMMA
		register("ferric_transmutation", fx + 40,    TREE_TOP_Y,             "blood_absorption", "blood_projection");

		// Row 1 (middle) — MEDIOCRITAS
		register("blood_absorption",     fx,         TREE_TOP_Y + ROW_GAP,   "venous_travel");
		register("blood_projection",     fx + COL_GAP, TREE_TOP_Y + ROW_GAP, "conjure_blade");
		register("sanguine_excavation",  fx + COL_GAP * 2, TREE_TOP_Y + ROW_GAP, "sanguine_mending");

		// Row 2 (bottom) — HUMILIS / MEDIOCRITAS roots
		register("venous_travel",        fx,         TREE_TOP_Y + ROW_GAP * 2);
		register("conjure_blade",        fx + COL_GAP, TREE_TOP_Y + ROW_GAP * 2);
		register("sanguine_mending",     fx + COL_GAP * 2, TREE_TOP_Y + ROW_GAP * 2);

		// ═══════════════════════════════════════════
		//  DUCTILIS tendency cluster  (yellow / nervous)
		// ═══════════════════════════════════════════
		int dx = fx + COL_GAP * 2 + 160;  // gap after FERRIC

		// Row 0 (top) — MEDIOCRITAS
		register("sanguine_ward",        dx,         TREE_TOP_Y,             "activation_potential");

		// Row 1 (middle) — MEDIOCRITAS root
		register("activation_potential", dx,         TREE_TOP_Y + ROW_GAP,   "crimson_harvest");

		// Row 2 (bottom) — HUMILIS
		register("crimson_harvest",      dx,         TREE_TOP_Y + ROW_GAP * 2);

		// ═══════════════════════════════════════════
		//  LUX tendency cluster  (white / light)
		// ═══════════════════════════════════════════
		int lx = dx + 160;  // gap after DUCTILIS

		// Row 0 (top) — MEDIOCRITAS
		register("crimson_sight",        lx + 40,    TREE_TOP_Y,             "hemosynthesis", "blood_lamp");

		// Row 1 (bottom) — HUMILIS
		register("hemosynthesis",        lx,         TREE_TOP_Y + ROW_GAP);
		register("blood_lamp",           lx + COL_GAP, TREE_TOP_Y + ROW_GAP);

		// ═══════════════════════════════════════════
		//  Elemental / Esoteric clusters
		//  (smaller tendency groups, one or two nodes each)
		// ═══════════════════════════════════════════
		int ex = lx + COL_GAP + 160;  // gap after LUX

		// CONGEATIO (blue / cold) — HUMILIS
		register("glacial_grasp",        ex,         TREE_TOP_Y + ROW_GAP);

		// FLAMMEUS (orange / fire) — MEDIOCRITAS
		register("pyretic_forge",        ex + COL_GAP, TREE_TOP_Y);

		// TENEBRIS (dark purple / shadow) — MEDIOCRITAS
		register("umbral_step",          ex + COL_GAP * 2, TREE_TOP_Y);

		// MORTEM (dark green / death) — MEDIOCRITAS
		register("vital_reservoir",      ex + COL_GAP * 3, TREE_TOP_Y);
	}

	// ────────────────────────────────────────────────────────────
	//  Registration helper
	// ────────────────────────────────────────────────────────────

	private static ManipulationTreeEntry register(String name, int x, int y, String... parents) {
		ManipulationTreeEntry entry = new ManipulationTreeEntry(name, x, y, parents);
		ENTRIES.add(entry);
		BY_NAME.put(name, entry);
		return entry;
	}

	/** Look up an entry by manipulation name. */
	public static ManipulationTreeEntry getEntry(String name) {
		return BY_NAME.get(name);
	}
}
