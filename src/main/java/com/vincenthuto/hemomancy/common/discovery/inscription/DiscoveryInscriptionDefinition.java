package com.vincenthuto.hemomancy.common.discovery.inscription;

import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.List;

public record DiscoveryInscriptionDefinition(
		ResourceLocation id,
		Kind kind,
		Path path,
		int requiredLevel,
		String title,
		List<String> obscuredText,
		List<String> revealedText,
		List<ResourceLocation> liberEntries,
		@Nullable ResourceLocation riteId) {

	public enum Kind {
		BLOOD_ECHO,
		RITE_FRAGMENT
	}

	public enum Path {
		HARBINGER,
		UNSTAINED
	}

	public boolean isReadable(int level) {
		return level >= requiredLevel;
	}
}
