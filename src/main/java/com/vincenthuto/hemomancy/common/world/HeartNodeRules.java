package com.vincenthuto.hemomancy.common.world;

import java.util.UUID;

/** The heart-node sanctum: who may plant one, how far its envelope reaches, and whose death it keeps. */
public final class HeartNodeRules {
	/** The heart lobe: a Gardens chamber, not a county. */
	public static final double HEART_RADIUS = 12.0D;
	/** Each Mycelial Synapse's lobe. */
	public static final double SYNAPSE_RADIUS = 8.0D;
	/** Synapses one node may hold; there is no bloodline to scale with. */
	public static final int SYNAPSE_BUDGET = 6;
	/** Blocks above or below the heart or synapse that claims a cell, so one envelope never takes the layer over it. */
	public static final int VERTICAL_BAND = 8;
	/** How much of the owner's benefit is left at the envelope's far edge is {@code 1 - EDGE_FALLOFF}. */
	public static final double EDGE_FALLOFF = 0.5D;
	/** Ticks from planting to full maturity. */
	public static final int MATURE_TICKS = 3 * 60 * 20;
	public static final int MATURE_STAGE = 3;
	/**
	 * Ticks between heartbeats: the pulse leaves the heart this often across the spread, and the node's own heart
	 * beats on the same clock.
	 */
	public static final int HEARTBEAT_TICKS = 50;
	/** The planting time of a node saved before maturity existed: far enough back to read as grown on any clock. */
	public static final long LONG_GROWN = Long.MIN_VALUE / 4;
	public static final int BONUS_TICKS = 40;
	public static final float BONUS_SPORES = 4f;
	public static final int CACHE_SEARCH = 8;
	/** How often the owner's crossing of their envelope's edge is checked, and the shortest gap between warnings. */
	public static final int EDGE_CHECK_TICKS = 10;
	public static final int EDGE_WARNING_TICKS = 10 * 20;
	/** A warning time that means none has been given yet. */
	public static final long NEVER_WARNED = Long.MIN_VALUE;

	private HeartNodeRules() {}

	public static boolean mayPlant(boolean apotheos, boolean inDimension, boolean onNetwork) {
		return apotheos && inDimension && onNetwork;
	}

	public static boolean shouldCache(boolean apotheos, boolean keepInventory, boolean insideOwnEnvelope) {
		return apotheos && !keepInventory && insideOwnEnvelope;
	}

	public static boolean mayOpen(UUID owner, UUID actor) {
		return owner != null && owner.equals(actor);
	}

	/** The node's maturity, 0 at planting and {@link #MATURE_STAGE} once {@link #MATURE_TICKS} have passed. */
	public static int stageAt(long plantedAt, long gameTime) {
		long age = Math.max(0L, gameTime - plantedAt);
		return age >= MATURE_TICKS ? MATURE_STAGE : (int) (age * MATURE_STAGE / MATURE_TICKS);
	}

	/** Ticks until the node reaches its next stage; 0 once it is mature. */
	public static long ticksToNextStage(long plantedAt, long gameTime) {
		int stage = stageAt(plantedAt, gameTime);
		if (stage >= MATURE_STAGE) return 0L;
		long age = Math.max(0L, gameTime - plantedAt);
		// The first age at which age * MATURE_STAGE / MATURE_TICKS reaches stage + 1.
		long next = ((long) (stage + 1) * MATURE_TICKS + MATURE_STAGE - 1) / MATURE_STAGE;
		return next - age;
	}

	/**
	 * Whether stepping from {@code wasInside} to {@code inside} at {@code now} earns the "you leave the sanctum" line:
	 * only on the way out, and no sooner than {@link #EDGE_WARNING_TICKS} after the last one.
	 */
	public static boolean warnsLeaving(boolean wasInside, boolean inside, long lastWarned, long now) {
		if (!wasInside || inside) return false;
		return lastWarned == NEVER_WARNED || now - lastWarned >= EDGE_WARNING_TICKS;
	}

	/** Spore load for one pulse at {@code strength} (1 in the heart lobe, tapering to the edge). */
	public static float bonusSpores(double strength) {
		return (float) (BONUS_SPORES * strength);
	}
}
