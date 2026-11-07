package com.vincenthuto.hemomancy.common.entity.boss.saint.hemorath;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodAccounting.Source;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodFlowContribution.Category;

/**
 * Every way blood leaves a player in Hemorath's fight, one named source each (resolution D-5). None counts as a spend
 * and none reaches past Sanguine Reserve's floor; each label is {@code blood_source.hemomancy.<id>}.
 */
public final class HemorathSources {
	/** The drain round his body. */
	public static final Source PRESENCE = Source.of("hemorath_presence", Category.HAZARD, false, false);
	/** Raking Claw's bite. */
	public static final Source CLAW = Source.of("hemorath_claw", Category.HAZARD, false, false);
	/** Marrow Siphon's draw. */
	public static final Source SIPHON = Source.of("hemorath_siphon", Category.HAZARD, false, false);
	/** The tide standing on the boss floor. */
	public static final Source TIDE = Source.of("hemorath_tide", Category.HAZARD, false, false);
	/** The Last Thirst's running gutters. */
	public static final Source THIRST = Source.of("hemorath_thirst", Category.HAZARD, false, false);
	/** The Gorge's drained burst on contact. */
	public static final Source GORGE = Source.of("hemorath_gorge", Category.HAZARD, false, false);
	/** Held Projection into his open ribs (Gorged, Strained, the Last Thirst). */
	public static final Source VESSEL_OFFERING = Source.of("hemorath_vessel_offering", Category.HAZARD, false, false);
	/** Held Projection along the Tithe's chain. */
	public static final Source TITHE_PAYMENT = Source.of("hemorath_tithe_payment", Category.HAZARD, false, false);
	/** Held Projection into one of his wells. */
	public static final Source WELL_OFFERING = Source.of("hemorath_well_offering", Category.HAZARD, false, false);

	private HemorathSources() {
	}
}
