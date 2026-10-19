package com.vincenthuto.hemomancy.client.screen.skilltree.shared;

import com.vincenthuto.hemomancy.common.rite.CardinalRitePurpose;
import com.vincenthuto.hemomancy.common.rite.floor.CardinalRiteFloorVisualRules;

/**
 * The arcs of the Harbinger Rites &amp; Crafts map, in the order they run round it: floorless crafts, then each
 * rite purpose, then reference families. A purpose group takes the glow of its floor style.
 */
public enum RecipeMapPurposeGroup {
	FLOORLESS("Floorless", null, 0xFFC0303A),
	SELF("Self", CardinalRitePurpose.SELF, 0),
	WORLD("World", CardinalRitePurpose.WORLD, 0),
	MAKING("Making", CardinalRitePurpose.MAKING, 0),
	BOND("Bond", CardinalRitePurpose.BOND, 0),
	REFERENCE("Reference", null, 0xFF8A7A88);

	private final String label;
	private final CardinalRitePurpose purpose;
	private final int fixedColor;

	RecipeMapPurposeGroup(String label, CardinalRitePurpose purpose, int fixedColor) {
		this.label = label;
		this.purpose = purpose;
		this.fixedColor = fixedColor;
	}

	public String label() {
		return label;
	}

	/** The rite purpose this group gathers, or null for the floorless and reference groups. */
	public CardinalRitePurpose purpose() {
		return purpose;
	}

	public int color() {
		if (purpose == null) return fixedColor;
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
