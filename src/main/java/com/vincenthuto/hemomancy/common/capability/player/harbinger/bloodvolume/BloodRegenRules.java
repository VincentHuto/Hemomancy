package com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume;

/**
 * The body's side of blood (core-loop Track 10, CL-18), the pure numbers. Passive regeneration follows the food bar:
 * a hungry body (under {@link #HUNGRY_BELOW} food) makes half as much, a well-fed one (a full bar and at least
 * {@link #WELL_FED_SATURATION} saturation, as after a steak) half as much again, and a starving one (an empty bar)
 * none. A full night's sleep leaves the body Well Rested for {@link #RESTED_TICKS}, which adds {@link #RESTED_BONUS}
 * to the rate while food lasts; it is a faster recovery, not a refill. Meat eaten gives a one-off amount, the food's
 * nutrition times what the food table says each point is worth. Casting never drains the food bar.
 */
public final class BloodRegenRules {
	/** Below this much food the body is hungry. */
	public static final int HUNGRY_BELOW = 6;
	/** A full food bar. */
	public static final int FULL_FOOD = 20;
	/** Saturation a full bar needs to count as well fed: a hearty meal's, more than a freshly spawned body has. */
	public static final float WELL_FED_SATURATION = 10.0F;
	public static final double HUNGRY = 0.5D;
	public static final double PLAIN = 1.0D;
	public static final double WELL_FED = 1.5D;
	/** What Well Rested adds to the rate, on top of the food bar's multiplier. */
	public static final double RESTED_BONUS = 1.0D;
	/** How long a full night's sleep keeps the body Well Rested: five minutes. */
	public static final int RESTED_TICKS = 6_000;

	/** The state of the body the food bar describes, for teaching surfaces. */
	public enum Body {
		STARVING, HUNGRY, PLAIN, WELL_FED
	}

	private BloodRegenRules() {
	}

	public static Body body(int food, float saturation) {
		if (food <= 0) return Body.STARVING;
		if (food < HUNGRY_BELOW) return Body.HUNGRY;
		if (food >= FULL_FOOD && saturation >= WELL_FED_SATURATION) return Body.WELL_FED;
		return Body.PLAIN;
	}

	/** The multiplier on passive regeneration for this body; zero while starving, whatever else. */
	public static double multiplier(int food, float saturation, boolean rested) {
		double base = switch (body(food, saturation)) {
			case STARVING -> 0.0D;
			case HUNGRY -> HUNGRY;
			case PLAIN -> PLAIN;
			case WELL_FED -> WELL_FED;
		};
		return base <= 0.0D ? 0.0D : base + (rested ? RESTED_BONUS : 0.0D);
	}

	/** The blood a meal gives: its nutrition times what one point of it is worth; never negative. */
	public static double mealBlood(int nutrition, double mlPerNutrition) {
		if (nutrition <= 0 || !(mlPerNutrition > 0.0D)) return 0.0D;
		return nutrition * mlPerNutrition;
	}
}
