package com.vincenthuto.hemomancy.common.entity.boss.endgame;

public final class VesperWingedFlightRules {
	public static final int WING_GROWTH_TICKS = 60;
	public static final int GROUNDED_COOLDOWN_TICKS = 160;
	public static final int TAKEOFF_TICKS = 20;
	public static final int MAX_AIRBORNE_TICKS = 120;
	public static final int LANDING_TICKS = 25;
	public static final int DIVE_TELEGRAPH_TICKS = 20;
	public static final int DIVE_TICKS = 20;
	public static final int TAIL_FUSILLADE_TICKS = 30;
	public static final int CIRCLE_TICKS = 24;
	public static final double MIN_ALTITUDE = 4.0D;
	public static final double MAX_ALTITUDE = 10.0D;
	public static final double SAFE_ARENA_HALF_EXTENT = 21.0D;

	private VesperWingedFlightRules() {
	}

	public static boolean shouldStartWingGrowth(float health, float maxHealth, boolean wingsGrown,
			boolean anchorExposed, boolean transitioning) {
		return !wingsGrown && maxHealth > 0.0F && health <= maxHealth * 0.5F
				&& !anchorExposed && !transitioning;
	}

	public static boolean mayStartSortie(int groundedTicks, boolean wingsGrown, boolean arenaValid) {
		return wingsGrown && arenaValid && groundedTicks >= GROUNDED_COOLDOWN_TICKS;
	}

	public static ArenaAuthority arenaAuthority(boolean ownerBound, boolean ownerAuthorityValid,
			boolean summonedArenaBound) {
		if (ownerBound) return ownerAuthorityValid ? ArenaAuthority.OWNED_ORDEAL : ArenaAuthority.NONE;
		return summonedArenaBound ? ArenaAuthority.SUMMONED : ArenaAuthority.NONE;
	}

	public static boolean shouldDeferAnchorExposure(FlightMode mode) {
		return mode == FlightMode.WING_GROWTH;
	}

	public static Point clampFlightPoint(double x, double y, double z,
			double centerX, double floorY, double centerZ) {
		return new Point(clamp(x, centerX - SAFE_ARENA_HALF_EXTENT, centerX + SAFE_ARENA_HALF_EXTENT),
				clamp(y, floorY + MIN_ALTITUDE, floorY + MAX_ALTITUDE),
				clamp(z, centerZ - SAFE_ARENA_HALF_EXTENT, centerZ + SAFE_ARENA_HALF_EXTENT));
	}

	public static Point clampGroundPoint(double x, double z, double centerX, double floorY, double centerZ) {
		return new Point(clamp(x, centerX - SAFE_ARENA_HALF_EXTENT, centerX + SAFE_ARENA_HALF_EXTENT),
				floorY + 1.0D,
				clamp(z, centerZ - SAFE_ARENA_HALF_EXTENT, centerZ + SAFE_ARENA_HALF_EXTENT));
	}

	public static AerialAttack selectAerialAttack(int sortieIndex, int airborneTicksRemaining) {
		if (airborneTicksRemaining < 40) return AerialAttack.DIVING_REND;
		return Math.floorMod(sortieIndex, 2) == 0
				? AerialAttack.DIVING_REND : AerialAttack.TAIL_NEEDLE_FUSILLADE;
	}

	public static boolean mustLand(int airborneTicks, boolean arenaValid) {
		return airborneTicks >= MAX_AIRBORNE_TICKS || !arenaValid;
	}

	public static int fusilladeNeedleCount(int attackTick) {
		return attackTick == 12 || attackTick == 16 || attackTick == 20 ? 5 : 0;
	}

	public static FlightMode recoverMode(FlightMode saved, boolean arenaValid, boolean flightPathValid) {
		if (saved.airborne() && (!arenaValid || !flightPathValid)) return FlightMode.LANDING;
		return saved;
	}

	private static double clamp(double value, double minimum, double maximum) {
		return Math.max(minimum, Math.min(maximum, value));
	}

	public enum AerialAttack { DIVING_REND, TAIL_NEEDLE_FUSILLADE }

	public enum ArenaAuthority { NONE, OWNED_ORDEAL, SUMMONED }

	public enum FlightMode {
		GROUNDED(false), WING_GROWTH(false), TAKEOFF(true), CIRCLING(true),
		DIVE_TELEGRAPH(true), DIVING_REND(true), TAIL_FUSILLADE(true), LANDING(true);

		private final boolean airborne;

		FlightMode(boolean airborne) {
			this.airborne = airborne;
		}

		public boolean airborne() {
			return airborne;
		}
	}

	public record Point(double x, double y, double z) {
	}
}
