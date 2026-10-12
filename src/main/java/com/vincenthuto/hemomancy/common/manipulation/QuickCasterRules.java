package com.vincenthuto.hemomancy.common.manipulation;

/** Quick Caster: selecting a QUICK manipulation from the radial or cycle key casts it as though R were pressed. */
public final class QuickCasterRules {
	private QuickCasterRules() {
	}

	/**
	 * Staff weapon forms are excluded because selection already applies the form; conjurations because their cast
	 * dispels the held conjured item. A cooling manipulation is only selected, without the cooldown message.
	 */
	public static boolean shouldCastOnSelect(boolean skillEnabled, EnumManipulationType type, boolean staffWeaponForm,
			boolean conjuration, boolean onCooldown) {
		return skillEnabled && type == EnumManipulationType.QUICK && !staffWeaponForm && !conjuration && !onCooldown;
	}
}
