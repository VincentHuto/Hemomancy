package com.vincenthuto.hemomancy.common.item.harbinger.tool.living;

import net.minecraft.util.Mth;

public final class LivingSpearLuxRules {
	public static final int MIN_LIGHT = 10;
	public static final float DAMAGE_THRESHOLD = 30.0F;
	public static final double BLAST_RADIUS = 5.0D;
	public static final float BLAST_DAMAGE = 8.0F;
	public static final int ILLUMINATION_TICKS = 200;

	private LivingSpearLuxRules() { }

	public static boolean qualifies(int localBrightness, boolean illuminated) {
		return localBrightness >= MIN_LIGHT || illuminated;
	}

	public static float addCharge(float current, float dealtDamage, boolean qualifies) {
		if (!qualifies || dealtDamage <= 0.0F) return Mth.clamp(current, 0.0F, DAMAGE_THRESHOLD);
		return Mth.clamp(current + dealtDamage, 0.0F, DAMAGE_THRESHOLD);
	}

	public static float chargeFraction(float charge) {
		return Mth.clamp(charge / DAMAGE_THRESHOLD, 0.0F, 1.0F);
	}

	public static boolean shouldBurst(float charge, float attackStrength, boolean successfulHit) {
		return successfulHit && charge >= DAMAGE_THRESHOLD && attackStrength >= 0.999F;
	}
}
