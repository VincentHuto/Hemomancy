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
		register("ferric_resonance",850,140, "sanguine_mending");
		register("iron_retort",790,140, "sanguine_mending");
		register("sanguine_magnetism",790,60, "iron_retort");

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
		register("activation_potential",1060,160, "synaptic_jolt");

		// Row 2 (bottom) — HUMILIS
		register("synaptic_jolt",990,230);
		register("conductive_mark",1125,110, "activation_potential");
		register("hemolymphal_pulse",1110,210, "activation_potential", "deadly_gaze");
		register("conjure_crossbow",1050,250, "hemolymphal_pulse")
				.setSoftParents("conjure_staff");

		// ═══════════════════════════════════════════
		//  LUX tendency cluster  (white / light)
		// ═══════════════════════════════════════════
		int lx = dx + 160;  // gap after DUCTILIS

		// Row 0 (top) — SUMMA
		register("unclosing_eye",1350,220, "crimson_sight");
		register("prismatic_reproof",1300,260, "crimson_sight");

		// Row 1 (middle) — MEDIOCRITAS
		register("crimson_sight",1270,160, "hematic_flare");
		register("hematic_beacon",1190,220, "crimson_sight");
		register("lumen_suture",1270,100, "hematic_flare");

		// Row 2 (bottom) — HUMILIS
		register("hematic_flare",1285,220);
		register("conjure_spear",1240,260, "crimson_sight")
				.setSoftParents("conjure_staff");

		// ═══════════════════════════════════════════
		//  Elemental / Esoteric clusters
		// ═══════════════════════════════════════════
		int ex = lx + COL_GAP + 160;  // gap after LUX

		// ── CONGEATIO (blue / cold) ──
		// Row 0 (top) — SUMMA
		register("osseous_bloom",1560,70, "glacial_bastion");
		register("endless_hour",1510,40, "glacial_bastion");

		// Row 1 (middle) — MEDIOCRITAS
		register("glacial_bastion",1510,110, "glacial_grasp", "cryogenic_pulse");
		register("glacial_rampart",1460,120, "glacial_bastion");
		register("conjure_flail",1460,170, "glacial_bastion", "glacial_rampart")
				.setSoftParents("conjure_staff");

		// Row 2 (bottom) — HUMILIS
		register("glacial_grasp",1460,70);
		register("cryogenic_pulse",1560,170, "glacial_circulation");
		register("glacial_circulation",1560,120);

		// ── FLAMMEUS (orange / fire) ──
		int fmx = ex + COL_GAP * 3 + 80;  // gap after CONGEATIO (now wider)

		// Row 0 (top) — SUMMA
		register("vitric_combustion",1790,50, "pyretic_forge");

		// Row 1 (middle) — MEDIOCRITAS
		register("cauterizing_rebuke",1730,110, "sanguine_ignition");
		register("pyretic_forge",1790,110, "sanguine_ignition");
		register("crimson_flame_conjuration",1870,180, "sanguine_ignition");
		register("conjure_torch",1850,110, "crimson_flame_conjuration")
				.setSoftParents("conjure_staff");

		// Row 2 (bottom) — HUMILIS
		register("sanguine_ignition",1790,180);
		register("scalding_updraft",1720,180, "sanguine_ignition");

		// ── TENEBRIS (dark purple / shadow) ──
		int tx = fmx + COL_GAP + 80;  // gap after FLAMMEUS

		// Row 0 (top) — MEDIOCRITAS
		register("umbral_step",2020,150, "blood_eclipse");
		register("black_veil_covenant",1930,70, "void_shroud");
		register("umbral_reversal",1990,40, "umbral_step", "black_veil_covenant");

		// Row 1 (bottom) — HUMILIS / MEDIOCRITAS
		register("void_shroud",1880,110);
		register("gloam_laceration",1980,110);
		register("blood_eclipse",2050,70);
		register("blood_eclipse_mantle",2090,120, "blood_eclipse");
		register("conjure_claws",1950,150, "void_shroud")
				.setSoftParents("conjure_staff");

		// ── MORTEM (dark green / death) ──
		int mx = tx + COL_GAP * 2 + 80;  // gap after TENEBRIS

		// Row 0 (top) — SUMMA
		register("crimson_tithe",2270,120, "exsanguinate");
		register("bloom_of_rot",2300,0, "hemorrhage", "exsanguinate");

		// Row 1 (middle) — MEDIOCRITAS
		register("grave_debt",2180,70, "hemorrhage", "exsanguinate");
		register("insatiable_hunger",2320,70, "hemorrhage");

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
