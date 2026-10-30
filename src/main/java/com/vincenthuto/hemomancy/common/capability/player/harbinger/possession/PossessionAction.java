package com.vincenthuto.hemomancy.common.capability.player.harbinger.possession;

import javax.annotation.Nullable;
import java.util.Locale;

/**
 * What a possessing will can do from the possession wheel: the visit's controls (inner ring) and the borrowed body's
 * gifts (outer ring). Which form offers which, and how each behaves, is {@link PossessionWheelRules}'s call.
 */
public enum PossessionAction {
	RETURN,
	STILL,
	ECHOLOCATE,
	STEP,
	SPIT,
	SLAM,
	LEAP,
	LUNGE,
	CLIMB,
	HOVER;

	private static final PossessionAction[] VALUES = values();

	/** The lang suffix and icon name: {@code possession_action.hemomancy.<id>}. */
	public String id() {
		return name().toLowerCase(Locale.ROOT);
	}

	@Nullable
	public static PossessionAction byOrdinal(int ordinal) {
		return ordinal >= 0 && ordinal < VALUES.length ? VALUES[ordinal] : null;
	}
}
