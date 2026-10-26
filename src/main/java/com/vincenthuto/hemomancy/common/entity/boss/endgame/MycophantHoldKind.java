package com.vincenthuto.hemomancy.common.entity.boss.endgame;

import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

/**
 * What she holds a player with. Only the embrace's sac hold has the meter and can be torn open; the Claw Pin and the
 * Last Brood's cocoon simply run out. The ordinal is synced beside the claimed id so the held client pins itself to
 * the right point.
 */
public enum MycophantHoldKind {
	/** The Rushing and Fusion Embrace: pressed against the sac, metered, broken by hits or a cast. */
	SAC,
	/** The Claw Pin: under the claw tip, on the floor. */
	PIN,
	/** The Last Brood: sealed against the sac. */
	COCOON;

	public boolean metered() {
		return this == SAC;
	}

	public boolean breakable() {
		return this == SAC;
	}

	/** How long the hold lasts before it lets go by itself; -1 for the sac, which runs until the meter fills or it breaks. */
	public int durationTicks() {
		return switch (this) {
			case SAC -> -1;
			case PIN -> 20;
			case COCOON -> MycophantLastBroodRules.COCOON;
		};
	}

	/** True when {@code attack} is a move that holds with this kind; a hold outliving its move is let go. */
	public boolean heldThrough(MycophantAttack attack) {
		return switch (this) {
			case SAC -> attack.isEmbrace();
			case PIN -> attack == MycophantAttack.CLAW_PIN;
			case COCOON -> attack == MycophantAttack.LAST_BROOD;
		};
	}

	/**
	 * Where a player held this way is kept, for her feet at {@code feet} facing {@code yawDegrees}. The sac and the
	 * cocoon share the sac's front; the pin is under the claw tip, at floor height.
	 */
	public Vec3 point(Vec3 feet, float yawDegrees) {
		float yaw = yawDegrees * Mth.DEG_TO_RAD;
		double fx = -Mth.sin(yaw);
		double fz = Mth.cos(yaw);
		if (this == PIN) {
			// under her right front claw, the one that rakes and pins; right is (-fz, fx), as MycophantEntity.bodyPoint has it
			double right = 1.6D;
			double forward = 3.6D;
			return feet.add(-fz * right + fx * forward, 0.0D, fx * right + fz * forward);
		}
		return feet.add(fx * 2.7D, 0.55D, fz * 2.7D);
	}

	/** A hold ends when the held player is gone (dead, logged out, another dimension), or a timed kind has run out. */
	public static boolean shouldRelease(boolean heldPresent, int heldTicks, MycophantHoldKind kind) {
		if (!heldPresent) return true;
		int duration = kind.durationTicks();
		return duration >= 0 && heldTicks >= duration;
	}

	public static MycophantHoldKind byOrdinal(int ordinal) {
		MycophantHoldKind[] values = values();
		return ordinal >= 0 && ordinal < values.length ? values[ordinal] : SAC;
	}
}
