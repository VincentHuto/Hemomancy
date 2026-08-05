package com.vincenthuto.hemomancy.common.block.harbinger;

public final class BrazierBloodInteractionRules {
	private BrazierBloodInteractionRules() {
	}

	public static boolean shouldExtinguishOnAbsorption(boolean lit, boolean empty, double maxAmount) {
		return lit && empty && maxAmount > 0.0D;
	}

	public static boolean shouldDrawAbsorptionParticles(boolean lit) {
		return lit;
	}
}
