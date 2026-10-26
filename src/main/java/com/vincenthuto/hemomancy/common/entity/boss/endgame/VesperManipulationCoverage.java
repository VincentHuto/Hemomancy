package com.vincenthuto.hemomancy.common.entity.boss.endgame;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals.Form;

import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency.*;

/**
 * Every registered manipulation and how the Evening Star can use it. CASTABLE goes through the shared entity cast
 * path; BOSS_VARIANT is a Vesper-side fight version that fires the manipulation's own authored visual form; EXCLUDED
 * makes no sense in a duel (summons, healing that would undo the player's work, utilities, weapon conjures,
 * death-cheats), each with its reason.
 */
public final class VesperManipulationCoverage {
	public enum Coverage { CASTABLE, BOSS_VARIANT, EXCLUDED }

	/** The shape of a boss variant's effect, applied by {@link VesperManipulationCaster}. */
	public enum Kind { STRIKE, LINE, AREA, SELF_BUFF, DEBUFF, PULL, PUSH, BLINK, SWAP, SPIKES, WALL, LAUNCH, FREEZE,
		BURN, POISON, UNRAVEL, DRAIN, PROJECTILE_FAN }

	public record Spec(Kind kind, Form form, float power) {
	}

	public record Entry(Coverage coverage, @Nullable EnumBloodTendency school, String reason, @Nullable Spec spec) {
	}

	public static final Map<String, Entry> TABLE = new LinkedHashMap<>();

	static {
		// Animus
		variant("venous_travel", DUCTILIS, Kind.BLINK, Form.TELEPORT, 0.0F);
		castable("blood_shot", ANIMUS);
		variant("guided_blood_shot", ANIMUS, Kind.STRIKE, Form.ANIMUS_IMPACT, 6.0F);
		variant("hematic_mortar", ANIMUS, Kind.AREA, Form.ANIMUS_IMPACT, 7.0F);
		variant("sanguine_halo", ANIMUS, Kind.PROJECTILE_FAN, Form.ANIMUS_CONJURE, 4.0F);
		variant("blood_binding", ANIMUS, Kind.DEBUFF, Form.THREAD, 0.0F);
		variant("lingering_blood_binding", ANIMUS, Kind.DEBUFF, Form.THREAD, 0.0F);
		variant("chain_blood_binding", ANIMUS, Kind.PULL, Form.THREAD, 4.0F);
		excluded("sanguine_marionette", "needs a player's sustained aim to steer its victim");
		variant("blood_lattice", ANIMUS, Kind.DEBUFF, Form.THREAD, 0.0F);
		castable("deadly_gaze", DUCTILIS);
		variant("hematic_rebuke", DUCTILIS, Kind.PUSH, Form.COMMAND, 6.0F);
		variant("hematic_riposte", ANIMUS, Kind.STRIKE, Form.RIPOSTE_STRIKE, 9.0F);
		excluded("hematic_impressment", "commands other creatures; Vesper commands his own puppets");
		for (String part : List.of("summon_avatar", "summon_avatar_arms", "summon_avatar_armor", "summon_avatar_legs",
				"summon_avatar_complete")) {
			excluded(part, "summons a blood avatar; his summons are his puppet roster");
		}
		castable("blood_needle", FERRIC);
		variant("blood_needle_fan", FERRIC, Kind.PROJECTILE_FAN, Form.FAN_CHARGE, 4.0F);
		variant("blood_needle_lance", FERRIC, Kind.LINE, Form.LANCE_CHARGE, 9.0F);
		castable("blood_cloud", ANIMUS);
		variant("expansive_blood_cloud", ANIMUS, Kind.AREA, Form.CLOUD, 5.0F);
		variant("pursuing_blood_cloud", ANIMUS, Kind.POISON, Form.CLOUD, 4.0F);
		variant("sanguine_tempest", ANIMUS, Kind.AREA, Form.CLOUD, 8.0F);
		variant("blood_rush", ANIMUS, Kind.SELF_BUFF, Form.RUSH, 0.0F);
		castable("blood_aneurysm", ANIMUS);
		excluded("vital_effusion", "heals; healing himself would undo the player's damage");
		excluded("ferric_transmutation", "transmutes materials; no fight effect");
		castable("activation_potential", DUCTILIS);
		castable("synaptic_jolt", DUCTILIS);
		castable("conductive_mark", DUCTILIS);
		variant("sanguine_ward", DUCTILIS, Kind.SELF_BUFF, Form.WARD, 0.0F);
		castable("hemolymphal_pulse", DUCTILIS);
		for (String conjure : List.of("conjure_blade", "conjure_axe", "conjure_spear", "conjure_claws",
				"conjure_crossbow", "conjure_torch", "conjure_flail", "conjure_sickle", "conjure_staff")) {
			excluded(conjure, "a weapon form; his own weapons already morph between these");
		}
		excluded("blood_absorption", "the player's channel; his wounds and defeat answer it instead");
		excluded("blood_projection", "fills vessels and stations; no fight effect");
		excluded("summon_thrall", "summons a thrall; his summons are his puppet roster");
		castable("crimson_flame_conjuration", FLAMMEUS);
		excluded("sanguine_mending", "heals; healing himself would undo the player's damage");
		excluded("hemosynthesis", "heals; healing himself would undo the player's damage");
		excluded("blood_lamp", "a light utility");
		castable("hematic_flare", LUX);
		excluded("crimson_harvest", "a harvesting utility");
		castable("glacial_grasp", CONGEATIO);
		excluded("sanguine_excavation", "a digging utility");
		excluded("vascular_dowsing", "a detection utility");
		excluded("ferric_resonance", "a detection utility");
		castable("iron_retort", FERRIC);
		excluded("ironhearted", "grants extra Iron Hearts; would pad his health bar");
		castable("sanguine_magnetism", FERRIC);
		variant("ferric_rampart", DUCTILIS, Kind.WALL, Form.FERRIC_CONJURE, 0.0F);
		variant("ferric_spikes", DUCTILIS, Kind.SPIKES, Form.FERRIC_IMPACT, 0.0F);
		castable("pyretic_forge", FLAMMEUS);
		// The shared entity umbral step is a plain teleport; his version arrives through the real umbral visuals.
		variant("umbral_step", TENEBRIS, Kind.BLINK, Form.UMBRA_ARRIVAL, 0.0F);
		excluded("crimson_sight", "a vision utility");
		castable("prismatic_reproof", LUX);
		castable("hematic_beacon", LUX);
		excluded("lumen_suture", "heals; healing himself would undo the player's damage");
		excluded("vital_reservoir", "stores blood; no fight effect");
		castable("cryogenic_pulse", CONGEATIO);
		variant("glacial_circulation", CONGEATIO, Kind.SELF_BUFF, Form.FROZEN_VEINS, 0.0F);
		variant("glacial_bastion", CONGEATIO, Kind.WALL, Form.ICE, 0.0F);
		castable("glacial_rampart", CONGEATIO);
		variant("osseous_bloom", CONGEATIO, Kind.AREA, Form.BONE, 8.0F);
		castable("sanguine_ignition", FLAMMEUS);
		castable("vitric_combustion", FLAMMEUS);
		variant("cauterizing_rebuke", FLAMMEUS, Kind.BURN, Form.CAUTERIZE, 6.0F);
		castable("scalding_updraft", FLAMMEUS);
		variant("soaring_updraft", FLAMMEUS, Kind.LAUNCH, Form.UPDRAFT, 4.0F);
		variant("suspended_updraft", FLAMMEUS, Kind.LAUNCH, Form.UPDRAFT, 2.0F);
		variant("expulsive_updraft", FLAMMEUS, Kind.PUSH, Form.UPDRAFT, 5.0F);
		castable("void_shroud", TENEBRIS);
		castable("gloam_laceration", TENEBRIS);
		castable("blood_eclipse", TENEBRIS);
		excluded("black_veil_covenant", "binds a covenant between allies");
		variant("umbral_reversal", TENEBRIS, Kind.SWAP, Form.UMBRA_SLASH, 0.0F);
		variant("blood_eclipse_mantle", TENEBRIS, Kind.SELF_BUFF, Form.VEIL, 0.0F);
		castable("hemorrhage", MORTEM);
		excluded("lignum_mortis", "grows wood; a building utility");
		excluded("canopy_mortis", "grows a canopy; a building utility");
		excluded("worked_lignum", "shapes wood; a building utility");
		castable("exsanguinate", MORTEM);
		castable("insatiable_hunger", MORTEM);
		castable("grave_debt", MORTEM);
		excluded("blackhearted", "converts wither harm into healing; would pad his health bar");
		variant("crimson_coronation", ANIMUS, Kind.SELF_BUFF, Form.CROWN, 0.0F);
		variant("sovereign_instinct", DUCTILIS, Kind.LINE, Form.SOVEREIGN_LASH, 8.0F);
		excluded("axonal_transduction", "routes the player's own nerves; no entity version");
		variant("living_circuit", DUCTILIS, Kind.STRIKE, Form.CIRCUIT, 7.0F);
		variant("synaptic_storm", DUCTILIS, Kind.AREA, Form.NERVE_PULSE, 9.0F);
		variant("white_verdict", LUX, Kind.STRIKE, Form.WHITE_VERDICT, 12.0F);
		variant("vigil_of_glass", LUX, Kind.AREA, Form.GLASS, 7.0F);
		variant("furnace_veins", FLAMMEUS, Kind.SELF_BUFF, Form.FURNACE, 0.0F);
		excluded("phoenix_debt", "cheats death; would let him skip his defeat");
		variant("absolute_stillness", CONGEATIO, Kind.FREEZE, Form.STILLNESS, 0.0F);
		variant("rimebound_sentence", CONGEATIO, Kind.FREEZE, Form.RIMEBOUND, 6.0F);
		variant("hematic_ballast", FERRIC, Kind.STRIKE, Form.FERRIC_IMPACT, 8.0F);
		variant("iron_choir", FERRIC, Kind.AREA, Form.CHOIR, 7.0F);
		variant("funeral_bell", MORTEM, Kind.PUSH, Form.BELL, 7.0F);
		excluded("carrion_communion", "feeds on corpses; the arena has none");
		variant("penumbral_drift", TENEBRIS, Kind.BLINK, Form.UMBRA_MIST, 0.0F);
		variant("eclipse_well", TENEBRIS, Kind.PULL, Form.WELL, 5.0F);
		variant("searing_lash", FLAMMEUS, Kind.LINE, Form.SEARING_LASH, 8.0F);
		variant("cruor_javelin", CONGEATIO, Kind.LINE, Form.CRUOR_JAVELIN, 10.0F);
		variant("fruiting_contagion", MORTEM, Kind.POISON, Form.CONTAGION, 4.0F);
		variant("umbral_rend", TENEBRIS, Kind.STRIKE, Form.UMBRAL_REND, 9.0F);
		variant("radiant_wake", LUX, Kind.LINE, Form.RADIANT_WAKE, 8.0F);
		variant("lodestone_collapse", FERRIC, Kind.PULL, Form.LODESTONE_COLLAPSE, 8.0F);
		variant("thread_ripper", DUCTILIS, Kind.UNRAVEL, Form.THREAD, 0.0F);
		variant("crimson_tithe", ANIMUS, Kind.DRAIN, Form.TITHE_COLLECT, 6.0F);
		castable("unclosing_eye", LUX);
		castable("bloom_of_rot", MORTEM);
		variant("endless_hour", CONGEATIO, Kind.DEBUFF, Form.HOUR, 0.0F);
	}

	private VesperManipulationCoverage() {
	}

	private static void castable(String id, EnumBloodTendency school) {
		TABLE.put(id, new Entry(Coverage.CASTABLE, school, "", null));
	}

	private static void variant(String id, EnumBloodTendency school, Kind kind, Form form, float power) {
		TABLE.put(id, new Entry(Coverage.BOSS_VARIANT, school, "", new Spec(kind, form, power)));
	}

	private static void excluded(String id, String reason) {
		TABLE.put(id, new Entry(Coverage.EXCLUDED, null, reason, null));
	}

	@Nullable
	public static Entry entry(String id) {
		return TABLE.get(id);
	}

	/** Every fight manipulation of a school, castable or boss variant. */
	public static List<String> usable(EnumBloodTendency school) {
		return TABLE.entrySet().stream()
				.filter(e -> e.getValue().coverage() != Coverage.EXCLUDED && e.getValue().school() == school)
				.map(Map.Entry::getKey).toList();
	}
}
