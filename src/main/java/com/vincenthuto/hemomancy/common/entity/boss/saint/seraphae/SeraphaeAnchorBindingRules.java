package com.vincenthuto.hemomancy.common.entity.boss.saint.seraphae;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodAccounting;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodFlowContribution.Category;

/**
 * Held Blood Projection into her anchors (design section 9, resolutions B-4 and D-5): a slipped anchor re-binds after
 * 240 blood, a dormant reserve wakes after 320, each at 4 blood a tick or the item's own rate if lower. A short grace
 * keeps the charge between ticks of a held channel; after it the charge bleeds off at half the feeding speed. Every
 * drain has its own named source. Pure apart from the source constants, so it is JVM-tested.
 */
public final class SeraphaeAnchorBindingRules {
	/** The most blood an anchor takes a tick. */
	public static final double RATE = 4.0D;
	public static final double REBIND_BLOOD = 240.0D;
	public static final double RESERVE_BLOOD = 320.0D;
	/** Ticks a charge holds after the last fed tick before it starts to bleed. */
	public static final int GRACE = 4;
	/** A held anchor at a Chain Wrench's commit counts as held by projection if it was fed within this many ticks. */
	public static final int HELD_AT_COMMIT = 3;
	/** How wide a cone round the player's look counts as aiming at an anchor, in degrees. */
	public static final double AIM_CONE = 24.0D;

	/** Re-binding a slipped binding anchor (or a torn reserve). */
	public static final BloodAccounting.Source SOURCE = BloodAccounting.Source.of("seraphae_anchor_binding",
			Category.HAZARD, true, false);
	/** Waking a dormant reserve anchor in the aisles. */
	public static final BloodAccounting.Source RESERVE_SOURCE = BloodAccounting.Source.of("seraphae_reserve_wake",
			Category.HAZARD, true, false);
	/** Feeding a held anchor while she condenses. */
	public static final BloodAccounting.Source CONDENSING_SOURCE = BloodAccounting.Source.of("seraphae_condensing_feed",
			Category.HAZARD, true, false);

	private SeraphaeAnchorBindingRules() {
	}

	/** The blood one tick takes: the anchor's rate or the item's, whichever is lower. */
	public static double take(double itemRate) {
		return Math.max(0.0D, Math.min(RATE, itemRate));
	}

	/** Progress 0..1 after {@code blood} more is fed toward a work needing {@code needed}. */
	public static float feed(float progress, double blood, double needed) {
		return (float) Math.min(1.0D, progress + Math.max(0.0D, blood) / needed);
	}

	/**
	 * Progress after one unfed tick: held through the grace, then bleeding at half the feeding speed (half of
	 * {@link #RATE} a tick, as a fraction of {@code needed}).
	 */
	public static float bleed(float progress, int ticksSinceFed, double needed) {
		if (ticksSinceFed <= GRACE) return progress;
		return (float) Math.max(0.0D, progress - RATE * 0.5D / needed);
	}

	public static boolean complete(float progress) {
		return progress >= 1.0F - 1.0E-6F;
	}

	/** Whether an anchor fed {@code ticksSinceFed} ago counts as held by projection at a Chain Wrench's commit. */
	public static boolean heldAtCommit(int ticksSinceFed) {
		return ticksSinceFed >= 0 && ticksSinceFed <= HELD_AT_COMMIT;
	}
}
