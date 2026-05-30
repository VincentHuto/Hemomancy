package com.vincenthuto.hemomancy.common.item.harbinger.tool.living;

public final class LivingStaffFocusRules {
	private static final int BARE_ABSORPTION_TARGET_CAP = 1;
	private static final int STAFF_BASE_ABSORPTION_TARGET_CAP = 2;
	private static final int STAFF_MAX_ABSORPTION_TARGET_CAP = 6;

	private static final double STAFF_BASE_ABSORPTION_RANGE = 6.0D;
	private static final double STAFF_ABSORPTION_RANGE_PER_CONDUIT_LEVEL = 1.5D;
	private static final double VESPER_ABSORPTION_RANGE_BONUS = 1.5D;

	private static final double STAFF_BASE_ABSORPTION_DAMAGE = 2.0D;
	private static final double STAFF_ABSORPTION_DAMAGE_PER_DRAW_LEVEL = 0.4D;
	private static final double VESPER_ABSORPTION_DAMAGE_BONUS = 0.3D;

	private static final int STAFF_BASE_ABSORPTION_PULSE_INTERVAL = 8;
	private static final int VESPER_ABSORPTION_PULSE_INTERVAL_BONUS = 1;

	private static final double BARE_STRUCTURE_PROJECTION_RATE = 5.0D;
	private static final double STAFF_STRUCTURE_PROJECTION_RATE = 8.0D;
	private static final double STAFF_STRUCTURE_PROJECTION_RATE_PER_LEVEL = 3.0D;
	private static final double VESPER_STRUCTURE_PROJECTION_RATE_BONUS = 3.0D;

	private static final double BARE_TILE_PROJECTION_RATE = 100.0D;
	private static final double STAFF_TILE_PROJECTION_RATE = 150.0D;
	private static final double STAFF_TILE_PROJECTION_RATE_PER_LEVEL = 75.0D;
	private static final double VESPER_TILE_PROJECTION_RATE_BONUS = 75.0D;

	private LivingStaffFocusRules() {
	}

	public static int absorptionTargetCap(boolean usingLivingStaff, LivingStaffFocusProfile focus) {
		if (!usingLivingStaff) {
			return BARE_ABSORPTION_TARGET_CAP;
		}
		LivingStaffFocusProfile safeFocus = safeFocus(focus);
		int cap = STAFF_BASE_ABSORPTION_TARGET_CAP + safeFocus.livingConduitLevel();
		if (safeFocus.vesperMemoryAwakened()) {
			cap++;
		}
		return Math.min(STAFF_MAX_ABSORPTION_TARGET_CAP, cap);
	}

	public static double absorptionRange(LivingStaffFocusProfile focus) {
		LivingStaffFocusProfile safeFocus = safeFocus(focus);
		double range = STAFF_BASE_ABSORPTION_RANGE
				+ safeFocus.livingConduitLevel() * STAFF_ABSORPTION_RANGE_PER_CONDUIT_LEVEL;
		if (safeFocus.vesperMemoryAwakened()) {
			range += VESPER_ABSORPTION_RANGE_BONUS;
		}
		return range;
	}

	public static double absorptionDamagePerTarget(LivingStaffFocusProfile focus) {
		LivingStaffFocusProfile safeFocus = safeFocus(focus);
		double damage = STAFF_BASE_ABSORPTION_DAMAGE
				+ safeFocus.vascularDrawLevel() * STAFF_ABSORPTION_DAMAGE_PER_DRAW_LEVEL;
		if (safeFocus.vesperMemoryAwakened()) {
			damage += VESPER_ABSORPTION_DAMAGE_BONUS;
		}
		return damage;
	}

	public static int absorptionPulseIntervalTicks(LivingStaffFocusProfile focus) {
		LivingStaffFocusProfile safeFocus = safeFocus(focus);
		int interval = STAFF_BASE_ABSORPTION_PULSE_INTERVAL - safeFocus.vascularDrawLevel();
		if (safeFocus.vesperMemoryAwakened()) {
			interval -= VESPER_ABSORPTION_PULSE_INTERVAL_BONUS;
		}
		return Math.max(4, interval);
	}

	public static double structureProjectionRate(boolean usingLivingStaff, LivingStaffFocusProfile focus) {
		if (!usingLivingStaff) {
			return BARE_STRUCTURE_PROJECTION_RATE;
		}
		LivingStaffFocusProfile safeFocus = safeFocus(focus);
		double rate = STAFF_STRUCTURE_PROJECTION_RATE
				+ safeFocus.crimsonProjectionLevel() * STAFF_STRUCTURE_PROJECTION_RATE_PER_LEVEL;
		if (safeFocus.vesperMemoryAwakened()) {
			rate += VESPER_STRUCTURE_PROJECTION_RATE_BONUS;
		}
		return rate;
	}

	public static double bloodTileProjectionRate(boolean usingLivingStaff, LivingStaffFocusProfile focus) {
		if (!usingLivingStaff) {
			return BARE_TILE_PROJECTION_RATE;
		}
		LivingStaffFocusProfile safeFocus = safeFocus(focus);
		double rate = STAFF_TILE_PROJECTION_RATE
				+ safeFocus.crimsonProjectionLevel() * STAFF_TILE_PROJECTION_RATE_PER_LEVEL;
		if (safeFocus.vesperMemoryAwakened()) {
			rate += VESPER_TILE_PROJECTION_RATE_BONUS;
		}
		return rate;
	}

	private static LivingStaffFocusProfile safeFocus(LivingStaffFocusProfile focus) {
		return focus == null ? LivingStaffFocusProfile.NONE : focus;
	}
}
