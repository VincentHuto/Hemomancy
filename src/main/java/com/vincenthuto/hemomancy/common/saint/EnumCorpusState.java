package com.vincenthuto.hemomancy.common.saint;

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
