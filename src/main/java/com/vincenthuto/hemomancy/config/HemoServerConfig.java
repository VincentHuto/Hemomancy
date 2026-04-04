package com.vincenthuto.hemomancy.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.Builder;

public class HemoServerConfig {

	// ===== Blood Volume =====
	public static ForgeConfigSpec.BooleanValue BLOOD_REGEN_ENABLED;
	public static ForgeConfigSpec.DoubleValue BLOOD_REGEN_RATE;
	public static ForgeConfigSpec.IntValue BLOOD_REGEN_INTERVAL;

	// Blood drain on damage
	public static ForgeConfigSpec.BooleanValue BLOOD_DRAIN_ON_DAMAGE_ENABLED;
	public static ForgeConfigSpec.DoubleValue BLOOD_DRAIN_PER_DAMAGE;

	// Blood gain on kill
	public static ForgeConfigSpec.BooleanValue BLOOD_GAIN_ON_KILL_ENABLED;
	public static ForgeConfigSpec.DoubleValue BLOOD_GAIN_PER_KILL;
	public static ForgeConfigSpec.DoubleValue BLOOD_GAIN_BOSS_MULTIPLIER;

	// ===== Blood Tendency =====
	public static ForgeConfigSpec.BooleanValue TENDENCY_SHIFT_ON_KILL_ENABLED;
	public static ForgeConfigSpec.DoubleValue TENDENCY_SHIFT_AMOUNT;
	public static ForgeConfigSpec.DoubleValue TENDENCY_SHIFT_ON_MANIP_USE;

	// ===== Vascular System =====
	public static ForgeConfigSpec.BooleanValue VASCULAR_DEGRADATION_ON_DAMAGE_ENABLED;
	public static ForgeConfigSpec.DoubleValue VASCULAR_DAMAGE_PER_HIT;
	public static ForgeConfigSpec.BooleanValue VASCULAR_DEGRADATION_ON_MANIP_ENABLED;
	public static ForgeConfigSpec.DoubleValue VASCULAR_MANIP_STRAIN;
	public static ForgeConfigSpec.BooleanValue VASCULAR_PASSIVE_HEAL_ENABLED;
	public static ForgeConfigSpec.DoubleValue VASCULAR_HEAL_RATE;
	public static ForgeConfigSpec.IntValue VASCULAR_HEAL_INTERVAL;
	public static ForgeConfigSpec.BooleanValue VASCULAR_DEBUFFS_ENABLED;

	// ===== Morphling =====
	public static ForgeConfigSpec.BooleanValue MORPHLING_PASSIVE_DRAIN_ENABLED;
	public static ForgeConfigSpec.DoubleValue MORPHLING_DRAIN_RATE;
	public static ForgeConfigSpec.IntValue MORPHLING_DRAIN_INTERVAL;

	public static void registerServerConfig(Builder builder) {
		// ───── Blood Volume ─────
		builder.comment("Blood Volume Settings").push("blood_volume");

		BLOOD_REGEN_ENABLED = builder
				.comment("Whether passive blood regeneration is enabled when the blood system is active.")
				.define("bloodRegenEnabled", true);

		BLOOD_REGEN_RATE = builder
				.comment("Amount of blood restored per regen tick.")
				.defineInRange("bloodRegenRate", 1.0, 0.1, 100.0);

		BLOOD_REGEN_INTERVAL = builder
				.comment("How many ticks between each passive blood regen tick. 20 ticks = 1 second.")
				.defineInRange("bloodRegenInterval", 20, 1, 1200);

		BLOOD_DRAIN_ON_DAMAGE_ENABLED = builder
				.comment("Whether the player loses blood volume when taking damage.")
				.define("bloodDrainOnDamageEnabled", true);

		BLOOD_DRAIN_PER_DAMAGE = builder
				.comment("Blood drained per point of damage taken.")
				.defineInRange("bloodDrainPerDamage", 5.0, 0.1, 500.0);

		BLOOD_GAIN_ON_KILL_ENABLED = builder
				.comment("Whether the player gains blood from killing living entities.")
				.define("bloodGainOnKillEnabled", true);

		BLOOD_GAIN_PER_KILL = builder
				.comment("Base blood gained per entity kill.")
				.defineInRange("bloodGainPerKill", 25.0, 1.0, 1000.0);

		BLOOD_GAIN_BOSS_MULTIPLIER = builder
				.comment("Multiplier for blood gained from killing boss entities.")
				.defineInRange("bloodGainBossMultiplier", 5.0, 1.0, 50.0);

		builder.pop();

		// ───── Blood Tendency ─────
		builder.comment("Blood Tendency Settings").push("blood_tendency");

		TENDENCY_SHIFT_ON_KILL_ENABLED = builder
				.comment("Whether killing entities shifts the player's blood tendency.")
				.define("tendencyShiftOnKillEnabled", true);

		TENDENCY_SHIFT_AMOUNT = builder
				.comment("How much tendency alignment is gained per relevant kill.")
				.defineInRange("tendencyShiftAmount", 1.0, 0.1, 100.0);

		TENDENCY_SHIFT_ON_MANIP_USE = builder
				.comment("How much tendency alignment is gained when using a manipulation of that tendency.")
				.defineInRange("tendencyShiftOnManipUse", 0.5, 0.0, 50.0);

		builder.pop();

		// ───── Vascular System ─────
		builder.comment("Vascular System Settings").push("vascular_system");

		VASCULAR_DEGRADATION_ON_DAMAGE_ENABLED = builder
				.comment("Whether taking damage degrades the vascular system of the hit section.")
				.define("vascularDegradationOnDamageEnabled", true);

		VASCULAR_DAMAGE_PER_HIT = builder
				.comment("Vascular health lost per point of damage to a random section.")
				.defineInRange("vascularDamagePerHit", 0.5, 0.01, 50.0);

		VASCULAR_DEGRADATION_ON_MANIP_ENABLED = builder
				.comment("Whether using blood manipulations strains the associated vein section.")
				.define("vascularDegradationOnManipEnabled", true);

		VASCULAR_MANIP_STRAIN = builder
				.comment("Vascular health lost per manipulation use on its associated section.")
				.defineInRange("vascularManipStrain", 1.0, 0.01, 50.0);

		VASCULAR_PASSIVE_HEAL_ENABLED = builder
				.comment("Whether vascular sections passively heal over time.")
				.define("vascularPassiveHealEnabled", true);

		VASCULAR_HEAL_RATE = builder
				.comment("Amount of vascular health restored per heal tick.")
				.defineInRange("vascularHealRate", 0.1, 0.01, 10.0);

		VASCULAR_HEAL_INTERVAL = builder
				.comment("How many ticks between each vascular heal tick. 20 ticks = 1 second.")
				.defineInRange("vascularHealInterval", 100, 1, 6000);

		VASCULAR_DEBUFFS_ENABLED = builder
				.comment("Whether damaged vascular sections apply debuffs (e.g. slowness for dead legs).")
				.define("vascularDebuffsEnabled", true);

		builder.pop();

		// ───── Morphling ─────
		builder.comment("Morphling Settings").push("morphling");

		MORPHLING_PASSIVE_DRAIN_ENABLED = builder
				.comment("Whether an equipped morphling passively drains blood.")
				.define("morphlingPassiveDrainEnabled", true);

		MORPHLING_DRAIN_RATE = builder
				.comment("Blood drained per tick by an equipped morphling.")
				.defineInRange("morphlingDrainRate", 0.5, 0.01, 100.0);

		MORPHLING_DRAIN_INTERVAL = builder
				.comment("How many ticks between each morphling drain tick. 20 ticks = 1 second.")
				.defineInRange("morphlingDrainInterval", 60, 1, 6000);

		builder.pop();
	}

}
