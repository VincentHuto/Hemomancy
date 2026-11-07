package com.vincenthuto.hemomancy.common.entity.boss.saint.hemorath;

/**
 * The iron in his bone (design section 7, "Patches"): one spike patch erupts per 1000 points leaked, up to nine, in a
 * fixed order of sites; each takes 4% off the damage he takes, and the count shapes the Iron Rake, the Spiked Halo and
 * the Iron Thicket. JVM-tested.
 */
public final class HemorathIronRules {
	public static final float LEAK_PER_PATCH = 1000.0F;
	public static final int MAX_PATCHES = 9;
	public static final float REDUCTION_PER_PATCH = 0.04F;
	/** The eruption order of the nine sites, as the model's {@code spike_patch_<i>} parts are named. */
	public static final String[] SITES = {"right_forearm", "right_arm", "left_rib_2", "right_rib_5", "left_forearm",
			"left_arm", "right_shin", "left_shin", "head"};
	/** The Iron Rake: lanes and their width by patch count (the lane footprint is always odd, so 3, 5, then 5 wider). */
	public static final int RAKE_MORE_LANES_AT = 4;
	public static final int RAKE_WIDER_AT = 8;
	public static final double RAKE_HALF_WIDTH = 0.5D;
	public static final double RAKE_WIDE_HALF_WIDTH = 0.55D;
	/** The Spiked Halo: base damage, plus one per this many patches. */
	public static final float HALO_DAMAGE = 8.0F;
	public static final int HALO_PATCHES_PER_DAMAGE = 3;
	/** The Iron Thicket: needs this many patches, spends up to THICKET_SPEND, two discs each, at most six. */
	public static final int THICKET_NEEDS = 3;
	public static final int THICKET_SPEND = 3;
	public static final int THICKET_DISCS_PER_PATCH = 2;
	public static final int THICKET_MAX_DISCS = 6;

	private HemorathIronRules() {
	}

	/** Patches erupted for a leak total, capped. */
	public static int patchesFor(float leakedTotal) {
		return Math.max(0, Math.min(MAX_PATCHES, (int) (leakedTotal / LEAK_PER_PATCH)));
	}

	/** The damage-taken factor his patches give: 1 - 0.04 per patch. */
	public static float reduction(int patches) {
		return 1.0F - REDUCTION_PER_PATCH * clamp(patches);
	}

	/** Side lanes either side of the centre lane: 1 (three lanes), then 2 (five) from four patches. */
	public static int rakeSideLanes(int patches) {
		return patches >= RAKE_MORE_LANES_AT ? 2 : 1;
	}

	public static double rakeHalfWidth(int patches) {
		return patches >= RAKE_WIDER_AT ? RAKE_WIDE_HALF_WIDTH : RAKE_HALF_WIDTH;
	}

	public static float haloDamage(int patches) {
		return HALO_DAMAGE + clamp(patches) / HALO_PATCHES_PER_DAMAGE;
	}

	/** How many patches a Thicket tears out, and how many discs that throws. */
	public static int thicketSpend(int patches) {
		return patches < THICKET_NEEDS ? 0 : Math.min(THICKET_SPEND, patches);
	}

	public static int thicketDiscs(int spent) {
		return Math.min(THICKET_MAX_DISCS, spent * THICKET_DISCS_PER_PATCH);
	}

	private static int clamp(int patches) {
		return Math.max(0, Math.min(MAX_PATCHES, patches));
	}
}
