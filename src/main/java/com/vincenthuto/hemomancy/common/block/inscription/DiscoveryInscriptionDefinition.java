package com.vincenthuto.hemomancy.common.block.inscription;

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
		List<ResourceLocation> advancements,
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

	/** {@code discovery_inscription.<namespace>.<path>.title}; the authored title is the fallback. */
	public net.minecraft.network.chat.MutableComponent displayTitle() {
		return net.minecraft.network.chat.Component.translatableWithFallback(keyPrefix() + ".title", title);
	}

	/** One component per obscured line: {@code <prefix>.obscured.<n>}, the authored line as the fallback. */
	public List<net.minecraft.network.chat.Component> displayObscuredText() {
		return displayLines(obscuredText, "obscured");
	}

	/** One component per revealed line: {@code <prefix>.revealed.<n>}, the authored line as the fallback. */
	public List<net.minecraft.network.chat.Component> displayRevealedText() {
		return displayLines(revealedText, "revealed");
	}

	private List<net.minecraft.network.chat.Component> displayLines(List<String> lines, String family) {
		List<net.minecraft.network.chat.Component> shown = new java.util.ArrayList<>(lines.size());
		for (int i = 0; i < lines.size(); i++) {
			String line = lines.get(i);
			shown.add(line == null || line.isBlank() ? net.minecraft.network.chat.Component.empty()
					: net.minecraft.network.chat.Component.translatableWithFallback(keyPrefix() + "." + family + "." + i, line));
		}
		return shown;
	}

	private String keyPrefix() {
		return "discovery_inscription." + id.getNamespace() + "." + id.getPath().replace('/', '.');
	}
}
