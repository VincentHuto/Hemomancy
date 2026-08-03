package com.vincenthuto.hemomancy.common.item.shared;

public final class MnemonicBlueprintRules {
	private MnemonicBlueprintRules() {
	}

	public static boolean isEligible(MnemonicBlueprintTarget.Type type, boolean unlocked) {
		return type != null && unlocked;
	}

	public static boolean shouldShowCue(MnemonicBlueprintTarget.Type type, boolean unlocked, boolean hasBlank) {
		return hasBlank && isEligible(type, unlocked);
	}
}
