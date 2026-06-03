package com.vincenthuto.hemomancy.common.init;

import com.vincenthuto.hemomancy.client.screen.skilltree.harbinger.ManipulationTreeEntry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
		register("blood_aneurysm",370,210, "blood_cloud");
		register("summon_avatar",520,160, "blood_cloud", "blood_rush");

		// Row 1 (middle) — MEDIOCRITAS
		register("deadly_gaze",1130,150, "activation_potential");
		register("blood_cloud",410,160, "blood_needle", "blood_shot");
		register("summon_thrall",560,210, "blood_rush");

		// Row 2 (bottom) — HUMILIS
		register("blood_shot",410,280);
		register("blood_needle",440,220);
		register("blood_rush",490,220);
		register("vital_effusion",520,280, "blood_rush");
		register("conjure_blade",465,330, "vital_effusion")
				.setSoftParents("conjure_staff");

		// ═══════════════════════════════════════════
		//  FERRIC tendency cluster  (grey / metallic)
		// ═══════════════════════════════════════════
		int fx = ax + COL_GAP * 3 + 160;  // gap after ANIMUS

		// Row 0 (top) — SUMMA
		register("ferric_transmutation",920,50, "blood_absorption", "blood_projection");

		// Row 1 (middle) — MEDIOCRITAS
		register("blood_absorption",980,140);
		register("blood_projection",980,50);
		register("sanguine_excavation",860,50, "ferric_transmutation");
		register("ferric_resonance",850,140, "sanguine_mending", "sanguine_excavation");

		// Row 2 (bottom) — HUMILIS / MEDIOCRITAS roots
		register("venous_travel",1067,60, "activation_potential");
		register("conjure_staff",1040,90, "blood_projection", "blood_absorption");
		register("sanguine_mending",910,140);
		register("vascular_dowsing",930,210, "sanguine_mending");

		// ═══════════════════════════════════════════
		//  DUCTILIS tendency cluster  (yellow / nervous)
		// ═══════════════════════════════════════════
		int dx = fx + COL_GAP * 2 + 160;  // gap after FERRIC

		// Row 0 (top) — MEDIOCRITAS
		register("sanguine_ward",1010,110, "activation_potential");

		// Row 1 (middle) — MEDIOCRITAS root
		register("activation_potential",1060,160, "crimson_harvest");

		// Row 2 (bottom) — HUMILIS
		register("crimson_harvest",990,170);
		register("hemolymphal_pulse",1110,210, "activation_potential", "deadly_gaze");
		register("conjure_crossbow",1050,260, "hemolymphal_pulse")
				.setSoftParents("conjure_staff");

		// ═══════════════════════════════════════════
		//  LUX tendency cluster  (white / light)
		// ═══════════════════════════════════════════
		int lx = dx + 160;  // gap after DUCTILIS

		// Row 0 (top) — SUMMA
		register("unclosing_eye",1260,70, "crimson_sight");

		// Row 1 (middle) — MEDIOCRITAS
		register("crimson_sight",1270,160, "hemosynthesis", "blood_lamp");

		// Row 2 (bottom) — HUMILIS
		register("hemosynthesis",1200,110);
		register("blood_lamp",1330,110);
		register("conjure_spear",1270,250, "crimson_sight")
				.setSoftParents("conjure_staff");

		// ═══════════════════════════════════════════
		//  Elemental / Esoteric clusters
		// ═══════════════════════════════════════════
		int ex = lx + COL_GAP + 160;  // gap after LUX

		// ── CONGEATIO (blue / cold) ──
		// Row 0 (top) — SUMMA
		register("osseous_bloom",1440,80, "glacial_bastion");
		register("endless_hour",1510,30, "glacial_bastion");

		// Row 1 (middle) — MEDIOCRITAS
		register("glacial_bastion",1510,110, "glacial_grasp", "cryogenic_pulse");
		register("conjure_flail",1510,200, "glacial_bastion")
				.setSoftParents("conjure_staff");

		// Row 2 (bottom) — HUMILIS
		register("glacial_grasp",1440,160);
		register("cryogenic_pulse",1580,160, "glacial_circulation");
		register("glacial_circulation",1580,80);

		// ── FLAMMEUS (orange / fire) ──
		int fmx = ex + COL_GAP * 3 + 80;  // gap after CONGEATIO (now wider)

		// Row 0 (top) — SUMMA
		register("vitric_combustion",1830,50, "pyretic_forge");

		// Row 1 (middle) — MEDIOCRITAS
		register("pyretic_forge",1800,110, "sanguine_ignition");
		register("crimson_flame_conjuration",1870,180, "sanguine_ignition");
		register("conjure_torch",1860,110, "crimson_flame_conjuration")
				.setSoftParents("conjure_staff");

		// Row 2 (bottom) — HUMILIS
		register("sanguine_ignition",1790,180);

		// ── TENEBRIS (dark purple / shadow) ──
		int tx = fmx + COL_GAP + 80;  // gap after FLAMMEUS

		// Row 0 (top) — MEDIOCRITAS
		register("umbral_step",1990,40, "void_shroud", "blood_eclipse");

		// Row 1 (bottom) — HUMILIS / MEDIOCRITAS
		register("void_shroud",1950,110);
		register("blood_eclipse",2030,110);
		register("conjure_claws",1990,190, "void_shroud")
				.setSoftParents("conjure_staff");

		// ── MORTEM (dark green / death) ──
		int mx = tx + COL_GAP * 2 + 80;  // gap after TENEBRIS

		// Row 0 (top) — SUMMA
		register("crimson_tithe",2270,120, "exsanguinate");
		register("bloom_of_rot",2300,0, "hemorrhage", "exsanguinate");

		// Row 1 (middle) — MEDIOCRITAS
		register("vital_reservoir",2180,0, "hemorrhage", "exsanguinate");

		// Row 2 (bottom) — HUMILIS / MEDIOCRITAS
		register("hemorrhage",2240,-30);
		register("exsanguinate",2240,50, "hemorrhage");
		register("conjure_axe",2210,120, "exsanguinate")
				.setSoftParents("conjure_staff");
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
