package com.vincenthuto.hemomancy.common.entity.boss.endgame;

import net.minecraft.util.RandomSource;

/**
 * The Evening Star's three acts. Locked: one weapon per stance, its chained moveset and its school's spells. Open:
 * weapons and schools swap mid-animation and any school's fight manipulations chain together. Sickles: the dual
 * Living Sickles to the end.
 */
public enum VesperEveningStarAct {
	LOCKED, OPEN, SICKLES;

	public static VesperEveningStarAct of(float health, float maxHealth) {
		if (maxHealth <= 0.0F) return LOCKED;
		float fraction = health / maxHealth;
		if (fraction <= 1.0F / 3.0F + 1.0E-6F) return SICKLES;
		if (fraction <= 2.0F / 3.0F + 1.0E-6F) return OPEN;
		return LOCKED;
	}

	public boolean locksWeaponToStance() {
		return this == LOCKED;
	}

	public boolean castsAnySchool() {
		return this == OPEN;
	}

	/** Between strings he holds one strafing side for a beat, then plants and pivots, instead of jittering. */
	public static int strafeHoldTicks(RandomSource random) {
		return 20 + random.nextInt(21);
	}
}
