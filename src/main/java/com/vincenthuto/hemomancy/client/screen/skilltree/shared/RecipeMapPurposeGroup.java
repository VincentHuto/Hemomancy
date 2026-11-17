package com.vincenthuto.hemomancy.client.screen.skilltree.shared;

import com.vincenthuto.hemomancy.common.rite.CardinalRitePurpose;
import com.vincenthuto.hemomancy.common.rite.floor.CardinalRiteFloorVisualRules;

/**
 * The arcs of the Harbinger Rites &amp; Crafts map, in the order they run round it: floorless crafts, then each
 * rite purpose, then reference families. A purpose group takes the glow of its floor style unless it has a tint of
 * its own: floorless crafts read as bone and Making as hematic iron, apart from World's red and Bond's flesh.
 */
public enum RecipeMapPurposeGroup {
	FLOORLESS(null, 0xFFC8BC9C),
	SELF(CardinalRitePurpose.SELF, 0),
	WORLD(CardinalRitePurpose.WORLD, 0),
	MAKING(CardinalRitePurpose.MAKING, 0xFF8C9DB8),
	BOND(CardinalRitePurpose.BOND, 0),
	REFERENCE(null, 0xFF8A7A88);

	private final CardinalRitePurpose purpose;
	private final int fixedColor;

	RecipeMapPurposeGroup(CardinalRitePurpose purpose, int fixedColor) {
		this.purpose = purpose;
		this.fixedColor = fixedColor;
	}

	/** The rite purpose this group gathers, or null for the floorless and reference groups. */
	public CardinalRitePurpose purpose() {
		return purpose;
	}

	public int color() {
		if (fixedColor != 0) return fixedColor;
		return CardinalRiteFloorVisualRules.palette(CardinalRiteFloorVisualRules.styleIndex(purpose.floorStyle())).glow();
	}

	public static RecipeMapPurposeGroup of(CardinalRitePurpose purpose) {
		if (purpose == null) return REFERENCE;
		return switch (purpose) {
			case SELF -> SELF;
			case WORLD -> WORLD;
			case MAKING -> MAKING;
			case BOND -> BOND;
		};
	}
}
