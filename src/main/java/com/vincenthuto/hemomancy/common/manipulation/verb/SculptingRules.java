package com.vincenthuto.hemomancy.common.manipulation.verb;

/** Pure timing for Sculpting sessions: when a pulse is due and when held work counts as a manipulation use. */
public final class SculptingRules {
	/**
	 * Ticks of paid Sculpting work that count as one use of the manipulation (mastery, temper shift, vascular strain,
	 * skill points): one use per four seconds, close to how often a Harbinger recasts an ordinary manipulation, so
	 * the D5 strain gate fills at a matched rate whether the player casts or shapes.
	 */
	public static final int TICKS_PER_CREDITED_USE = 80;

	private SculptingRules() {
	}

	/**
	 * Whether a pulse is due: at once when the player has never pulsed ({@code lastPulse} is {@link Long#MIN_VALUE}),
	 * then once {@code cadence} ticks have passed since the last one, whatever the target. Never twice in one tick.
	 */
	public static boolean pulseDue(long lastPulse, long now, int cadence) {
		return lastPulse == Long.MIN_VALUE || now - lastPulse >= Math.max(1, cadence);
	}

	/** How many credited uses a paid pulse earns, given the paid work already banked this session (in ticks). */
	public static int creditedUses(int bankedTicks, int cadence) {
		int before = Math.max(0, bankedTicks) / TICKS_PER_CREDITED_USE;
		int after = (Math.max(0, bankedTicks) + Math.max(1, cadence)) / TICKS_PER_CREDITED_USE;
		return after - before;
	}

	/**
	 * The order a manipulation's Sculptings of one verb claim in: its narrow Sculptings first, in registration order, then
	 * its main one (core-loop round two P4). A narrow Sculpting takes only a few targets (water for Glacial Grasp's
	 * freeze) ahead of a main one that takes most (its plant strand).
	 */
	public static <T> java.util.List<T> claimOrder(java.util.List<T> narrow, T main) {
		java.util.List<T> order = new java.util.ArrayList<>(narrow);
		if (main != null) order.add(main);
		return java.util.List.copyOf(order);
	}

	/**
	 * The cells a sweep passes between the last worked cell and this one, not counting either end, when they lie at
	 * most {@code join} cells apart (by the largest axis); empty when they are further apart or the same cell. A sweep
	 * faster than the pulse still leaves an unbroken line.
	 */
	public static java.util.List<net.minecraft.core.BlockPos> sweepBetween(net.minecraft.core.BlockPos last,
			net.minecraft.core.BlockPos cell, int join) {
		java.util.List<net.minecraft.core.BlockPos> cells = new java.util.ArrayList<>();
		if (last == null || last.equals(cell)) return cells;
		int steps = Math.max(Math.abs(cell.getX() - last.getX()),
				Math.max(Math.abs(cell.getY() - last.getY()), Math.abs(cell.getZ() - last.getZ())));
		if (steps > join) return cells;
		for (int i = 1; i < steps; i++) {
			double t = (double) i / steps;
			cells.add(net.minecraft.core.BlockPos.containing(last.getX() + 0.5D + (cell.getX() - last.getX()) * t,
					last.getY() + 0.5D + (cell.getY() - last.getY()) * t, last.getZ() + 0.5D + (cell.getZ() - last.getZ()) * t));
		}
		return cells;
	}

	/**
	 * How long something a Sculpting raises stands (a swept wall's ice or iron): its full {@code baseTicks} at full
	 * strength, shorter when the pulse ran dry, never under a quarter of it. The Staff raises more, not longer.
	 */
	public static int standingTicks(int baseTicks, double workScale) {
		double scale = Math.max(0.25D, Math.min(1.0D, workScale));
		return Math.max(1, (int) Math.round(baseTicks * scale));
	}

	/** Blood per second a Sculpting costs at its base price, for the channel HUD. */
	public static double bloodPerSecond(double bloodPerPulse, int cadence) {
		return Math.max(0.0D, bloodPerPulse) * 20.0D / Math.max(1, cadence);
	}
}
