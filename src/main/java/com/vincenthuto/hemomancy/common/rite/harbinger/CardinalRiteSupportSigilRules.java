package com.vincenthuto.hemomancy.common.rite.harbinger;

/**
 * Shared meanings for support sigils which can occupy authored sockets.
 */
final class CardinalRiteSupportSigilRules {
	static final int CAGE_EFFECT_TICKS = 40;
	static final int CAGE_SLOWNESS_AMPLIFIER = 6;

	private CardinalRiteSupportSigilRules() {
	}

	static boolean revealsFalseOmens(boolean mnemonicComplete, boolean lensComplete) {
		return mnemonicComplete || lensComplete;
	}

	static boolean bindsThreats(boolean cageComplete) {
		return cageComplete;
	}
}
