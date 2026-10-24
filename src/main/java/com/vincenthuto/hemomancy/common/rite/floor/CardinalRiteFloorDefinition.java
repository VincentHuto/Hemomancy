package com.vincenthuto.hemomancy.common.rite.floor;

import com.vincenthuto.hemomancy.common.recipe.CardinalRiteType;
import com.vincenthuto.hemomancy.common.rite.CardinalRitePurpose;
import com.vincenthuto.hutoslib.math.MultiblockPattern;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
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
		float footprintRadius,
		CardinalRitePurpose purpose) {
	public CardinalRiteFloorDefinition {
		Objects.requireNonNull(id, "id");
		Objects.requireNonNull(style, "style");
		Objects.requireNonNull(tier, "tier");
		Objects.requireNonNull(pattern, "pattern");
		Objects.requireNonNull(focus, "focus");
		Objects.requireNonNull(purpose, "purpose");
		brazierSockets = List.copyOf(brazierSockets);
		if (footprintRadius <= 0.0F) throw new IllegalArgumentException("Floor footprint must be positive");
	}

	/** A floor whose style is not one of the four purpose styles: it declares its purpose and hosts special rites. */
	public boolean special() {
		return CardinalRitePurpose.ofFloorStyle(style) == null;
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

	public MutableComponent displayName() {
		return displayName(style, tier);
	}

	/** "Working Floor (Greater)". */
	public static MutableComponent displayName(String style, CardinalRiteType tier) {
		return Component.translatable("cardinal_rite_floor.hemomancy.display_name", styleName(style), tierName(tier));
	}

	/** "Working"; a datapack style without a key shows its id capitalised. */
	public static MutableComponent styleName(String style) {
		if (style.isEmpty()) return Component.translatable("cardinal_rite_floor.hemomancy.style.unnamed");
		return Component.translatableWithFallback("cardinal_rite_floor.hemomancy.style." + style,
				Character.toUpperCase(style.charAt(0)) + style.substring(1));
	}

	/** "Greater". */
	public static MutableComponent tierName(CardinalRiteType tier) {
		return Component.translatable("cardinal_rite_floor.hemomancy.tier." + tier.getSerializedName());
	}

	/** "Greater (7x7)". */
	public static MutableComponent tierWithSize(CardinalRiteType tier) {
		return Component.translatable("cardinal_rite_floor.hemomancy.tier_size", tierName(tier), tier.getSize(), tier.getSize());
	}

	/** "Working Greater". */
	public MutableComponent styleAndTier() {
		return Component.translatable("cardinal_rite_floor.hemomancy.style_tier", styleName(style), tierName(tier));
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
