package com.vincenthuto.hemomancy.common.circus;

public final class CircusCarouselEncounterRules {
	private static final int ALL_THREE = 0b111;

	private CircusCarouselEncounterRules() {
	}

	public static int sever(int mask, int rider) {
		return valid(rider) ? mask | 1 << rider : mask;
	}

	public static boolean canBreakAnchor(int severedMask, int anchor) {
		return valid(anchor) && (severedMask & 1 << anchor) != 0;
	}

	public static int breakAnchor(int brokenMask, int severedMask, int anchor) {
		return canBreakAnchor(severedMask, anchor) ? brokenMask | 1 << anchor : brokenMask;
	}

	public static boolean allAnchorsBroken(int mask) {
		return (mask & ALL_THREE) == ALL_THREE;
	}

	private static boolean valid(int index) {
		return index >= 0 && index < 3;
	}
}
