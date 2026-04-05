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

		// ═══════════════════════════════════════════
		//  ANIMUS tendency cluster  (red / aggressive)
		//  X range: TREE_OFFSET_X + 0..160
		// ═══════════════════════════════════════════
		int ax = TREE_OFFSET_X;

		// Row 0  — HUMILIS (basic)
		register("blood_shot",     ax,        TREE_TOP_Y);
		register("blood_needle",   ax + 80,   TREE_TOP_Y);
		register("blood_rush",     ax + 160,  TREE_TOP_Y);

		// Row 1  — MEDIOCRITAS (mid-tier, descend from basics)
		register("deadly_gaze",    ax,        TREE_TOP_Y + 70,  "blood_shot");
		register("blood_cloud",    ax + 80,   TREE_TOP_Y + 70,  "blood_needle", "blood_shot");

		// Row 2  — SUMMA (high-tier)
		register("blood_aneurysm", ax + 40,   TREE_TOP_Y + 140, "deadly_gaze", "blood_cloud");
		register("summon_avatar",  ax + 120,  TREE_TOP_Y + 140, "blood_cloud", "blood_rush");

		// ═══════════════════════════════════════════
		//  FERRIC tendency cluster  (grey / metallic)
		//  X range: TREE_OFFSET_X + 220..380
		// ═══════════════════════════════════════════
		int fx = TREE_OFFSET_X + 240;

		// Row 0
		register("venous_travel",       fx,        TREE_TOP_Y);
		register("conjure_blade",       fx + 80,   TREE_TOP_Y);

		// Row 1
		register("blood_absorption",    fx,        TREE_TOP_Y + 70,  "venous_travel");
		register("blood_projection",    fx + 80,   TREE_TOP_Y + 70,  "conjure_blade");

		// Row 2
		register("ferric_transmutation", fx + 40,  TREE_TOP_Y + 140, "blood_absorption", "blood_projection");

		// ═══════════════════════════════════════════
		//  DUCTILIS tendency cluster  (yellow / nervous)
		//  X range: TREE_OFFSET_X + 460..540
		// ═══════════════════════════════════════════
		int dx = TREE_OFFSET_X + 480;

		// Row 0
		register("activation_potential", dx,       TREE_TOP_Y);

		// Row 1
		register("sanguine_ward",        dx,       TREE_TOP_Y + 70,  "activation_potential");
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
