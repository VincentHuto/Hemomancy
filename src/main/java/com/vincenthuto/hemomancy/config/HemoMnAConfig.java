package com.vincenthuto.hemomancy.config;

import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.ModConfigSpec.Builder;

/**
 * Server-side config options for all Hemomancy × Mana and Artifice cross-mod
 * mechanics. These are only registered when MnA is present.
 */
public class HemoMnAConfig {

	// ===== Blood ↔ Mana Conversion =====
	public static ModConfigSpec.DoubleValue MANA_TO_BLOOD_RATIO;
	public static ModConfigSpec.DoubleValue BLOOD_TO_MANA_RATIO;

	// ===== Blood Tithe =====
	public static ModConfigSpec.BooleanValue BLOOD_TITHE_ENABLED;
	public static ModConfigSpec.DoubleValue BLOOD_TITHE_MANA_REDUCTION;
	public static ModConfigSpec.DoubleValue BLOOD_TITHE_BLOOD_PER_MANA;

	// ===== Living Thread Armor =====
	public static ModConfigSpec.DoubleValue LIVING_THREAD_SET_BONUS_MAX_MANA;
	public static ModConfigSpec.DoubleValue LIVING_THREAD_SET_BONUS_MANA_REGEN;

	// ===== Broken Mana Trapezohedron =====
	public static ModConfigSpec.IntValue TRAPEZOHEDRON_EFFECT_RADIUS;

	// ===== Spell → Manipulation Combos =====
	public static ModConfigSpec.BooleanValue SPELL_MANIP_COMBO_ENABLED;
	public static ModConfigSpec.IntValue ARCANE_RESONANCE_DURATION;
	public static ModConfigSpec.DoubleValue ARCANE_RESONANCE_BLOOD_REDUCTION;
	public static ModConfigSpec.IntValue SANGUINE_CLARITY_DURATION;
	public static ModConfigSpec.DoubleValue SANGUINE_CLARITY_MANA_REDUCTION;

	// ===== Sanguilith Summon =====
	public static ModConfigSpec.DoubleValue SANGUILITH_HEALTH_PER_MAGNITUDE;
	public static ModConfigSpec.IntValue SANGUILITH_MAX_SUMMONS;

	public static ModConfigSpec register() {
		Builder builder = new Builder();

		// ───── Blood ↔ Mana Conversion ─────
		builder.comment("Blood ↔ Mana Conversion Ratios").push("conversion");

		MANA_TO_BLOOD_RATIO = builder
				.comment("Blood gained per 1 mana consumed by the Sanguine Transmutation spell component.")
				.defineInRange("manaToBloodRatio", 5.0, 0.1, 100.0);

		BLOOD_TO_MANA_RATIO = builder
				.comment("Mana gained per 1 blood consumed by the Sanguine Offering spell component.")
				.defineInRange("bloodToManaRatio", 0.2, 0.01, 10.0);

		builder.pop();

		// ───── Blood Tithe ─────
		builder.comment("Blood Tithe — Harbinger faction perk that lets blood-affinity spells"
				+ " partially pay their mana cost with blood instead.").push("blood_tithe");

		BLOOD_TITHE_ENABLED = builder
				.comment("Whether the Blood Tithe mechanic is enabled for Harbinger faction members.")
				.define("bloodTitheEnabled", true);

		BLOOD_TITHE_MANA_REDUCTION = builder
				.comment("Percentage of mana cost that is converted to blood cost for blood-affinity spells (0.0–1.0)."
						+ " Default 0.25 means 25% of mana cost is paid in blood instead.")
				.defineInRange("bloodTitheManaReduction", 0.25, 0.0, 1.0);

		BLOOD_TITHE_BLOOD_PER_MANA = builder
				.comment("Blood drained per 1 mana replaced by Blood Tithe.")
				.defineInRange("bloodTitheBloodPerMana", 5.0, 0.1, 100.0);

		builder.pop();

		// ───── Living Thread Armor ─────
		builder.comment("Living Thread Armor Set Bonus Values").push("living_thread_armor");

		LIVING_THREAD_SET_BONUS_MAX_MANA = builder
				.comment("Bonus max mana granted by the 3-piece Living Thread set bonus.")
				.defineInRange("setBonus3pcMaxMana", 500.0, 0.0, 10000.0);

		LIVING_THREAD_SET_BONUS_MANA_REGEN = builder
				.comment("Mana regen multiplier from the 3-piece Living Thread set bonus. 0.5 = +50%.")
				.defineInRange("setBonus3pcManaRegen", 0.5, 0.0, 10.0);

		builder.pop();

		// ───── Broken Mana Trapezohedron ─────
		builder.comment("Broken Mana Trapezohedron Settings").push("trapezohedron");

		TRAPEZOHEDRON_EFFECT_RADIUS = builder
				.comment("Effect radius of the Broken Mana Trapezohedron's mana regen aura (in blocks).")
				.defineInRange("effectRadius", 8, 1, 64);

		builder.pop();

		// ───── Spell → Manipulation Combos ─────
		builder.comment("Spell ↔ Manipulation Combo System").push("spell_manip_combos");

		SPELL_MANIP_COMBO_ENABLED = builder
				.comment("Whether casting MnA blood spells and Hemomancy manipulations in quick succession"
						+ " grants resource cost reduction buffs.")
				.define("comboSystemEnabled", true);

		ARCANE_RESONANCE_DURATION = builder
				.comment("Duration in ticks of the Arcane Resonance buff (granted after casting a blood-affinity"
						+ " MnA spell). Reduces the blood cost of the next Hemomancy manipulation.")
				.defineInRange("arcaneResonanceDuration", 100, 20, 600);

		ARCANE_RESONANCE_BLOOD_REDUCTION = builder
				.comment("Percentage reduction in blood cost for the next manipulation while Arcane Resonance"
						+ " is active (0.0–1.0). Default 0.25 means 25% less blood cost.")
				.defineInRange("arcaneResonanceBloodReduction", 0.25, 0.0, 1.0);

		SANGUINE_CLARITY_DURATION = builder
				.comment("Duration in ticks of the Sanguine Clarity buff (granted after using a Hemomancy"
						+ " manipulation). Reduces the mana cost of the next MnA spell.")
				.defineInRange("sanguineClarityDuration", 100, 20, 600);

		SANGUINE_CLARITY_MANA_REDUCTION = builder
				.comment("Percentage reduction in mana cost for the next spell while Sanguine Clarity"
						+ " is active (0.0–1.0). Default 0.20 means 20% less mana cost.")
				.defineInRange("sanguineClarityManaReduction", 0.20, 0.0, 1.0);

		builder.pop();

		// ───── Sanguilith Summon ─────
		builder.comment("Sanguilith Summon Component Settings").push("sanguilith");

		SANGUILITH_HEALTH_PER_MAGNITUDE = builder
				.comment("Bonus health per magnitude point above 1 for summoned Sanguliliths.")
				.defineInRange("healthPerMagnitude", 10.0, 1.0, 100.0);

		SANGUILITH_MAX_SUMMONS = builder
				.comment("Maximum number of Sanguliliths a player can have active at once.")
				.defineInRange("maxSummons", 2, 1, 10);

		builder.pop();

		return builder.build();
	}
}
