package com.vincenthuto.hemomancy.common.item.shared;

/** Stack limits for blank and component-bearing mnemonic blueprints. */
public final class MnemonicBlueprintStacking {
	private MnemonicBlueprintStacking() {}

	public static int maxStackSize(boolean imprinted) {
		return imprinted ? 1 : 64;
	}
}
