package com.vincenthuto.hemomancy.common.entity.boss.saint.seraphae;

/**
 * Her presence over-heals (design section 5): near her a wounded player is mended, and a whole one grows. Growth runs
 * 0 to 10 per player: Swelling from 3, Knotting from 6, a Burst at 10. Wounds, fed anchors and stilling casts shed it;
 * it ebbs away outside her radius. Pure, so it is JVM-tested.
 */
public final class SeraphaeGrowthRules {
	public static final double RADIUS = 9.0D;
	public static final double CLOSE = 3.5D;
	public static final int CADENCE = 20;
	public static final int CADENCE_REPOSE = 14;
	public static final float HEAL_PER_PULSE = 1.0F;
	public static final int MAX = 10;
	public static final int SWELLING = 3;
	public static final int KNOTTING = 6;
	public static final float BURST_DAMAGE = 6.0F;
	public static final float BURST_DAMAGE_LATE = 8.0F;
	public static final int BURST_WEAKNESS = 60;
	public static final int BURST_ROOT = 20;
	/** Damage that sheds one growth: a wound for the light to fill. */
	public static final float DAMAGE_PER_SHED = 2.0F;
	/** Fed Projection ticks that shed one growth. */
	public static final int FED_TICKS_PER_SHED = 20;
	public static final int CAST_SHED = 2;
	/** Outside her radius growth ebbs by one this often. */
	public static final int EBB_TICKS = 40;
	/** The stage effect is refreshed at this length every 20 ticks. */
	public static final int STAGE_EFFECT_TICKS = 30;
	/** A hold: every 10 ticks it heals this much, else grows this much (more in phase III). */
	public static final int HOLD_PULSE = 10;
	public static final float HOLD_HEAL = 2.0F;
	public static final int HOLD_GROWTH = 2;
	public static final int HOLD_GROWTH_LATE = 3;

	public enum Stage { NONE, SWELLING, KNOTTING, BURST }

	/** Per-player flag bits on the meter (bits 0..15 are the Saint's). */
	public static final int FLAG_SWELLING = 1;
	public static final int FLAG_KNOTTING = 1 << 1;
	public static final int FLAG_HELD = 1 << 2;
	public static final int FLAG_FEEDING = 1 << 3;
	public static final int FLAG_BURST = 1 << 4;

	private SeraphaeGrowthRules() {
	}

	public static Stage stage(int growth) {
		if (growth >= MAX) return Stage.BURST;
		if (growth >= KNOTTING) return Stage.KNOTTING;
		if (growth >= SWELLING) return Stage.SWELLING;
		return Stage.NONE;
	}

	/** One pulse's heal: up to {@link #HEAL_PER_PULSE} of the missing health, never past full. */
	public static float healing(float health, float maximum) {
		return Math.max(0.0F, Math.min(HEAL_PER_PULSE, maximum - health));
	}

	public static boolean wounded(float health, float maximum) {
		return maximum - health > 1.0E-3F;
	}

	/** One pulse's change for a player: the wounded shed one, the whole grow one (two when close). */
	public static int pulse(boolean wounded, double distance) {
		if (wounded) return -1;
		return distance <= CLOSE ? 2 : 1;
	}

	public static int cadence(boolean noRest) {
		return noRest ? CADENCE_REPOSE : CADENCE;
	}

	public static float burstDamage(int phase) {
		return phase >= 3 ? BURST_DAMAGE_LATE : BURST_DAMAGE;
	}

	public static int holdGrowth(int phase) {
		return phase >= 3 ? HOLD_GROWTH_LATE : HOLD_GROWTH;
	}

	/**
	 * Growth after damage is taken: every {@link #DAMAGE_PER_SHED} sheds one. Returns {growth, carried damage}: the
	 * part of a wound too small to shed carries to the next.
	 */
	public static float[] shedByDamage(int growth, float carried, float damage) {
		float total = carried + Math.max(0.0F, damage);
		int shed = (int) Math.floor(total / DAMAGE_PER_SHED);
		return new float[]{Math.max(0, growth - shed), total - shed * DAMAGE_PER_SHED};
	}

	public static int clamp(int growth) {
		return Math.max(0, Math.min(MAX, growth));
	}

	/** The flag word the meter carries for a growth and its held and feeding states. */
	public static int flags(int growth, boolean held, boolean feeding) {
		int flags = 0;
		Stage stage = stage(growth);
		if (stage == Stage.SWELLING) flags |= FLAG_SWELLING;
		if (stage == Stage.KNOTTING || stage == Stage.BURST) flags |= FLAG_SWELLING | FLAG_KNOTTING;
		if (held) flags |= FLAG_HELD;
		if (feeding) flags |= FLAG_FEEDING;
		return flags;
	}
}
