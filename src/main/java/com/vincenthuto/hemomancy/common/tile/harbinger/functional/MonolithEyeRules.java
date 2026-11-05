package com.vincenthuto.hemomancy.common.tile.harbinger.functional;

/**
 * How a husk's third eye shows the blood it is fed. The server counts fed ticks and sends its count every fourth tick,
 * so a client that drew the count as it lands would open the slit in steps. Instead the client keeps a shown count that
 * walks toward the server's one tick at a time, and when the gate opens (the server's count going back to 0) holds at
 * full until the opening eye has grown past the tremble, so the opening carries on from where the tremble left it.
 * Pure: no Minecraft types.
 */
public final class MonolithEyeRules {
	/** Fed ticks that open the gate. */
	public static final int CHARGE_TICKS = 40;
	/** The server sends its count every this many fed ticks. */
	private static final int SYNC_EVERY = 4;
	/** How far the slit trembles open at full charge. */
	private static final float TREMBLE = 0.14F;

	private MonolithEyeRules() {
	}

	/** The slit's opening from a charge alone. */
	public static float tremble(float charge) {
		return TREMBLE * charge / CHARGE_TICKS;
	}

	/**
	 * @param shown the count shown last tick
	 * @param synced the server's count as last received
	 * @param gateOpen whether the gate is open
	 * @param eased how far the eye itself has opened, 0 to 1
	 * @return the count to show this tick
	 */
	public static int shownCharge(int shown, int synced, boolean gateOpen, float eased) {
		if (gateOpen) return eased >= tremble(CHARGE_TICKS) ? 0 : Math.min(CHARGE_TICKS, shown + 1);
		// Far behind (a stone met mid-charge), it jumps to a landing's distance and walks from there.
		if (synced > shown) return synced - shown > 2 * SYNC_EVERY ? synced - SYNC_EVERY : shown + 1;
		if (synced < shown) return Math.max(synced, shown - 2);
		return shown;
	}

	/**
	 * The light in the husk's cracks, in ticks of {@link #CHARGE_TICKS}: dark when the eye is shut, climbing with the
	 * shown charge as blood goes in, on to full and held there while the eye stands open, and dying two a tick after.
	 *
	 * @param glow the light last tick
	 * @param shown the charge as shown ({@link #shownCharge})
	 * @param lit whether the eye stands open (the Seed inside, or the gate open)
	 */
	public static int glowTicks(int glow, int shown, boolean lit) {
		int target = lit ? CHARGE_TICKS : shown;
		if (target > glow) return glow + 1;
		if (target < glow) return Math.max(target, glow - 2);
		return glow;
	}
}
