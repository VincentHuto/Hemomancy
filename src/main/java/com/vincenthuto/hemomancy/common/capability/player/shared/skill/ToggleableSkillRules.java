package com.vincenthuto.hemomancy.common.capability.player.shared.skill;

public final class ToggleableSkillRules {
	private ToggleableSkillRules() {}

	public static double allowedBloodDrain(boolean reserveEnabled, double currentBlood,
			double reserveFloor, double requested) {
		if (!reserveEnabled) return Math.max(0.0D, requested);
		return Math.max(0.0D, Math.min(requested, currentBlood - Math.max(0.0D, reserveFloor)));
	}
}
