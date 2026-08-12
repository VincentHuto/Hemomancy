package com.vincenthuto.hemomancy.common.entity.projectile;

public final class LivingFlailImpactRules {
	private LivingFlailImpactRules() {
	}

	public static boolean mayImpact(boolean alreadyImpacted) {
		return !alreadyImpacted;
	}

	public static boolean isValidTarget(boolean owner, boolean allied, boolean alive, boolean mayAttack) {
		return !owner && !allied && alive && mayAttack;
	}

	public static float timeoutImpactScale(boolean timeout) {
		return timeout ? 0.65F : 1.0F;
	}
}
