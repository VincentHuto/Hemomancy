package com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel;

import javax.annotation.Nullable;

/**
 * Putriciel's lingering ground hazards, spawned as the kit's {@code BossGroundHazardEntity} through the arena: the rot
 * colony a landed shelf leaves, Rot Breath's lingering cloud, the purging fire a Censer Cast leaves and the short fire
 * wall at a Purging Sweep's end, and (revision 2026-11-06) the rot sludge, the pools burning where the fire caught them
 * and the fire runners carrying it there. Each pulse hurts and changes colonisation; the interval's fire catches the
 * sludge and the colonies on its schedule ({@link PutricielSludgeRules}) and flashes the breath clouds off.
 * The kind's id is the hazard's synced kind, which the client's telegraph style reads.
 */
public enum PutricielHazard {
	ROT_COLONY(1, 160, 20, 1.0F, 1.0F, 0, false),
	ROT_CLOUD(2, 60, 20, 1.0F, 1.0F, 0, false),
	PURGING_FIRE(3, 100, 10, 2.0F, -2.0F, 0, true),
	FIRE_WALL(4, 60, 10, 3.0F, -1.0F, 2, true),
	/** Rot sludge (revision 2026-11-06, R2): slows and colonises whoever wades, until the interval burns it. */
	ROT_SLUDGE(5, PutricielSludgeRules.POOL_LIFE, PutricielSludgeRules.POOL_PULSE, 0.0F,
			PutricielSludgeRules.POOL_COLONISATION, 0, false),
	/** A pool burning where the fire caught it: a small cleanse that costs, then cooling that touches nothing. */
	SLUDGE_FIRE(6, PutricielSludgeRules.FIRE_LIFE, PutricielSludgeRules.FIRE_PULSE, PutricielSludgeRules.FIRE_DAMAGE,
			PutricielSludgeRules.FIRE_COLONISATION, 0, true, PutricielSludgeRules.BURN),
	/** A fire runner crawling from a relit pyre or a burning pool to the next pool: harmless, its life set per spawn. */
	FIRE_RUNNER(7, PutricielSludgeRules.RUNNER_TAIL, PutricielSludgeRules.RUNNER_TAIL, 0.0F, 0.0F, 0, true, 0);

	/** A rot colony's radius, and the purging fire's. */
	public static final double COLONY_RADIUS = 1.5D;
	public static final double FIRE_RADIUS = 2.0D;
	/** The fire wall across a Purging Sweep's end: its length and half-width. */
	public static final double WALL_LENGTH = 4.0D;
	public static final double WALL_HALF_WIDTH = 0.4D;

	private final int id;
	private final int life;
	private final int pulse;
	private final float damage;
	private final float colonisation;
	private final int igniteSeconds;
	private final boolean fire;
	/** Ticks of age after which the hazard touches nothing (-1: never, 0: from its first tick). */
	private final int harmlessAfter;

	PutricielHazard(int id, int life, int pulse, float damage, float colonisation, int igniteSeconds, boolean fire) {
		this(id, life, pulse, damage, colonisation, igniteSeconds, fire, -1);
	}

	PutricielHazard(int id, int life, int pulse, float damage, float colonisation, int igniteSeconds, boolean fire,
			int harmlessAfter) {
		this.harmlessAfter = harmlessAfter;
		this.id = id;
		this.life = life;
		this.pulse = pulse;
		this.damage = damage;
		this.colonisation = colonisation;
		this.igniteSeconds = igniteSeconds;
		this.fire = fire;
	}

	public int id() {
		return id;
	}

	public int life() {
		return life;
	}

	public int pulse() {
		return pulse;
	}

	public float damage() {
		return damage;
	}

	/** Colonisation each pulse adds (negative: the fire's cleanse). */
	public float colonisation() {
		return colonisation;
	}

	public int igniteSeconds() {
		return igniteSeconds;
	}

	public boolean fire() {
		return fire;
	}

	/** Whether a hazard of this kind at {@code age} touches nothing: a burning pool cooling, a fire runner. */
	public boolean harmless(int age) {
		return harmlessAfter == 0 || harmlessAfter > 0 && age >= harmlessAfter;
	}

	/** The rot the interval's fire catches on its schedule: sludge pools and colony patches. */
	public boolean catches() {
		return this == ROT_SLUDGE || this == ROT_COLONY;
	}

	@Nullable
	public static PutricielHazard byId(int id) {
		for (PutricielHazard hazard : values()) if (hazard.id == id) return hazard;
		return null;
	}
}
