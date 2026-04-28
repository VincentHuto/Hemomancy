package com.vincenthuto.hemomancy.common.capability.player.knowledge.discovery;

import net.minecraft.resources.ResourceLocation;

public record MemoDefinition(ResourceLocation id, ResourceLocation liberEntry, MemoPath path) {
	public enum MemoPath {
		HARBINGER,
		UNSTAINED,
		SHARED
	}
}
