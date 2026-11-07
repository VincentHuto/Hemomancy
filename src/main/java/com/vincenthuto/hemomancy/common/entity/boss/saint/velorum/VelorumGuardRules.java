package com.vincenthuto.hemomancy.common.entity.boss.saint.velorum;

/**
 * The Still Guard (design section 6, user answer 2): outside his openings Velorum takes 30% of every blow, his health
 * cannot be brought under the next floor (60%, then 25%) except by damage dealt inside an opening, and under 10% nothing
 * outside an opening hurts him at all. Inside an opening (or the Endless Hour's hold) blows land whole and pass the
 * floors; the last of his health falls only there. JVM-tested.
 */
public final class VelorumGuardRules {
	public static final float STILL_GUARD = 0.3F;
	/** The phase floors and the last tenth, as fractions of his maximum. */
	public static final float[] FLOORS = {0.60F, 0.25F, 0.10F};
	/** Floors an opening can pass for good: the two phase floors. The last tenth is never passed outside one. */
	public static final int PHASE_FLOORS = 2;
	/** School casts inside the Overheld Invocation. */
	public static final float OVERHELD_CAST = 1.25F;

	private VelorumGuardRules() {
	}

	/** The damage factor a blow takes: whole inside an opening or the Hour, 30% outside; casts x1.25 when overheld. */
	public static float factor(boolean open, boolean hour, boolean overheldCast) {
		if (!open && !hour) return STILL_GUARD;
		return overheldCast ? OVERHELD_CAST : 1.0F;
	}

	/**
	 * The health fraction a blow may not take him under. Inside an opening or the Hour: 0 (the kit keeps 1 health, and
	 * that is the ending). Outside: the next floor not yet passed, or the health itself when he already stands on or
	 * under it, so nothing outside an opening moves him.
	 */
	public static float floor(float health, int passed, boolean openOrHour) {
		if (openOrHour) return 0.0F;
		float next = FLOORS[Math.max(0, Math.min(FLOORS.length - 1, passed))];
		return health > next ? next : health;
	}

	/** Phase floors passed after the health fell to {@code health} (only damage inside an opening can do it). */
	public static int passedBelow(float health, int passed) {
		int result = Math.max(0, passed);
		while (result < PHASE_FLOORS && health < FLOORS[result] - 1.0E-4F) result++;
		return result;
	}

	/** The phase floor (0 or 1) a floor fraction is, or -1 for the last tenth or none. */
	public static int phaseFloor(float fraction) {
		for (int i = 0; i < PHASE_FLOORS; i++) if (Math.abs(fraction - FLOORS[i]) < 1.0E-4F) return i;
		return -1;
	}

	/** The phase the passed floors put him in: 1, 2 or 3. */
	public static int phase(int passed) {
		return 1 + Math.max(0, Math.min(PHASE_FLOORS, passed));
	}

	/** Whether he stands at his last tenth, where only openings hurt him. */
	public static boolean lastTenth(float health) {
		return health <= FLOORS[FLOORS.length - 1] + 1.0E-4F;
	}
}
