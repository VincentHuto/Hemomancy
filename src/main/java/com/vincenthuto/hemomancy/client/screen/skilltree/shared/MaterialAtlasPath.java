package com.vincenthuto.hemomancy.client.screen.skilltree.shared;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public enum MaterialAtlasPath {
	HARBINGER("screen.hemomancy.material_atlas.harbinger", "screen.hemomancy.material_atlas.harbinger.hub", 0xFFCC6644),
	UNSTAINED("screen.hemomancy.material_atlas.unstained", "screen.hemomancy.material_atlas.unstained.hub", 0xFF80B0A0);

	private final String displayNameKey;
	private final String hubLabelKey;
	private final int accentColor;

	MaterialAtlasPath(String displayNameKey, String hubLabelKey, int accentColor) {
		this.displayNameKey = displayNameKey;
		this.hubLabelKey = hubLabelKey;
		this.accentColor = accentColor;
	}

	public MutableComponent displayName() {
		return Component.translatable(displayNameKey);
	}

	public MutableComponent hubLabel() {
		return Component.translatable(hubLabelKey);
	}

	public int accentColor() {
		return accentColor;
	}
}
