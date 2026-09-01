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


	public static void init() {
		ENTRIES.clear();
		BY_NAME.clear();


		// Row 0 (top) — SUMMA
		register("blood_aneurysm",480,242, "blood_cloud");
		register("summon_avatar",590,192, "blood_cloud", "blood_rush");

		// Row 1 (middle) — MEDIOCRITAS
		register("deadly_gaze",1110,180, "synaptic_jolt");
		register("blood_cloud",520,192, "blood_needle", "blood_shot");
		register("hematic_rebuke",670,182, "blood_rush");

		// Row 2 (bottom) — HUMILIS
		register("blood_shot",530,292);
		register("blood_needle",530,242);
		register("blood_rush",590,242);
		register("blood_binding",470,342);
		register("vital_effusion",590,292, "blood_rush");
		register("conjure_blade",555,342, "vital_effusion")
				.setSoftParents("conjure_staff");

		// Row 1 (middle) — MEDIOCRITAS
		register("blood_absorption",970,170);
		register("blood_projection",980,80);
		register("iron_retort",790,140, "sanguine_mending");
		register("sanguine_magnetism",790,200, "iron_retort");
		register("ironhearted",850,80, "iron_retort");

		// Row 2 (bottom) — HUMILIS / MEDIOCRITAS roots
		register("conjure_staff",1040,120, "blood_projection", "blood_absorption");
		register("sanguine_mending",910,140);
		register("vascular_dowsing",910,200, "sanguine_mending");

		// Row 0 (top) — MEDIOCRITAS
		register("sanguine_ward",980,110, "synaptic_jolt");

		// Row 1 (middle) — MEDIOCRITAS root

		// Row 2 (bottom) — HUMILIS
		register("synaptic_jolt",980,230);
		register("conductive_mark",1035,90, "synaptic_jolt");
		register("hemolymphal_pulse",1120,240, "synaptic_jolt", "deadly_gaze");
		register("conjure_crossbow",1050,220, "hemolymphal_pulse")
				.setSoftParents("conjure_staff");

		// Row 0 (top) — SUMMA
		register("unclosing_eye",1340,180, "hematic_flare");
		register("prismatic_reproof",1300,230, "hematic_flare");

		// Row 1 (middle) — MEDIOCRITAS
		register("hematic_beacon",1200,200, "hematic_flare");
		register("lumen_suture",1310,120, "hematic_flare");

		// Row 2 (bottom) — HUMILIS
		register("hematic_flare",1195,140);
		register("conjure_spear",1240,260, "hematic_flare")
				.setSoftParents("conjure_staff");

		// Row 0 (top) — SUMMA
		register("osseous_bloom",1560,70, "glacial_rampart");
		register("endless_hour",1510,200, "glacial_rampart");

		// Row 1 (middle) — MEDIOCRITAS
		register("glacial_rampart",1460,120, "glacial_grasp");
		register("conjure_flail",1460,170, "glacial_rampart")
				.setSoftParents("conjure_staff");

		// Row 2 (bottom) — HUMILIS
		register("glacial_grasp",1460,70);

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

		// Row 0 (top) — MEDIOCRITAS
		register("umbral_step",2040,160, "blood_eclipse");
		register("black_veil_covenant",1940,90, "void_shroud");

		// Row 1 (bottom) — HUMILIS / MEDIOCRITAS
		register("void_shroud",1870,70);
		register("gloam_laceration",2010,90);
		register("blood_eclipse",2030,30);
		register("conjure_claws",1950,150, "void_shroud")
				.setSoftParents("conjure_staff");

		// Row 0 (top) — SUMMA
		register("crimson_tithe",2300,141, "exsanguinate");
		register("bloom_of_rot",2330,31, "hemorrhage", "exsanguinate");

		// Row 1 (middle) — MEDIOCRITAS
		register("grave_debt",2170,81, "hemorrhage", "exsanguinate");
		register("blackhearted",2160,21, "grave_debt");

		// Degree 4–5 tendency capstones
		register("sovereign_instinct",560,121, "hematic_rebuke");
		register("crimson_coronation",490,133, "sovereign_instinct", "summon_avatar");
		register("iron_choir",820,20, "iron_retort", "ironhearted");
		register("living_circuit",1110,125, "conductive_mark");
		register("white_verdict",1300,300, "unclosing_eye", "prismatic_reproof");
		register("absolute_stillness",1580,260, "glacial_grasp", "endless_hour");
		register("furnace_veins",1800,-20, "vitric_combustion");
		register("phoenix_debt",1860,20, "furnace_veins");
		register("penumbral_drift",1890,10, "black_veil_covenant");
		register("eclipse_well",1970,-30, "penumbral_drift", "blood_eclipse");
		register("carrion_communion",2240,-49, "blackhearted", "bloom_of_rot");
		register("funeral_bell",2240,-114, "carrion_communion");
		register("insatiable_hunger",2320,91, "hemorrhage");

		// Row 2 (bottom) — HUMILIS / MEDIOCRITAS
		register("hemorrhage",2240,11);
		register("lignum_mortis",2390,11);
		register("exsanguinate",2240,81, "hemorrhage");
		register("conjure_axe",2190,141, "exsanguinate")
				.setSoftParents("conjure_staff");
	}


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
