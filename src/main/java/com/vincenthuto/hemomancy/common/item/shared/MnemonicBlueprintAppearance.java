package com.vincenthuto.hemomancy.common.item.shared;

import javax.annotation.Nullable;

/** Stable custom-model-data values used by the mnemonic blueprint item model. */
public final class MnemonicBlueprintAppearance {
	public static final int BLANK = 0;
	public static final int CARDINAL_RITE = 1;
	public static final int BLOOD_STRUCTURE = 2;

	private MnemonicBlueprintAppearance() {
	}

	public static int customModelData(@Nullable MnemonicBlueprintTarget.Type type) {
		if (type == null) return BLANK;
		return switch (type) {
			case CARDINAL_RITE -> CARDINAL_RITE;
			case BLOOD_STRUCTURE -> BLOOD_STRUCTURE;
		};
	}
}
