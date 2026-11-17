package com.vincenthuto.hemomancy.common.manipulation.ferric;

/**
 * Ferric Spikes under held Projection, as plain numbers: a run of blades that travels through the ground toward
 * wherever the caster looks. It moves at a steady pace however far the aim is, climbs a block, drops two, and stops at
 * anything more. No Minecraft types, so it can be tested on the JVM.
 */
public final class FerricRunRules {
	/** Blocks the run travels a tick. */
	public static final double SPEED = 0.45D;
	/** The most the ground may rise and fall under the run in one step before it stops. */
	public static final int MAX_CLIMB = 1, MAX_DROP = 2;
	/** How far from its caster the run may go. */
	public static final double LEASH = 16.0D;
	/** How close to the run's middle a body must stand to be impaled, and how far above or below it. */
	public static final double BITE_RADIUS = 0.9D, BITE_HEIGHT = 1.6D;
	/** Damage a pass deals before Crimson Mastery, how long its Lodestone lasts, and ticks before the same body is bitten again. */
	public static final float BITE_DAMAGE = 3.0F;
	public static final int LODESTONE_TICKS = 120, BITE_LOCKOUT = 10;
	/** Blood a pulse costs before the cost ledger: 50 mL a second. */
	public static final double BLOOD_PER_PULSE = 25.0D;
	/** How long the standing blades of an eruption last, how many rise, and how far apart. */
	public static final int ERUPTION_TICKS = 80, ERUPTION_BLADES = 3;
	public static final double ERUPTION_SPACING = 0.9D;
	/** Ticks after an eruption before a new run may start. */
	public static final int SPENT_TICKS = 30;
	/** How fast the caster walks while steering, as a share of their pace. */
	public static final double PACE = 0.7D;

	private FerricRunRules() {
	}

	/**
	 * One tick of travel on the ground plane from ({@code x}, {@code z}) toward ({@code toX}, {@code toZ}): a full
	 * stride while the aim is further off than one, else onto the aim. Returns {@code {x, z}}.
	 */
	public static double[] stride(double x, double z, double toX, double toZ) {
		double dx = toX - x, dz = toZ - z;
		double distance = Math.sqrt(dx * dx + dz * dz);
		if (distance <= SPEED) return new double[] {toX, toZ};
		return new double[] {x + dx / distance * SPEED, z + dz / distance * SPEED};
	}

	/** Whether the run may follow ground that changes by {@code rise} blocks in one stride (negative for a drop). */
	public static boolean follows(int rise) {
		return rise <= MAX_CLIMB && rise >= -MAX_DROP;
	}

	/** Whether a point {@code distance} from the caster is still on the run's leash. */
	public static boolean leashed(double distance) {
		return distance <= LEASH;
	}

	/** How far to either side of the run's middle blade {@code index} of an eruption stands. */
	public static double bladeOffset(int index) {
		return (index - (ERUPTION_BLADES - 1) / 2.0D) * ERUPTION_SPACING;
	}
}
