package com.vincenthuto.hemomancy.common.rite;

public final class ScarBrazierInteractionRules {
	public enum Burn {
		NONE,
		LEARN,
		COMMIT,
		CLEAR
	}

	private ScarBrazierInteractionRules() {
	}

	public static Burn selectOffering(boolean lit, boolean scarItem, boolean preparedPattern,
			boolean blankMotifPaper) {
		if (!lit) {
			return Burn.NONE;
		}
		if (scarItem) {
			return Burn.LEARN;
		}
		if (preparedPattern) {
			return Burn.COMMIT;
		}
		return blankMotifPaper ? Burn.CLEAR : Burn.NONE;
	}

	public static boolean canAbsorb(boolean channelingAbsorption, double maxAmount) {
		return channelingAbsorption && maxAmount > 0.0D;
	}

	public static int maxActiveScars(int degree) {
		if (degree >= 6) {
			return 4;
		}
		if (degree >= 5) {
			return 2;
		}
		return degree >= ScarBrazierRite.REQUIRED_DEGREE ? 1 : 0;
	}
}
