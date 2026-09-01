package com.vincenthuto.hemomancy.common.tile.harbinger.puzzle;

/**
 * Local corpus state enum used by SaintSarcophagusBlockEntity.
 * Kept in the same package as the block entity to avoid cross-package classloading issues.
 */
public enum EnumCorpusState {
	DORMANT("Dormant"),
	RESPONSIVE("Responsive"),
	AWAKENED("Awakened");

	private final String displayName;

	EnumCorpusState(String displayName) {
		this.displayName = displayName;
	}

	public String getDisplayName() {
		return displayName;
	}
}

