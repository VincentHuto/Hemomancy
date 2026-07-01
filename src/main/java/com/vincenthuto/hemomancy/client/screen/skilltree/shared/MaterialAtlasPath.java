package com.vincenthuto.hemomancy.client.screen.skilltree.shared;

public enum MaterialAtlasPath {
	HARBINGER("Harbinger", "Blood Atlas", 0xFFCC6644),
	UNSTAINED("Unstained", "Still Atlas", 0xFF80B0A0);

	private final String displayName;
	private final String hubLabel;
	private final int accentColor;

	MaterialAtlasPath(String displayName, String hubLabel, int accentColor) {
		this.displayName = displayName;
		this.hubLabel = hubLabel;
		this.accentColor = accentColor;
	}

	public String displayName() {
		return displayName;
	}

	public String hubLabel() {
		return hubLabel;
	}

	public int accentColor() {
		return accentColor;
	}
}
