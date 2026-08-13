package com.vincenthuto.hemomancy.common.capability.player.shared.skill;

public final class ToggleablePlayerPowerRules {
	private ToggleablePlayerPowerRules() {}

	public static boolean bloodhoundCanSense(boolean enabled, float health, float maxHealth) {
		return enabled && maxHealth > 0.0F && health < maxHealth * 0.75F;
	}

	public static boolean leaveCrimsonWake(boolean enabled, boolean sprinting, float health, float maxHealth) {
		return enabled && sprinting && maxHealth > 0.0F && health < maxHealth * 0.5F;
	}

	public static boolean summonShouldSpare(boolean enabled, boolean explicitlyFocused,
			float health, float maxHealth) {
		return enabled && !explicitlyFocused && maxHealth > 0.0F
				&& health <= Math.max(1.0F, maxHealth * 0.1F);
	}
}
