package com.vincenthuto.hemomancy.common.entity.mob.monster.will;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Map;

public final class WillCombatRules {
	private static final double[] BROKEN_HEALTH = { 20.0D, 30.0D, 34.0D, 38.0D };
	private static final double[] BROKEN_DAMAGE = { 3.0D, 5.0D, 6.0D, 6.0D };
	private static final double[] BROKEN_SPEED = { 0.24D, 0.26D, 0.27D, 0.28D };
	private static final double FALTER_BURST_FRACTION = 0.25D;
	private static final int FALTER_BURST_WINDOW_TICKS = 80;
	private static final int FALTER_WINDOW_TICKS = 100;

	private static final Map<EnumBloodTendency, List<ResourceLocation>> SCHOOL_KITS = Map.of(
			EnumBloodTendency.ANIMUS, ids("blood_shot", "blood_needle", "blood_aneurysm", "blood_cloud"),
			EnumBloodTendency.FLAMMEUS,
			ids("crimson_flame_conjuration", "sanguine_ignition", "scalding_updraft", "vitric_combustion"),
			EnumBloodTendency.DUCTILIS, ids("deadly_gaze", "hemolymphal_pulse", "activation_potential"),
			EnumBloodTendency.LUX, ids("hematic_flare", "prismatic_reproof", "hematic_beacon", "unclosing_eye"),
			EnumBloodTendency.MORTEM, ids("hemorrhage", "exsanguinate", "bloom_of_rot"),
			EnumBloodTendency.CONGEATIO, ids("cryogenic_pulse", "glacial_grasp", "glacial_rampart"),
			EnumBloodTendency.FERRIC, ids("blood_needle", "prismatic_reproof", "iron_retort", "pyretic_forge"),
			EnumBloodTendency.TENEBRIS, ids("void_shroud", "gloam_laceration", "umbral_step", "blood_eclipse"));

	private static final Map<EnumBloodTendency, EnumBloodTendency> COUNTERS = Map.of(
			EnumBloodTendency.ANIMUS, EnumBloodTendency.MORTEM,
			EnumBloodTendency.MORTEM, EnumBloodTendency.LUX,
			EnumBloodTendency.DUCTILIS, EnumBloodTendency.FERRIC,
			EnumBloodTendency.FERRIC, EnumBloodTendency.FLAMMEUS,
			EnumBloodTendency.LUX, EnumBloodTendency.TENEBRIS,
			EnumBloodTendency.TENEBRIS, EnumBloodTendency.DUCTILIS,
			EnumBloodTendency.FLAMMEUS, EnumBloodTendency.CONGEATIO,
			EnumBloodTendency.CONGEATIO, EnumBloodTendency.ANIMUS);

	private WillCombatRules() {
	}

	public static Stats brokenStats(int tier) {
		int index = clampTier(tier) - 1;
		return new Stats(BROKEN_HEALTH[index], BROKEN_DAMAGE[index], BROKEN_SPEED[index]);
	}

	public static Stats sentStats(int playerDegree, double playerMaxHealth) {
		int degree = Math.max(6, playerDegree);
		double health = Math.max(28.0D, playerMaxHealth * 1.2D + (degree - 6) * 8.0D);
		double damage = 6.0D + Math.max(0, degree - 6) * 1.25D;
		double speed = 0.29D + Math.min(0.04D, Math.max(0, degree - 6) * 0.01D);
		return new Stats(health, damage, speed);
	}

	public static List<ResourceLocation> schoolKit(EnumBloodTendency school, int tier) {
		List<ResourceLocation> kit = SCHOOL_KITS.getOrDefault(school, SCHOOL_KITS.get(EnumBloodTendency.ANIMUS));
		int count = Math.min(kit.size(), Math.max(1, clampTier(tier)));
		return List.copyOf(kit.subList(0, count));
	}

	public static EnumBloodTendency counterSchool(EnumBloodTendency school) {
		return COUNTERS.getOrDefault(school, EnumBloodTendency.MORTEM);
	}

	public static double falterBurstFraction() {
		return FALTER_BURST_FRACTION;
	}

	public static int falterBurstWindowTicks() {
		return FALTER_BURST_WINDOW_TICKS;
	}

	public static int falterWindowTicks() {
		return FALTER_WINDOW_TICKS;
	}

	public static int castIntervalTicks(int tier) {
		return switch (clampTier(tier)) {
		case 1 -> 100;
		case 2 -> 80;
		case 3 -> 70;
		default -> 60;
		};
	}

	public static LootProfile lootFor(EnumBloodTendency school, int tier, WillOrigin origin) {
		return new LootProfile(school, Math.max(1, clampTier(tier)), origin == WillOrigin.BROKEN);
	}

	private static int clampTier(int tier) {
		return Math.max(1, Math.min(4, tier));
	}

	private static List<ResourceLocation> ids(String... names) {
		return java.util.Arrays.stream(names).map(Hemomancy::rloc).toList();
	}

	public record Stats(double maxHealth, double attackDamage, double movementSpeed) {
	}

	public record LootProfile(EnumBloodTendency school, int rolls, boolean canDropFadedMemory) {
	}
}
