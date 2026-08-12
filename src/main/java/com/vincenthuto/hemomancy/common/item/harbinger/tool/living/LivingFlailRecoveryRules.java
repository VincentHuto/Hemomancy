package com.vincenthuto.hemomancy.common.item.harbinger.tool.living;

public final class LivingFlailRecoveryRules {
	private LivingFlailRecoveryRules() {
	}

	public static boolean shouldRecover(boolean deployed, boolean projectilePresent, boolean sameDimension,
			boolean stackBeingRestored) {
		return deployed && (stackBeingRestored || !projectilePresent || !sameDimension);
	}
}
