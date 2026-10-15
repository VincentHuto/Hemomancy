package com.vincenthuto.hemomancy.common.rite;

import com.vincenthuto.hemomancy.common.recipe.CardinalRiteType;

/**
 * Shape rules for a Working craft: a making rite on a Working floor whose built
 * subject fits the floor tier's bed and whose blood is paid by feeding it.
 */
public final class WorkingCraftRecipeRules {
	/** Like station upgrades, crafts may carry up to six offerings whatever their degree. */
	public static final int MAX_OFFERINGS = 6;

	private WorkingCraftRecipeRules() {
	}

	/** @return why the craft is malformed, or null when it is valid */
	public static String violation(CardinalRitePurpose purpose, String floorPath, String[][] subjectPattern,
			int feedMl, int anchorCount, int bloodCost) {
		if (purpose != CardinalRitePurpose.MAKING) return "must be a making rite";
		if (!CardinalRitePurpose.MAKING.floorStyle().equals(CardinalRitePurpose.floorStyleOf(floorPath))) {
			return "must use a Working floor, not " + floorPath;
		}
		CardinalRiteType tier;
		try {
			tier = CardinalRiteType.byName(floorPath.substring(floorPath.lastIndexOf('_') + 1));
		} catch (IllegalArgumentException e) {
			return "names a floor with no tier: " + floorPath;
		}
		WorkingCraftBed.Footprint footprint = WorkingCraftBed.Footprint.of(subjectPattern);
		if (footprint.width() == 0) return "has an empty subject";
		if (!WorkingCraftBed.forTier(tier).fits(footprint)) {
			return "subject " + footprint.width() + "x" + footprint.depth() + "x" + footprint.height()
					+ " does not fit the " + tier.getSerializedName() + " Working bed";
		}
		if (feedMl <= 0) return "must feed its subject a positive amount of blood";
		if (bloodCost != 0) return "pays its blood through the subject feed; bloodCost must be 0";
		if (anchorCount != 0) return "takes no anchors";
		return null;
	}
}
