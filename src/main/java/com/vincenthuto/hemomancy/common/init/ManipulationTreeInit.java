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
		register("blood_aneurysm",470,243, "blood_cloud");
		register("summon_avatar",620,193, "blood_cloud", "blood_rush");

		// Row 1 (middle) — MEDIOCRITAS
		register("deadly_gaze",1080,160, "synaptic_jolt");
		register("blood_cloud",510,193, "blood_needle", "blood_shot");
		register("hematic_rebuke",650,243, "blood_rush");

		// Row 2 (bottom) — HUMILIS
		register("blood_shot",510,293, "blood_binding");
		register("blood_needle",530,243);
		register("blood_rush",590,243);
		register("blood_binding",560,353);
		register("vital_effusion",620,293, "blood_rush");
		register("conjure_blade",565,293, "vital_effusion", "blood_binding")
				.setSoftParents("conjure_staff");

		// Row 1 (middle) — MEDIOCRITAS
		register("blood_absorption",980,160);
		register("blood_projection",980,90);
		register("iron_retort",920,50, "sanguine_mending");
		register("sanguine_magnetism",850,180, "iron_retort");
		register("ironhearted",820,130, "iron_retort");

		// Row 2 (bottom) — HUMILIS / MEDIOCRITAS roots
		register("conjure_staff",1040,130, "blood_projection", "blood_absorption");
		register("sanguine_mending",910,130);
		register("vascular_dowsing",920,200, "sanguine_mending");

		// Row 0 (top) — MEDIOCRITAS
		register("sanguine_ward",970,160, "synaptic_jolt");

		// Row 1 (middle) — MEDIOCRITAS root

		// Row 2 (bottom) — HUMILIS
		register("synaptic_jolt",1140,170);
		register("conductive_mark",1025,120, "synaptic_jolt");
		register("hemolymphal_pulse",1020,200, "synaptic_jolt", "deadly_gaze");
		register("conjure_crossbow",1110,220, "hemolymphal_pulse")
				.setSoftParents("conjure_staff");

		// Row 0 (top) — SUMMA
		register("unclosing_eye",1330,180, "hematic_flare");
		register("prismatic_reproof",1271,210, "hematic_flare");

		// Row 1 (middle) — MEDIOCRITAS
		register("hematic_beacon",1261,270, "hematic_flare");
		register("lumen_suture",1291,130, "hematic_flare");

		// Row 2 (bottom) — HUMILIS
		register("hematic_flare",1195,140);
		register("conjure_spear",1191,220, "hematic_flare")
				.setSoftParents("conjure_staff");

		// Row 0 (top) — SUMMA
		register("osseous_bloom",1490,140, "glacial_rampart", "glacial_grasp");
		register("endless_hour",1550,200, "glacial_rampart");

		// Row 1 (middle) — MEDIOCRITAS
		register("glacial_rampart",1540,90);
		register("conjure_flail",1610,170, "glacial_rampart")
				.setSoftParents("conjure_staff");

		// Row 2 (bottom) — HUMILIS
		register("glacial_grasp",1620,90);

		register("vitric_combustion",1760,50, "pyretic_forge");

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
		register("umbral_step",2010,100, "blood_eclipse");
		register("black_veil_covenant",1950,60, "void_shroud");

		// Row 1 (bottom) — HUMILIS / MEDIOCRITAS
		register("void_shroud",1870,70);
		register("gloam_laceration",2050,140);
		register("blood_eclipse",2010,40);
		register("conjure_claws",1950,120, "void_shroud")
				.setSoftParents("conjure_staff");

		// Row 0 (top) — SUMMA
		register("crimson_tithe",2330,143, "exsanguinate");
		register("bloom_of_rot",2330,13, "hemorrhage", "exsanguinate");

		// Row 1 (middle) — MEDIOCRITAS
		register("grave_debt",2210,83, "hemorrhage", "exsanguinate");
		register("blackhearted",2210,13, "grave_debt");

		// Degree 4–5 tendency capstones
		register("sovereign_instinct",640,132, "hematic_rebuke");
		register("crimson_coronation",480,134, "sovereign_instinct", "summon_avatar");
		register("iron_choir",850,70, "iron_retort", "ironhearted");
		register("living_circuit",1110,95, "conductive_mark");
		register("white_verdict",1351,280, "unclosing_eye", "prismatic_reproof", "hematic_beacon");
		register("absolute_stillness",1470,220, "glacial_grasp", "endless_hour");
		register("furnace_veins",1830,50, "vitric_combustion");
		register("phoenix_debt",1800,-10, "furnace_veins");
		register("penumbral_drift",1900,10, "black_veil_covenant");
		register("eclipse_well",1960,-30, "penumbral_drift", "blood_eclipse");
		register("carrion_communion",2270,-58, "blackhearted", "bloom_of_rot");
		register("funeral_bell",2270,-113, "carrion_communion");
		register("insatiable_hunger",2330,83, "hemorrhage");

		// Row 2 (bottom) — HUMILIS / MEDIOCRITAS
		register("hemorrhage",2270,52, "lignum_mortis");
		register("lignum_mortis",2270,-8);
		register("exsanguinate",2270,134, "hemorrhage");
		register("conjure_axe",2210,143, "exsanguinate")
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
