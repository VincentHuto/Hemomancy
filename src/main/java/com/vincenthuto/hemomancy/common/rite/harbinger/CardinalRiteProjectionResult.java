package com.vincenthuto.hemomancy.common.rite.harbinger;

/**
 * Separates whether a rite claimed a projection target from how much blood the
 * target happened to accept.
 */
public record CardinalRiteProjectionResult(boolean handled, double bloodSpent) {
	public static CardinalRiteProjectionResult handled(double bloodSpent) {
		return new CardinalRiteProjectionResult(true, Math.max(0.0D, bloodSpent));
	}

	public static CardinalRiteProjectionResult unhandled() {
		return new CardinalRiteProjectionResult(false, 0.0D);
	}

	public boolean allowsOrdinaryProjection() {
		return !handled;
	}
}
