package com.vincenthuto.hemomancy.common.saint;

public enum EnumDoctrineTag {

	MARTYRDOM("Martyrdom"),
	ABSOLUTION("Absolution"),
	WITNESS("Witness"),
	SILENCE("Silence"),
	JUDGMENT("Judgment");

	private final String displayName;

	EnumDoctrineTag(String displayName) {
		this.displayName = displayName;
	}

	public String getDisplayName() {
		return displayName;
	}
}
