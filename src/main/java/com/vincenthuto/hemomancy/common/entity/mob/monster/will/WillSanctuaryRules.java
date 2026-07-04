package com.vincenthuto.hemomancy.common.entity.mob.monster.will;

public final class WillSanctuaryRules {
	private WillSanctuaryRules() {
	}

	public static boolean isSanctuary(boolean insideFane, boolean inChamberDimension, boolean insideOutpostPiece) {
		return insideFane || inChamberDimension || insideOutpostPiece;
	}
}
