package com.vincenthuto.hemomancy.common.manipulation.saint;

import com.vincenthuto.hemomancy.common.util.HemoEasing;

/**
 * The Endless Hour's numbers (D-1, canon memory rework spec 3.4): Velorum's Still Floor and Endless Hour made the
 * player's. The caster plants their feet; inside 9 blocks every body that stops moving fills with stillness until it is
 * set in its own frozen blood, and the dark at the rim makes stopping easy. Not time manipulation: only bodies slow. No
 * Minecraft types, so the JVM tests read it whole.
 */
public final class EndlessHourRules {
	public static final double PLANT_COST = 600.0D;
	/** Paid as {@code endless_hour_hold} each {@link #UPKEEP_TICKS} while the Hour holds. */
	public static final double UPKEEP = 15.0D;
	public static final int UPKEEP_TICKS = 20;
	public static final int HOUR_TICKS = 200;
	public static final int COOLDOWN_TICKS = 900;
	public static final double RADIUS = 9.0D, DARK_INNER = 6.0D;
	/** The seams fill out to the rim over this many ticks after planting. */
	public static final int PLANT_TICKS = 20;
	/** The Hour holds while the caster stays this close to the planted point. */
	public static final double PLANTED_REACH = 1.5D;
	/** A planted caster takes this much less damage (his Still Guard at a player's scale). */
	public static final double STILL_GUARD = 0.40D;

	public static final double MAX_STILLNESS = 100.0D;
	/** A body is standing while it moves under walking pace over the last {@link #TRAIL_TICKS} ticks. */
	public static final double WALKING_PACE = 0.13D;
	public static final int TRAIL_TICKS = 10;
	public static final double STANDING_FILL = 1.2D, ACTING_FILL = 0.6D, RIME_FILL = 0.4D, TRAVEL_DRAIN = 1.5D;
	public static final double DARK_FILL = 1.4D, SEARING_DRAIN = 0.5D;
	/** Stillness out of the Hour drains at the travel rate. */
	public static final double OUTSIDE_DRAIN = 1.5D;

	/** At full stillness a body is preserved: Paralysis, Rime to 3, 6 Congeatio, stillness back to 30. */
	public static final int PRESERVE_PARALYSIS = 60;
	public static final int PRESERVE_RIME = 3;
	public static final float PRESERVE_DAMAGE = 6.0F;
	public static final double AFTER_PRESERVE = 30.0D;
	/** A boss's stillness stops here; when it would be preserved it takes the damage and drops back. */
	public static final double BOSS_CAP = 90.0D;
	/** The Obscured the dark rim lays is refreshed for this long while a body stands in it. */
	public static final int DARK_OBSCURED = 20;

	/** At the close every body at 50 or more is preserved and the rest take 4. */
	public static final double CLOSE_PRESERVE = 50.0D;
	public static final float CLOSE_DAMAGE = 4.0F;
	/** Then the caster sets in their own cruor: Paralysis this long, taking this much less damage. */
	public static final int SELF_SET_TICKS = 20;
	public static final double SELF_SET_GUARD = 0.70D;

	/** The Bow: a caster who takes this share of their max health inside one Hour breaks it. */
	public static final double BOW_SHARE = 0.30D;
	public static final int BOW_TICKS = 40;
	public static final double BOW_HURT = 0.50D;
	/** In Velorum's own fight his stillness fills this many times faster on a caster whose Hour holds. */
	public static final float VELORUM_STILLNESS = 1.5F;

	/** The close's timeline: blades erupt 0 to 12, the caster's own set 12 to 32. */
	public static final int CLOSE_END = 12, SET_END = 32;

	private EndlessHourRules() {
	}

	/** How far the frozen seams have filled toward the rim {@code age} ticks after planting, 0 to 1. */
	public static double seams(double age) {
		return HemoEasing.easeOutCubic(HemoEasing.clamp01(age / PLANT_TICKS));
	}

	public static boolean inHour(double distance) {
		return distance <= RADIUS;
	}

	public static boolean inDark(double distance) {
		return distance >= DARK_INNER && distance <= RADIUS;
	}

	public static boolean planted(double distanceFromPoint) {
		return distanceFromPoint <= PLANTED_REACH;
	}

	/** Horizontal speed in blocks a tick over the trail. */
	public static double pace(double travelled, int ticks) {
		return ticks <= 0 ? 0.0D : travelled / ticks;
	}

	/**
	 * One tick's change in stillness: standing fills 1.2 (+0.6 acting, +0.4 per Rime level, times 1.4 in the dark);
	 * travelling faster drains 1.5; Searing drains 0.5 more; a Disrupted body counts as standing.
	 */
	public static double step(double pace, boolean acting, int rime, boolean dark, boolean searing, boolean disrupted) {
		boolean standing = disrupted || pace < WALKING_PACE;
		double change;
		if (standing) {
			change = STANDING_FILL + (acting ? ACTING_FILL : 0.0D) + RIME_FILL * Math.max(0, rime);
			if (dark) change *= DARK_FILL;
		} else {
			change = -TRAVEL_DRAIN;
		}
		if (searing) change -= SEARING_DRAIN;
		return change;
	}

	/** Stillness after {@code change}, kept in 0 to 100, a boss's never past 90. */
	public static double still(double before, double change, boolean boss) {
		double cap = boss ? BOSS_CAP : MAX_STILLNESS;
		return Math.max(0.0D, Math.min(cap, before + change));
	}

	/** Whether {@code stillness} preserves the body: 100, or a boss pressed at its cap. */
	public static boolean preserves(double stillness, boolean boss) {
		return stillness >= (boss ? BOSS_CAP : MAX_STILLNESS);
	}

	/** The share of each hit a caster takes: the Still Guard while planted, the self-set, the Bow. */
	public static double taken(boolean planted, boolean setting, boolean bowing) {
		double scale = 1.0D;
		if (planted) scale *= 1.0D - STILL_GUARD;
		if (setting) scale *= 1.0D - SELF_SET_GUARD;
		if (bowing) scale *= 1.0D + BOW_HURT;
		return scale;
	}

	public static boolean bows(double takenThisHour, double maxHealth) {
		return maxHealth > 0.0D && takenThisHour >= BOW_SHARE * maxHealth;
	}
}
