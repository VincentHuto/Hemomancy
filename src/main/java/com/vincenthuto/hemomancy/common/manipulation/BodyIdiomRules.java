package com.vincenthuto.hemomancy.common.manipulation;

import net.minecraft.util.Mth;

public final class BodyIdiomRules {
	public static final float MAX_IRON_HEART_HEALTH = 10.0F;
	public static final float IRON_HEART_HEALTH_PER_CAST = 2.0F;
	public static final int IRON_HEART_DURATION_TICKS = 12_000;
	public static final int IRON_HEART_CHARGE_TICKS = 40;
	public static final float BLACKHEARTED_CONVERSION = 0.65F;
	public static final float BLACKHEARTED_HEALING_FRACTION = 0.5F;
	public static final float NECROTIC_SATURATION_CAP = 12.0F;
	public static final float NECROTIC_RUPTURE_DAMAGE = 6.0F;
	public static final int BLACKHEARTED_COOLDOWN_TICKS = 1_400;

	private BodyIdiomRules() {
	}

	public static float addIronHeartHealth(float current) {
		return Mth.clamp(current + IRON_HEART_HEALTH_PER_CAST, 0.0F, MAX_IRON_HEART_HEALTH);
	}

	public static IronHeartAbsorption absorbWithIronHearts(float ironHeartHealth, float incomingDamage) {
		float resource = Mth.clamp(ironHeartHealth, 0.0F, MAX_IRON_HEART_HEALTH);
		float damage = Math.max(0.0F, incomingDamage);
		float absorbed = Math.min(resource, damage);
		return new IronHeartAbsorption(resource - absorbed, damage - absorbed);
	}

	public static BlackheartedResult metabolizeWither(float incomingDamage, float currentSaturation,
			boolean refractory) {
		float damage = Math.max(0.0F, incomingDamage);
		float saturation = Mth.clamp(currentSaturation, 0.0F, NECROTIC_SATURATION_CAP);
		if (refractory || damage == 0.0F) {
			return new BlackheartedResult(damage, 0.0F, saturation, false);
		}
		float prevented = Math.min(damage * BLACKHEARTED_CONVERSION,
				NECROTIC_SATURATION_CAP - saturation);
		float filled = saturation + prevented;
		boolean ruptured = filled >= NECROTIC_SATURATION_CAP;
		return new BlackheartedResult(damage - prevented,
				prevented * BLACKHEARTED_HEALING_FRACTION,
				ruptured ? 0.0F : filled, ruptured);
	}

	public record IronHeartAbsorption(float ironHeartHealth, float remainingDamage) {
	}

	public record BlackheartedResult(float remainingDamage, float healing, float saturation, boolean ruptured) {
	}
}
