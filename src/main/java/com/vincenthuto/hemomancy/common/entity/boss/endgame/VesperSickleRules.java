package com.vincenthuto.hemomancy.common.entity.boss.endgame;

import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import static com.vincenthuto.hemomancy.common.entity.boss.endgame.VesperWeaponAction.*;

/**
 * The dual-sickle act as strings rather than isolated moves: an opener, branch points that read whether the last hit
 * landed, whether the target is shielding and how far away it is, and a readable recovery that grows with the
 * string. The hook's pull is a combo starter, spins carry their momentum, and Harvest is his last stand.
 */
public final class VesperSickleRules {
	public static final int HARVEST_RECOVERY_TICKS = 60;
	public static final float REEL_BREAK_DAMAGE = 12.0F;
	public static final int REEL_BREAK_SPRINT_TICKS = 30;

	public enum SickleString { REAPING, HOOK_AND_REEL, GALLOWS_WHEEL, CYCLONE, POUNCE, CRESCENT, ANCHOR_HOOK, HARVEST }

	public static final Set<VesperWeaponAction> ALL_MOVES = EnumSet.of(SICKLE_CYCLONE, SICKLE_POUNCE,
			SICKLE_CROSS_REND, SICKLE_HOOK, SANGUINE_CRESCENTS, SICKLE_REAP_L, SICKLE_REAP_R, SICKLE_HOOK_REEL,
			SICKLE_REEL_CYCLONE, SICKLE_REAPERS_RISE, SICKLE_PASS_THROUGH, SICKLE_GALLOWS_WHEEL,
			SICKLE_TRAVELLING_CYCLONE, SICKLE_REVERSE_CYCLONE, SICKLE_FEINT_CYCLONE, SICKLE_POUNCE_FEINT,
			SICKLE_CRESCENT_FAN, SICKLE_CRESCENT_STAGGER, SICKLE_CRESCENT_SKIM, SICKLE_CRESCENT_RETURN,
			SICKLE_ANCHOR_HOOK, SICKLE_HARVEST);

	private VesperSickleRules() {
	}

	/** The next string. Harvest is never chosen here; its trigger starts it. */
	public static SickleString choose(double distance, boolean shielding, boolean cornered, SickleString last,
			RandomSource random) {
		if (cornered && last != SickleString.ANCHOR_HOOK) return SickleString.ANCHOR_HOOK;
		if (shielding && last != SickleString.HOOK_AND_REEL) return SickleString.HOOK_AND_REEL;
		List<SickleString> options;
		if (distance <= 4.5D) options = List.of(SickleString.REAPING, SickleString.CYCLONE, SickleString.REAPING);
		else if (distance <= 8.5D) options = List.of(SickleString.POUNCE, SickleString.GALLOWS_WHEEL,
				SickleString.CYCLONE, SickleString.HOOK_AND_REEL);
		else options = List.of(SickleString.HOOK_AND_REEL, SickleString.CRESCENT, SickleString.ANCHOR_HOOK,
				SickleString.POUNCE);
		List<SickleString> fresh = options.stream().filter(option -> option != last).toList();
		return fresh.get(random.nextInt(fresh.size()));
	}

	/** The move at this step of a string, or NONE when the string is over. */
	public static VesperWeaponAction branch(SickleString string, int step, int reaps, boolean lastHit, boolean shielding,
			double distance, RandomSource random) {
		return switch (string) {
			case REAPING -> {
				if (step < reaps) yield (step & 1) == 0 ? SICKLE_REAP_L : SICKLE_REAP_R;
				if (step > reaps) yield NONE;
				// Backing off out of the string gets the hook thrown after them; close, it ends in a cross or a spin.
				if (distance > 6.0D) yield SICKLE_HOOK_REEL;
				yield pick(random, SICKLE_CROSS_REND, SICKLE_FEINT_CYCLONE, SICKLE_CYCLONE);
			}
			case HOOK_AND_REEL -> switch (step) {
				case 0 -> SICKLE_HOOK_REEL;
				case 1 -> lastHit ? pick(random, SICKLE_REEL_CYCLONE, SICKLE_REAPERS_RISE, SICKLE_PASS_THROUGH)
						: SICKLE_REAP_L;
				default -> NONE;
			};
			case GALLOWS_WHEEL -> step == 0 ? SICKLE_GALLOWS_WHEEL : NONE;
			case CYCLONE -> {
				if (step == 0) yield pick(random, SICKLE_TRAVELLING_CYCLONE, SICKLE_REVERSE_CYCLONE, SICKLE_FEINT_CYCLONE);
				yield NONE;
			}
			case POUNCE -> {
				if (step == 0) yield shielding ? SICKLE_POUNCE_FEINT : pick(random, SICKLE_POUNCE, SICKLE_POUNCE_FEINT);
				yield NONE;
			}
			case CRESCENT -> step == 0 ? pick(random, SICKLE_CRESCENT_FAN, SICKLE_CRESCENT_STAGGER,
					SICKLE_CRESCENT_SKIM, SICKLE_CRESCENT_RETURN, SANGUINE_CRESCENTS) : NONE;
			case ANCHOR_HOOK -> switch (step) {
				case 0 -> SICKLE_ANCHOR_HOOK;
				case 1 -> distance <= 6.0D ? SICKLE_POUNCE : SICKLE_CRESCENT_FAN;
				default -> NONE;
			};
			case HARVEST -> step == 0 ? SICKLE_HARVEST : NONE;
		};
	}

	/** Follow-ups chained off a single move rather than the string's step (feints and landing spins). */
	public static VesperWeaponAction followUp(VesperWeaponAction finished, boolean lastHit) {
		return switch (finished) {
			case SICKLE_FEINT_CYCLONE -> SICKLE_POUNCE;
			case SICKLE_POUNCE -> SICKLE_CYCLONE;
			case SICKLE_POUNCE_FEINT -> SICKLE_HOOK_REEL;
			case SICKLE_REVERSE_CYCLONE -> lastHit ? SICKLE_CROSS_REND : NONE;
			default -> NONE;
		};
	}

	public static int reapCount(RandomSource random) {
		return 2 + random.nextInt(4);
	}

	/** Spin speed over a spin's life: winds up into the rotation and bleeds out of it. */
	public static float spinRate(float progress) {
		float t = Mth.clamp(progress, 0.0F, 1.0F);
		return Mth.sin(t * Mth.PI) * (0.6F + 0.4F * Mth.sin(t * Mth.PI));
	}

	/** Leftover momentum a spin hands to whatever comes next (blocks per tick). */
	public static double carryVelocity(VesperWeaponAction spin) {
		return switch (spin) {
			case SICKLE_TRAVELLING_CYCLONE -> 0.55D;
			case SICKLE_FEINT_CYCLONE -> 0.75D;
			case SICKLE_CYCLONE, SICKLE_REEL_CYCLONE -> 0.35D;
			case SICKLE_REVERSE_CYCLONE -> 0.25D;
			default -> 0.0D;
		};
	}

	public static boolean reelBreaks(float damageDuringReel, int sprintAwayTicks) {
		return damageDuringReel >= REEL_BREAK_DAMAGE || sprintAwayTicks >= REEL_BREAK_SPRINT_TICKS;
	}

	public static boolean harvestReady(float health, float maxHealth, boolean used) {
		return !used && maxHealth > 0.0F && health < maxHealth * 0.10F;
	}

	public static int recoveryTicks(int stringLength) {
		return Math.min(18, 3 + 3 * Math.max(0, stringLength));
	}

	public static boolean committedSideHolds(int ticksOnSide) {
		return ticksOnSide >= 20 && ticksOnSide <= 40;
	}

	private static VesperWeaponAction pick(RandomSource random, VesperWeaponAction... options) {
		return options[random.nextInt(options.length)];
	}

	/** The Hook and Reel throw keeps its catch on the line so the reel-spin, rise or pass-through can use it. */
	public static boolean holdsTether(VesperWeaponAction finished) {
		return finished == VesperWeaponAction.SICKLE_HOOK_REEL;
	}

	/** For a hook throw, a catch is the landing the string branches on, whether or not the close cut connected. */
	public static boolean landed(VesperWeaponAction finished, boolean hit, boolean hookCaught) {
		return hit || holdsTether(finished) && hookCaught;
	}
}
