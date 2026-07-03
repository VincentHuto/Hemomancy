package com.vincenthuto.hemomancy.config;

import com.vincenthuto.hemomancy.common.event.HematicSalvageRules;
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
	public static ModConfigSpec.DoubleValue TENDENCY_BOSS_MULTIPLIER;

	// ===== Hematic Salvage =====
	public static ModConfigSpec.BooleanValue HEMATIC_SALVAGE_ENABLED;
	public static ModConfigSpec.DoubleValue HEMATIC_SALVAGE_MIN_DAMAGE;
	public static ModConfigSpec.IntValue HEMATIC_SALVAGE_COOLDOWN_TICKS;
	public static ModConfigSpec.DoubleValue HEMATIC_SALVAGE_BASE_CHANCE;
	public static ModConfigSpec.DoubleValue HEMATIC_SALVAGE_CHANCE_PER_PIECE;
	public static ModConfigSpec.DoubleValue HEMATIC_SALVAGE_MAX_CHANCE;
	public static ModConfigSpec.IntValue HEMATIC_SALVAGE_MAX_DEGREE;

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
	public static ModConfigSpec.IntValue FANE_MAX_STAKE_BUDGET;

	// ===== Morphling =====
	public static ModConfigSpec.BooleanValue MORPHLING_PASSIVE_DRAIN_ENABLED;
	public static ModConfigSpec.DoubleValue MORPHLING_DRAIN_RATE;
	public static ModConfigSpec.IntValue MORPHLING_DRAIN_INTERVAL;
	public static ModConfigSpec.BooleanValue MORPHLING_CRADLE_LEECH_TARGET_PLAYERS;
	public static ModConfigSpec.BooleanValue MORPHLING_HUNGER_ENABLED;
	public static ModConfigSpec.IntValue MORPHLING_FED_DURATION_TICKS;
	public static ModConfigSpec.DoubleValue MORPHLING_STARVING_DRAIN_RATE;
	public static ModConfigSpec.IntValue MORPHLING_HUSBANDRY_STAGE_QUOTA;

	// ===== Ghastly Alembic Leak =====
	public static ModConfigSpec.IntValue ALEMBIC_LEAK_INTERVAL_TICKS;
	public static ModConfigSpec.DoubleValue ALEMBIC_LEAK_RATE_PER_TICK;

	// ===== Power Systems Guardrails (audit phase 1) =====
	public static ModConfigSpec.BooleanValue CIRCULATION_ENABLED;
	public static ModConfigSpec.DoubleValue CIRCULATION_BASE_BANDWIDTH;
	public static ModConfigSpec.DoubleValue CIRCULATION_BANDWIDTH_PER_DEGREE;
	public static ModConfigSpec.DoubleValue CIRCULATION_BANDWIDTH_PER_CAPACITY_POINT;
	public static ModConfigSpec.IntValue CIRCULATION_WINDOW_TICKS;
	public static ModConfigSpec.BooleanValue LAST_RITE_ENABLED;
	public static ModConfigSpec.IntValue LAST_RITE_SHARED_COOLDOWN_TICKS;
	public static ModConfigSpec.BooleanValue BORROWED_BLOOD_ENABLED;
	public static ModConfigSpec.DoubleValue BORROWED_BLOOD_CAP;

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

		TENDENCY_BOSS_MULTIPLIER = builder
				.comment("Multiplier applied to tendency shift when killing a boss entity (Ender Dragon, Wither, Elder Guardian, Warden). Default 10x the base shift amount.")
				.defineInRange("tendencyBossMultiplier", 10.0, 1.0, 100.0);

		builder.pop();

		// ───── Hematic Salvage ─────
		builder.comment("First-hour Hematic Salvage Settings").push("hematic_salvage");

		HEMATIC_SALVAGE_ENABLED = builder
				.comment("Whether serious wounds can shake Hematic Iron Scrap loose from damaged vanilla iron armor.")
				.define("enabled", true);

		HEMATIC_SALVAGE_MIN_DAMAGE = builder
				.comment("Minimum post-mitigation damage required to roll for Hematic Iron Scrap. 2 damage = 1 heart.")
				.defineInRange("minimumDamage", HematicSalvageRules.DEFAULT_MINIMUM_DAMAGE, 0.0, 100.0);

		HEMATIC_SALVAGE_COOLDOWN_TICKS = builder
				.comment("Ticks between successful Hematic Iron Scrap drops per player.")
				.defineInRange("cooldownTicks", 1200, 0, 72000);

		HEMATIC_SALVAGE_BASE_CHANCE = builder
				.comment("Base chance for Hematic Iron Scrap when the player has at least one damaged vanilla iron armor piece.")
				.defineInRange("baseChance", 0.12, 0.0, 1.0);

		HEMATIC_SALVAGE_CHANCE_PER_PIECE = builder
				.comment("Additional chance per damaged vanilla iron armor piece.")
				.defineInRange("chancePerDamagedPiece", 0.04, 0.0, 1.0);

		HEMATIC_SALVAGE_MAX_CHANCE = builder
				.comment("Maximum Hematic Iron Scrap drop chance after damaged armor bonuses.")
				.defineInRange("maxChance", 0.25, 0.0, 1.0);

		HEMATIC_SALVAGE_MAX_DEGREE = builder
				.comment("Highest Harbinger degree that can receive first-hour Hematic Iron Scrap from wounded iron.")
				.defineInRange("maximumDegree", 2, 0, 8);

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
				.comment("Whether damaged vascular sections apply debuffs (e.g. slowness for dead legs). for players who want an extra level of punishment")
				.define("vascularDebuffsEnabled", false);

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

		FANE_MAX_STAKE_BUDGET = builder
				.comment("Maximum Hematic Stake anchors a single Founding Fane may use.")
				.defineInRange("faneMaxStakeBudget", 12, 3, 64);

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

		MORPHLING_HUNGER_ENABLED = builder
				.comment("Whether Mature+ non-wild-bound morphlings track Fed, Hungry, and Starving states.")
				.define("hungerEnabled", false);

		MORPHLING_FED_DURATION_TICKS = builder
				.comment("Ticks a fed Mature+ morphling stays Fed before becoming Hungry. 24000 = one Minecraft day.")
				.defineInRange("fedDurationTicks", 24000, 20, 240000);

		MORPHLING_STARVING_DRAIN_RATE = builder
				.comment("Extra blood drained per hunger tick by a Starving morphling when hunger is enabled.")
				.defineInRange("starvingDrainRate", 2.0, 0.0, 100.0);

		MORPHLING_HUSBANDRY_STAGE_QUOTA = builder
				.comment("Husbandry progress required per post-Fledgling maturity stage. 0 preserves enzyme-only progression.")
				.defineInRange("husbandryStageQuota", 0, 0, 100000);

		builder.pop();

		// ───── Ghastly Alembic Leak ─────
		builder.comment("Ghastly Alembic Leak Settings").push("alembic_leak");

		ALEMBIC_LEAK_INTERVAL_TICKS = builder
				.comment("How many ticks between each alembic leak tick. 20 ticks = 1 second. "
						+ "Each tick, blood is drained and the blood crystal below is advanced if enough ticks have accumulated.")
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

		// ───── Power Systems Guardrails (audit phase 1) ─────
		builder.comment("Power Systems Guardrail Settings — shared governors for stacked passive income, "
				+ "death-saves, and the borrowed-blood reserve. Single sources are never affected; only stacks are disciplined.")
				.push("guardrails");

		CIRCULATION_ENABLED = builder
				.comment("Whether all passive blood income (armor regen, mask trickles, morphling siphons, cradle redistribution) "
						+ "shares one bandwidth window. Disabled = pure pass-through, pre-guardrail behavior.")
				.define("circulationEnabled", true);

		CIRCULATION_BASE_BANDWIDTH = builder
				.comment("Base passive blood income allowed per window, before degree and Capacity scaling.")
				.defineInRange("circulationBaseBandwidth", 6.0, 0.0, 1000.0);

		CIRCULATION_BANDWIDTH_PER_DEGREE = builder
				.comment("Additional bandwidth per Initiatory Degree.")
				.defineInRange("circulationBandwidthPerDegree", 1.5, 0.0, 100.0);

		CIRCULATION_BANDWIDTH_PER_CAPACITY_POINT = builder
				.comment("Additional bandwidth per point of Capacity-skill blood bonus (500 bonus = one skill level).")
				.defineInRange("circulationBandwidthPerCapacityPoint", 0.002, 0.0, 1.0);

		CIRCULATION_WINDOW_TICKS = builder
				.comment("Length of the circulation window in ticks. 20 ticks = 1 second.")
				.defineInRange("circulationWindowTicks", 20, 1, 1200);

		LAST_RITE_ENABLED = builder
				.comment("Whether all death-prevention effects (Ink Mantle, Last-Light Mantle, Silent Archon refusal) "
						+ "share one cooldown: the blood may refuse the return only once per window.")
				.define("lastRiteEnabled", true);

		LAST_RITE_SHARED_COOLDOWN_TICKS = builder
				.comment("Shared cooldown after any death-save fires, in ticks (12000 = 10 minutes).")
				.defineInRange("lastRiteSharedCooldownTicks", 12000, 0, 240000);

		BORROWED_BLOOD_ENABLED = builder
				.comment("Whether the borrowed-blood reserve accepts deposits (Blood Lust overkill lifesteal; "
						+ "future morphling feed-banking). Draining an existing reserve always works.")
				.define("borrowedBloodEnabled", true);

		BORROWED_BLOOD_CAP = builder
				.comment("Maximum blood the borrowed reserve can hold.")
				.defineInRange("borrowedBloodCap", 500.0, 0.0, 10000.0);

		builder.pop();
	}

}
