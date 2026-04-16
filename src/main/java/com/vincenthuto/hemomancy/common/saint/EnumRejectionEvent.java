package com.vincenthuto.hemomancy.common.saint;

public enum EnumRejectionEvent {

	HEMATIC_REVERSAL("Hematic Reversal"),
	MEMORY_SCRAMBLE("Memory Scramble"),
	MANIFESTATION("Manifestation"),
	MARKED_BY_CANON("Marked by Canon");

	private final String displayName;

	EnumRejectionEvent(String displayName) {
		this.displayName = displayName;
	}

	public String getDisplayName() {
		return displayName;
	}
}
