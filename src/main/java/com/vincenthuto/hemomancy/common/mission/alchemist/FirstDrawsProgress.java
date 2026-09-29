package com.vincenthuto.hemomancy.common.mission.alchemist;

import net.minecraft.resources.ResourceLocation;

import java.util.HashSet;
import java.util.Set;

public record FirstDrawsProgress(int samples, Set<ResourceLocation> species) {
	private static final Set<ResourceLocation> COMMON_ANIMALS = Set.of(
			ResourceLocation.parse("minecraft:cow"), ResourceLocation.parse("minecraft:pig"),
			ResourceLocation.parse("minecraft:sheep"), ResourceLocation.parse("minecraft:chicken"),
			ResourceLocation.parse("minecraft:rabbit"), ResourceLocation.parse("minecraft:goat"));
	public static final FirstDrawsProgress EMPTY = new FirstDrawsProgress(0, Set.of());

	public FirstDrawsProgress {
		samples = Math.max(0, samples);
		species = Set.copyOf(species);
	}

	public FirstDrawsProgress record(ResourceLocation source) {
		if (!COMMON_ANIMALS.contains(source)) return this;
		var seen = new HashSet<>(species);
		seen.add(source);
		return new FirstDrawsProgress(samples + 1, seen);
	}

	public boolean complete() {
		return samples >= 5 && species.size() >= 3;
	}
}
