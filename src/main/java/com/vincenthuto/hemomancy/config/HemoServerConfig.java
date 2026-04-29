package com.vincenthuto.hemomancy.config;

import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.ModConfigSpec.Builder;

public class HemoServerConfig {

	// ===== Blood Volume =====
	public static ModConfigSpec.BooleanValue BLOOD_REGEN_ENABLED;
	public static ModConfigSpec.DoubleValue BLOOD_REGEN_RATE;
	public static ModConfigSpec.IntValue BLOOD_REGEN_INTERVAL;

	// Blood drain on damage
	public static ModConfigSpec.BooleanValue BLOOD_DRAIN_ON_DAMAGE_ENABLED;
	public static ModConfigSpec.DoubleValue BLOOD_DRAIN_PER_DAMAGE;

	// Blood gain on kill
	public static ModConfigSpec.BooleanValue BLOOD_GAIN_ON_KILL_ENABLED;
	public static ModConfigSpec.DoubleValue BLOOD_GAIN_PER_KILL;
	public static ModConfigSpec.DoubleValue BLOOD_GAIN_BOSS_MULTIPLIER;

	// ===== Blood Tendency =====
	public static ModConfigSpec.BooleanValue TENDENCY_SHIFT_ON_KILL_ENABLED;
	public static ModConfigSpec.DoubleValue TENDENCY_SHIFT_AMOUNT;
	public static ModConfigSpec.DoubleValue TENDENCY_SHIFT_ON_MANIP_USE;

	// ===== Vascular System =====
	public static ModConfigSpec.BooleanValue VASCULAR_DEGRADATION_ON_DAMAGE_ENABLED;
	public static ModConfigSpec.DoubleValue VASCULAR_DAMAGE_PER_HIT;
	public static ModConfigSpec.BooleanValue VASCULAR_DEGRADATION_ON_MANIP_ENABLED;
	public static ModConfigSpec.DoubleValue VASCULAR_MANIP_STRAIN;
	public static ModConfigSpec.BooleanValue VASCULAR_PASSIVE_HEAL_ENABLED;
	public static ModConfigSpec.DoubleValue VASCULAR_HEAL_RATE;
	public static ModConfigSpec.IntValue VASCULAR_HEAL_INTERVAL;
	public static ModConfigSpec.BooleanValue VASCULAR_DEBUFFS_ENABLED;

	// ===== Bloodline =====
	public static ModConfigSpec.BooleanValue BLOODLINE_POOL_ENABLED;
	public static ModConfigSpec.DoubleValue BLOODLINE_POOL_CONTRIBUTION_RATE;
	public static ModConfigSpec.IntValue BLOODLINE_POOL_CONTRIBUTION_INTERVAL;
	public static ModConfigSpec.DoubleValue BLOODLINE_POOL_MIN_BLOOD_THRESHOLD;
	public static ModConfigSpec.DoubleValue BLOODLINE_AUTO_DRAW_MAX_RATE;
	public static ModConfigSpec.BooleanValue BLOODLINE_HEAL_ENABLED;
	public static ModConfigSpec.DoubleValue BLOODLINE_HEAL_AMOUNT;
	public static ModConfigSpec.IntValue BLOODLINE_HEAL_INTERVAL;
	public static ModConfigSpec.DoubleValue BLOODLINE_HEAL_HEALTH_THRESHOLD;
	public static ModConfigSpec.DoubleValue BLOODLINE_HEAL_RANGE;

	// ===== Morphling =====
	public static ModConfigSpec.BooleanValue MORPHLING_PASSIVE_DRAIN_ENABLED;
	public static ModConfigSpec.DoubleValue MORPHLING_DRAIN_RATE;
	public static ModConfigSpec.IntValue MORPHLING_DRAIN_INTERVAL;
	public static ModConfigSpec.BooleanValue MORPHLING_CRADLE_LEECH_TARGET_PLAYERS;

	// ===== Ghastly Alembic Leak =====
	public static ModConfigSpec.IntValue ALEMBIC_LEAK_INTERVAL_TICKS;
	public static ModConfigSpec.DoubleValue ALEMBIC_LEAK_RATE_PER_TICK;

	// ===== Drudge System =====
	public static ModConfigSpec.IntValue DRUDGE_LEASH_RADIUS;
	public static ModConfigSpec.IntValue DRUDGE_MAX_PER_SSC;
	public static ModConfigSpec.DoubleValue DRUDGE_BIRTH_COST;
	public static ModConfigSpec.IntValue DRUDGE_ROGUE_TIMEOUT_TICKS;
	public static ModConfigSpec.DoubleValue DRUDGE_ACTION_COST_MULTIPLIER;
	public static ModConfigSpec.IntValue DRUDGE_COOLDOWN_MULTIPLIER;
	public static ModConfigSpec.IntValue DRUDGE_REQUIRED_DEGREE;
	public static ModConfigSpec.IntValue DRUDGE_WORK_RADIUS;

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

		// ───── Bloodline ─────
		builder.comment("Bloodline Settings").push("bloodline");

		BLOODLINE_POOL_ENABLED = builder
				.comment("Whether bloodline members passively contribute blood to the shared pool.")
				.define("bloodlinePoolEnabled", true);

		BLOODLINE_POOL_CONTRIBUTION_RATE = builder
				.comment("Amount of blood contributed to the shared pool per tick interval.")
				.defineInRange("bloodlinePoolContributionRate", 0.5, 0.01, 100.0);

		BLOODLINE_POOL_CONTRIBUTION_INTERVAL = builder
				.comment("How many ticks between each bloodline pool contribution. 20 ticks = 1 second.")
				.defineInRange("bloodlinePoolContributionInterval", 100, 1, 6000);

		BLOODLINE_POOL_MIN_BLOOD_THRESHOLD = builder
				.comment("Minimum blood percentage a player must have to contribute to the shared pool (0.0-1.0).")
				.defineInRange("bloodlinePoolMinBloodThreshold", 0.25, 0.0, 1.0);

		BLOODLINE_AUTO_DRAW_MAX_RATE = builder
				.comment("Maximum blood per tick that auto-draw can pull from the shared bloodline pool.")
				.defineInRange("bloodlineAutoDrawMaxRate", 2.0, 0.1, 100.0);

		BLOODLINE_HEAL_ENABLED = builder
				.comment("Whether bloodline members can heal each other when nearby.")
				.define("bloodlineHealEnabled", true);

		BLOODLINE_HEAL_AMOUNT = builder
				.comment("Amount of health restored per heal tick from the shared bloodline pool.")
				.defineInRange("bloodlineHealAmount", 1.0, 0.1, 20.0);

		BLOODLINE_HEAL_INTERVAL = builder
				.comment("How many ticks between each bloodline heal tick. 20 ticks = 1 second.")
				.defineInRange("bloodlineHealInterval", 40, 1, 6000);

		BLOODLINE_HEAL_HEALTH_THRESHOLD = builder
				.comment("Player health percentage below which bloodline healing activates (0.0-1.0).")
				.defineInRange("bloodlineHealHealthThreshold", 0.5, 0.05, 1.0);

		BLOODLINE_HEAL_RANGE = builder
				.comment("Maximum distance (in blocks) between bloodline members for healing to work.")
				.defineInRange("bloodlineHealRange", 32.0, 1.0, 256.0);

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
		MORPHLING_CRADLE_LEECH_TARGET_PLAYERS = builder
				.comment("Whether Leech Morphling Cradles can target non-owner players.")
				.define("morphlingCradleLeechTargetPlayers", false);

		builder.pop();

		// ───── Ghastly Alembic Leak ─────
		builder.comment("Ghastly Alembic Leak Settings").push("alembic_leak");

		ALEMBIC_LEAK_INTERVAL_TICKS = builder
				.comment("How many ticks between each alembic leak tick. 20 ticks = 1 second. "
						+ "Each tick, blood is drained and the crystal bud below is advanced if enough ticks have accumulated.")
				.defineInRange("alembicLeakIntervalTicks", 200, 20, 6000);

		ALEMBIC_LEAK_RATE_PER_TICK = builder
				.comment("Blood drained from the alembic per leak interval when a dripstone is directly below.")
				.defineInRange("alembicLeakRatePerTick", 50.0, 1.0, 2000.0);

		builder.pop();

		// ───── Drudge System ─────
		builder.comment("Drudge System Settings").push("drudge");

		DRUDGE_LEASH_RADIUS = builder
				.comment("Maximum distance (in blocks) a Drudge may wander from its bound SSC before returning.")
				.defineInRange("drudgeLeashRadius", 24, 4, 128);

		DRUDGE_MAX_PER_SSC = builder
				.comment("Maximum number of Drudges that can be bound to a single SSC.")
				.defineInRange("drudgeMaxPerSSC", 3, 1, 10);

		DRUDGE_BIRTH_COST = builder
				.comment("Blood cost (mL) to birth a new Drudge from an SSC using the Drudge Electrode.")
				.defineInRange("drudgeBirthCost", 3000.0, 100.0, 20000.0);

		DRUDGE_ROGUE_TIMEOUT_TICKS = builder
				.comment("Ticks a Drudge will try to pathfind back to its SSC before going rogue (200 = 10 seconds).")
				.defineInRange("drudgeRogueTimeoutTicks", 200, 20, 6000);

		DRUDGE_ACTION_COST_MULTIPLIER = builder
				.comment("Blood cost multiplier applied when a Drudge executes a manipulation (1.5 = 50% more expensive).")
				.defineInRange("drudgeActionCostMultiplier", 1.5, 0.5, 5.0);

		DRUDGE_COOLDOWN_MULTIPLIER = builder
				.comment("Cooldown multiplier applied when a Drudge executes a manipulation (2 = twice as long).")
				.defineInRange("drudgeCooldownMultiplier", 2, 1, 10);

		DRUDGE_REQUIRED_DEGREE = builder
				.comment("Minimum Initiatory Degree required to birth a Drudge (3 = Illuminatus).")
				.defineInRange("drudgeRequiredDegree", 3, 0, 8);

		DRUDGE_WORK_RADIUS = builder
				.comment("Radius (blocks) within which the Drudge scans for targets to apply its memory.")
				.defineInRange("drudgeWorkRadius", 12, 2, 48);

		builder.pop();
	}

}
