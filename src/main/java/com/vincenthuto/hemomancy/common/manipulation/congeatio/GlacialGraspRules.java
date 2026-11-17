package com.vincenthuto.hemomancy.common.manipulation.congeatio;

/**
 * Glacial Grasp under held Projection, as plain numbers: a hand of ice forms round one creature, holds it, and closes
 * on the attack button. The squeeze is worth more the longer the hand has held.
 */
public final class GlacialGraspRules {
	/** Ticks the hand takes to form round a creature before it holds. */
	public static final int FORM_TICKS = 20;
	/** Ticks of holding, past forming, for the hardest squeeze. */
	public static final int FULL_GRIP_TICKS = 60;
	/** Blood a pulse costs before the cost ledger: 40 mL a second. */
	public static final double BLOOD_PER_PULSE = 20.0D;
	/** How long each pulse's Rime level lasts, and the hold between pulses. */
	public static final int RIME_TICKS = 120, HOLD_TICKS = 16;
	/** The squeeze's damage from a hand just formed and from one held full, before Crimson Mastery. */
	public static final float MIN_SQUEEZE = 4.0F, MAX_SQUEEZE = 14.0F;
	/** Rime levels the squeeze drives in. */
	public static final int SQUEEZE_RIME = 2;
	/** The slow a boss is under while the hand is on it (Slowness II); nothing less than a boss is simply held. */
	public static final int BOSS_SLOW_AMPLIFIER = 1;
	/** How far past the caster's reach the gripped creature may be carried before the hand lets go. */
	public static final double LEASH = 1.6D;
	/** Ticks after a squeeze before a new hand may form. */
	public static final int SPENT_TICKS = 20;
	/** How fast the caster walks while gripping, as a share of their pace. */
	public static final double PACE = 0.6D;

	private GlacialGraspRules() {
	}

	/** Whether the hand has formed and holds, after {@code heldTicks} on the creature. */
	public static boolean holds(int heldTicks) {
		return heldTicks >= FORM_TICKS;
	}

	/** How closed the forming hand is, 0 to 1. */
	public static double formed(double heldTicks) {
		return Math.max(0.0D, Math.min(1.0D, heldTicks / FORM_TICKS));
	}

	/** How much of the full grip has been held, 0 to 1; nothing before the hand has formed. */
	public static double grip(int heldTicks) {
		return Math.max(0.0D, Math.min(1.0D, (heldTicks - FORM_TICKS) / (double) FULL_GRIP_TICKS));
	}

	/** The squeeze's damage; a hand that has not formed cannot squeeze. */
	public static float squeeze(int heldTicks) {
		if (!holds(heldTicks)) return 0.0F;
		return (float) (MIN_SQUEEZE + (MAX_SQUEEZE - MIN_SQUEEZE) * grip(heldTicks));
	}
}
