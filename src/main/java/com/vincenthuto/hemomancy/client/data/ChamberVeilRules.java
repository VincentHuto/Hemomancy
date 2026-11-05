package com.vincenthuto.hemomancy.client.data;

/** The timing of the veil drawn over a dimension change into or out of the Chamber of Will. Pure, in milliseconds. */
public final class ChamberVeilRules {
	/** The hole's apparent radius as the crossing ends: one block from a hole 0.6 wide, as the view shader draws it. */
	public static final float ARRIVAL_RADIUS = (float) Math.atan2(0.6D, 1.0D);
	/** Past this the hole covers every ray the view can cast. */
	public static final float FULL_RADIUS = 2.2F;
	/** How long the hole takes to swallow the view; the level is usually ready sooner, and the veil then simply ends. */
	public static final long SWALLOW_MILLIS = 700L;
	/** An armed veil not used by then belongs to a crossing that stepped back out, and is forgotten. */
	public static final long ARM_MILLIS = 3000L;

	private ChamberVeilRules() {
	}

	/** The hole's radius {@code elapsed} milliseconds into a plain veil: eased out, and full from the swallow on. */
	public static float holeRadius(long elapsed) {
		return holeRadius(elapsed, ARRIVAL_RADIUS);
	}

	/** As above from {@code start}: a veil armed by a gateway walk starts where the walk's hole ended, already full. */
	public static float holeRadius(long elapsed, float start) {
		float t = Math.clamp(elapsed / (float) SWALLOW_MILLIS, 0.0F, 1.0F);
		float eased = 1.0F - (1.0F - t) * (1.0F - t);
		return start + (FULL_RADIUS - start) * eased;
	}

	public static boolean stillArmed(long armedAt, long now) {
		return now - armedAt <= ARM_MILLIS;
	}
}
