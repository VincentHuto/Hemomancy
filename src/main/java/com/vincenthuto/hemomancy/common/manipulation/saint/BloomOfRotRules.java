package com.vincenthuto.hemomancy.common.manipulation.saint;

import com.vincenthuto.hemomancy.common.util.HemoEasing;

/**
 * Bloom of Rot's numbers (D-1, canon memory rework spec 3.3): Putriciel's Bloom, Ignition and colonisation made the
 * player's. A field sown round the caster that grows from radius 3 to 8, colonises every body standing in it (the
 * caster too, at half rate), stirs Kindling, and either burns out under the second press or reclaims the caster. No
 * Minecraft types, so the JVM tests read it whole.
 */
public final class BloomOfRotRules {
	public static final double SOW_COST = 450.0D;
	public static final double START_RADIUS = 3.0D, MAX_RADIUS = 8.0D;
	public static final int GROW_TICKS = 40, FIELD_TICKS = 240;
	public static final int COOLDOWN_TICKS = 600;
	/** The caster kneels this long after sowing and cannot walk. */
	public static final int KNEEL_TICKS = 10;
	/** A body stands on the field within this height of the sown floor. */
	public static final double FLOOR_DEPTH = 2.0D;

	public static final int PULSE_TICKS = 20;
	public static final float ROT_DAMAGE = 1.5F;
	public static final int ROOTED = 4, SHELVED = 8, OVERGROWN = 12;
	/** Colonisation falls back to this once a body is Overgrown. */
	public static final int AFTER_OVERGROWN = 6;
	public static final int MAX_COLONISATION = 12;
	public static final int OVERGROWN_ROOT = 30;
	public static final float OVERGROWN_DAMAGE = 5.0F;
	/** The caster is colonised by their own field once per this many ticks, and their Overgrown is milder. */
	public static final int CASTER_TICKS = 40, CASTER_ROOT = 20;
	public static final float CASTER_OVERGROWN_DAMAGE = 4.0F;
	/** Colonisation off every field falls by one each this many ticks. */
	public static final int DECAY_TICKS = 40;
	/** A body that moved less than this since the last pulse stood still. */
	public static final double STILL_DISTANCE = 0.1D;

	public static final double MAX_KINDLING = 100.0D, IGNITE_AT = 40.0D;
	/** The buried will stirs: one Kindling each this many ticks. */
	public static final int KINDLING_TICKS = 10;
	/** One Kindling for each this much damage anything takes on the field. */
	public static final double DAMAGE_PER_KINDLING = 3.0D;
	/** Flammeus damage or fire on the field feeds Kindling this many times over (their rule). */
	public static final double FLAMMEUS_KINDLING = 2.5D;

	/** The fire runs out from the caster across the field over this many ticks. */
	public static final int IGNITION_SPREAD = 12;
	/** The field chars and ends this long after the Ignition. */
	public static final int CHAR_TICKS = 30;
	/** The packed ash lingers until this tick of the Ignition's timeline (client only). */
	public static final int ASH_END = 160;
	public static final double FULL_FIRE = 1.25D;

	/** An unlit field reclaims its caster: this much colonisation and this long of Necrosis. */
	public static final int RECLAIM_COLONISATION = 4, RECLAIM_NECROSIS = 160;
	/** A stage gained refreshes its hold on the body for this long. */
	public static final int STAGE_HOLD = 40;

	private BloomOfRotRules() {
	}

	/** The field's radius {@code age} ticks after sowing: 3 growing to 8 over 40 ticks. */
	public static double radius(double age) {
		return START_RADIUS + (MAX_RADIUS - START_RADIUS) * HemoEasing.easeOutCubic(HemoEasing.clamp01(age / GROW_TICKS));
	}

	/** Colonisation a pulse lays on a body: 1, +1 standing still, +1 with Rime, +1 with Pressure. */
	public static int gain(boolean still, boolean rime, boolean pressure) {
		return 1 + (still ? 1 : 0) + (rime ? 1 : 0) + (pressure ? 1 : 0);
	}

	/** Colonisation after {@code gain}, never over the cap however many fields lay it. */
	public static int colonise(int before, int gain) {
		return Math.max(0, Math.min(MAX_COLONISATION, before + gain));
	}

	/** 0 bare, 1 Rooted, 2 Shelved, 3 Overgrown. */
	public static int stage(int colonisation) {
		if (colonisation >= OVERGROWN) return 3;
		if (colonisation >= SHELVED) return 2;
		return colonisation >= ROOTED ? 1 : 0;
	}

	/** Kindling a hit of {@code damage} on the field stirs: 1 per 3, two and a half times that for Flammeus or fire. */
	public static double kindlingFromDamage(double damage, boolean flammeus) {
		return Math.max(0.0D, damage) / DAMAGE_PER_KINDLING * (flammeus ? FLAMMEUS_KINDLING : 1.0D);
	}

	public static double kindle(double kindling, double add) {
		return Math.max(0.0D, Math.min(MAX_KINDLING, kindling + Math.max(0.0D, add)));
	}

	/** The second press lights the field once Kindling reaches 40. */
	public static boolean canIgnite(double kindling) {
		return kindling >= IGNITE_AT;
	}

	/** At 100 the field lights itself. */
	public static boolean ignitesItself(double kindling) {
		return kindling >= MAX_KINDLING;
	}

	/** The Ignition's Flammeus damage on a body: 2 + 0.75 per colonisation, a quarter more at full Kindling. */
	public static float ignitionDamage(int colonisation, double kindling) {
		double damage = 2.0D + 0.75D * Math.max(0, colonisation);
		return (float) (ignitesItself(kindling) ? damage * FULL_FIRE : damage);
	}

	/** How far the fire has run from the caster {@code age} ticks after the Ignition. */
	public static double fireReach(double age, double radius) {
		return radius * HemoEasing.clamp01(age / IGNITION_SPREAD);
	}

	/** Whether a body {@code horizontal} blocks out and {@code dy} above the sown floor stands on a field of {@code radius}. */
	public static boolean onField(double horizontal, double dy, double radius) {
		return horizontal <= radius && dy >= -FLOOR_DEPTH && dy <= FLOOR_DEPTH;
	}
}
