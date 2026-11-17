package com.vincenthuto.hemomancy.common.manipulation.flammeus;

/** Pyretic Forge's Sculpting numbers, pure. */
public final class PyreticForgeRules {
	/** Blood one pulse costs before the cost ledger: 40 mL a second at the default cadence. */
	public static final double BLOOD_PER_PULSE = 20.0D;
	/** Items smelted per pulse by a bare hand without Crimson Mastery: eight in two seconds, like the old cast. */
	public static final double ITEMS_PER_PULSE = 2.0D;

	private PyreticForgeRules() {
	}

	/** How many items one pulse smelts: two by hand, more with the Staff's work and Crimson Mastery; never none. */
	public static int itemsPerPulse(double workScale, double crimsonMastery) {
		return Math.max(1, (int) Math.ceil(ITEMS_PER_PULSE * Math.max(0.0D, workScale) * Math.max(1.0D, crimsonMastery) - 1.0E-6D));
	}
}
