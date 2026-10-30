package com.vincenthuto.hemomancy.common.capability.player.harbinger.possession;

/**
 * When a Blood Binding on a surrogate holds without end: the mob stands on a formed Qliphoth grand floor, inside its
 * footprint, and the floor's hold lasts until the floor stops forming or a rite run on it has ended. Pure, so the
 * binding and the tests agree.
 */
public final class SurrogateTetherRules {
	/** Blocks above the floor a bound body may stand and still count as on it (a tall surrogate, a step). */
	public static final int HEIGHT = 3;
	/** Ticks between the floor checks of a held binding. */
	public static final int CHECK_TICKS = 20;
	/** Ticks the cast's tendril visual plays for a binding without end; the effect's own serpents show it after. */
	public static final int VISUAL_TICKS = 200;

	public enum Hold { KEEP, RELEASE }

	private SurrogateTetherRules() {
	}

	/** Whether a body's feet, {@code dx, dy, dz} blocks from the Focus, stand on a floor {@code halfWidth} wide. */
	public static boolean onFloor(int dx, int dy, int dz, int halfWidth) {
		return Math.abs(dx) <= halfWidth && Math.abs(dz) <= halfWidth && dy >= 0 && dy <= HEIGHT;
	}

	/**
	 * Whether a held binding keeps holding: the floor must still form, and once a rite has run on it, the rite must
	 * still be running.
	 */
	public static Hold hold(boolean floorFormed, boolean ritePassed, boolean riteRunning) {
		if (!floorFormed) return Hold.RELEASE;
		if (ritePassed && !riteRunning) return Hold.RELEASE;
		return Hold.KEEP;
	}
}
