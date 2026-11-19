package com.vincenthuto.hemomancy.common.encounter.ordeal;

/**
 * Each professor's block of ground-hazard kinds (2026-11-17), so the client's ordeal pass knows a footprint is an
 * ordeal's and which move drew it. A professor's interventions and their puppet's moves share the professor's block of
 * ten; every kind in {@link #FIRST} to {@link #LAST} is drawn by {@code OrdealTelegraphPass}, from its shape unless the
 * pass gives that kind a look of its own.
 */
public final class OrdealHazardKinds {
	public static final int FIRST = 4001, LAST = 4099;
	/** The Strongman's kinds are {@code OrdealInterventionsDriver.KIND_*}, 4001 to 4008. */
	public static final int STRONGMAN = 4001;
	public static final int ACROBAT = 4010, KNIFE_THROWER = 4020, STILT_WALKER = 4030, BEAST_TAMER = 4040,
			FIRE_EATER = 4050, UNDERSTUDY = 4060;
	/** The Ringmaster's finale (2026-11-18): his block is 4070 to 4089, his rafter moves and the carousel's from 4070. */
	public static final int RINGMASTER = 4070;

	private OrdealHazardKinds() {
	}

	public static boolean isOrdeal(int kind) {
		return kind >= FIRST && kind <= LAST;
	}
}
