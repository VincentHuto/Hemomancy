package com.vincenthuto.hemomancy.common.manipulation;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;

import javax.annotation.Nullable;

public final class ManipulationStatusRules {
	public static final int CONDUCTIVE_MARK_TICKS = 160;
	public static final int CONDUCTIVE_ARC_INTERVAL_TICKS = 15;
	public static final int CONDUCTIVE_ARC_TARGETS = 3;
	public static final double CONDUCTIVE_ARC_RADIUS = 5.0D;
	public static final float CONDUCTIVE_ARC_DAMAGE = 2.0F;

	public static final float INSATIABLE_HEAL_MULTIPLIER = 0.25F;
	public static final int INSATIABLE_FOOD_HUNGER_TICKS = 160;
	public static final int INSATIABLE_FOOD_HUNGER_AMPLIFIER = 1;
	public static final float INSATIABLE_FOOD_EXHAUSTION = 8.0F;

	public static final int GRAVE_DEBT_TICKS = 180;
	public static final float GRAVE_DEBT_HEALTH_FRACTION = 0.25F;
	public static final double GRAVE_DEBT_RADIUS = 4.0D;
	public static final float GRAVE_DEBT_BURST_DAMAGE = 5.0F;
	public static final double GRAVE_DEBT_DEATH_REFUND = 250.0D;

	public static final int IRON_RETORT_TICKS = 60;
	public static final float IRON_RETORT_DAMAGE_MULTIPLIER = 0.5F;
	public static final float IRON_RETORT_DAMAGE = 4.0F;

	public static final int SANGUINE_MAGNETISM_TICKS = 120;
	public static final double SANGUINE_MAGNETISM_RADIUS = 8.0D;
	public static final double SANGUINE_MAGNETISM_PIN_RADIUS = 1.3D;

	private ManipulationStatusRules() {
	}

	public static boolean isConductiveSchool(@Nullable EnumBloodTendency primary,
			@Nullable EnumBloodTendency secondary) {
		return isConductiveSchoolName(primary == null ? null : primary.name(),
				secondary == null ? null : secondary.name());
	}

	public static boolean isConductiveSchoolName(@Nullable String primary, @Nullable String secondary) {
		return isConductiveTendencyName(primary) || isConductiveTendencyName(secondary);
	}

	public static boolean canTriggerConductiveArc(long gameTime, long lastArcTick) {
		return lastArcTick < 0L || gameTime - lastArcTick >= CONDUCTIVE_ARC_INTERVAL_TICKS;
	}

	public static boolean crossedGraveDebtThreshold(float previousHealth, float currentHealth, float maxHealth) {
		float threshold = maxHealth * GRAVE_DEBT_HEALTH_FRACTION;
		return previousHealth > threshold && currentHealth <= threshold;
	}

	public static boolean canMagnetize(boolean hostile, boolean allied, boolean owner) {
		return hostile && !allied && !owner;
	}

	private static boolean isConductiveTendencyName(@Nullable String tendency) {
		return "DUCTILIS".equals(tendency)
				|| "LUX".equals(tendency)
				|| "FERRIC".equals(tendency);
	}
}
