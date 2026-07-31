package com.vincenthuto.hemomancy.common.rite.floor;

import com.vincenthuto.hemomancy.common.recipe.CardinalRiteType;
import com.vincenthuto.hutoslib.math.MultiblockPattern;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Objects;

public record CardinalRiteFloorDefinition(
		ResourceLocation id,
		String style,
		CardinalRiteType tier,
		MultiblockPattern pattern,
		BlockPos focus,
		List<BlockPos> brazierSockets,
		float footprintRadius) {
	public CardinalRiteFloorDefinition {
		Objects.requireNonNull(id, "id");
		Objects.requireNonNull(style, "style");
		Objects.requireNonNull(tier, "tier");
		Objects.requireNonNull(pattern, "pattern");
		Objects.requireNonNull(focus, "focus");
		brazierSockets = List.copyOf(brazierSockets);
		if (footprintRadius <= 0.0F) throw new IllegalArgumentException("Floor footprint must be positive");
	}

	public CardinalRiteFloorRequirement requirement() {
		return new CardinalRiteFloorRequirement(style, tier);
	}
}
