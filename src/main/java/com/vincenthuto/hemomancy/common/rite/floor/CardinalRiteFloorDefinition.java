package com.vincenthuto.hemomancy.common.rite.floor;

import com.vincenthuto.hemomancy.common.recipe.CardinalRiteType;
import com.vincenthuto.hemomancy.common.rite.CardinalRitePurpose;
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

	/** Blocks from the Focus to the floor's edge. */
	public int half() {
		return Math.max(focus.getX(), focus.getZ());
	}

	/**
	 * Every cell (socket frame) a brazier counts from: the N×N×N space above the floor
	 * ({@link CardinalRiteBrazierSpace}). {@link #brazierSockets()} are the gash spots within it.
	 */
	public List<BlockPos> brazierSpace() {
		return CardinalRiteBrazierSpace.cells(half());
	}

	public CardinalRiteFloorRequirement requirement() {
		return new CardinalRiteFloorRequirement(style, tier);
	}

	public String displayName() {
		return displayName(style, tier);
	}

	/** "Working Floor (Greater)". */
	public static String displayName(String style, CardinalRiteType tier) {
		String styleName = style.isEmpty() ? "Rite" : Character.toUpperCase(style.charAt(0)) + style.substring(1);
		String tierName = tier.getSerializedName();
		return styleName + " Floor (" + Character.toUpperCase(tierName.charAt(0)) + tierName.substring(1) + ")";
	}

	/** Lists floors in purpose order (Threshold, Dominion, Working, Communion), smallest tier first; other styles last. */
	public static int listOrder(String style, CardinalRiteType tier) {
		int purpose = CardinalRitePurpose.values().length;
		for (CardinalRitePurpose candidate : CardinalRitePurpose.values()) {
			if (candidate.floorStyle().equals(style)) purpose = candidate.ordinal();
		}
		return purpose * CardinalRiteType.values().length + tier.ordinal();
	}
}
