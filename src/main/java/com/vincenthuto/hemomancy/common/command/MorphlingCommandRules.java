package com.vincenthuto.hemomancy.common.command;

public final class MorphlingCommandRules {
	public enum Source {
		EQUIPPED,
		MAIN_HAND,
		OFF_HAND,
		NONE
	}

	private MorphlingCommandRules() {
	}

	public static Source chooseSource(boolean equippedMorphling, boolean mainHandMorphling,
			boolean offhandMorphling) {
		if (equippedMorphling) return Source.EQUIPPED;
		if (mainHandMorphling) return Source.MAIN_HAND;
		if (offhandMorphling) return Source.OFF_HAND;
		return Source.NONE;
	}
}
