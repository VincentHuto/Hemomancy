package com.vincenthuto.hemomancy.common.entity.mob.monster.will;

import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.LivingStaffFocusProfile;
import net.minecraft.util.Mth;

public final class WillAbsorptionRules {
	public static final float DEFAULT_PROGRESS_REQUIRED = 100.0F;
	public static final int GRACE_TICKS = 20;
	public static final double ESCAPE_HEALTH_FRACTION = 0.4D;
	public static final int CONTROLLED_RAGE_TICKS = 160;

	private static final float BARE_PROGRESS_PER_TICK = 1.0F;
	private static final float STAFF_BASE_PROGRESS_PER_TICK = 1.5F;
	private static final float STAFF_PROGRESS_PER_DRAW_LEVEL = 0.10F;
	private static final float STAFF_PROGRESS_PER_FOCUS_LEVEL = 0.05F;
	private static final float VESPER_PROGRESS_BONUS = 0.15F;
	private static final float VESPER_REFUSAL_PROGRESS_BONUS = 0.05F;
	private static final float STAFF_MAX_PROGRESS_PER_TICK = 2.25F;

	private WillAbsorptionRules() {
	}

	public static float progressRequired(double configured) {
		return configured <= 0.0D ? DEFAULT_PROGRESS_REQUIRED : (float) Mth.clamp(configured, 1.0D, 10000.0D);
	}

	public static int graceTicks(int configured) {
		return configured <= 0 ? GRACE_TICKS : Math.max(1, configured);
	}

	public static int rageTicks(int configured) {
		return configured <= 0 ? CONTROLLED_RAGE_TICKS : Math.max(1, configured);
	}

	public static double escapeHealthFraction(double configured) {
		return configured <= 0.0D ? ESCAPE_HEALTH_FRACTION : Mth.clamp(configured, 0.0D, 1.0D);
	}

	public static float escapeHealthFloor(float maxHealth, double configuredFraction) {
		double fraction = escapeHealthFraction(configuredFraction);
		return Mth.clamp((float) (maxHealth * fraction), 1.0F, Math.max(1.0F, maxHealth));
	}

	public static int stageForProgress(float progress, float required) {
		float ratio = Mth.clamp(progress / Math.max(1.0F, required), 0.0F, 1.0F);
		if (ratio >= 0.67F) {
			return 2;
		}
		if (ratio >= 0.33F) {
			return 1;
		}
		return 0;
	}

	public static float bareProgressPerTick() {
		return BARE_PROGRESS_PER_TICK;
	}

	public static float staffProgressPerTick(LivingStaffFocusProfile focus) {
		LivingStaffFocusProfile safeFocus = focus == null ? LivingStaffFocusProfile.NONE : focus;
		float progress = STAFF_BASE_PROGRESS_PER_TICK
				+ safeFocus.vascularDrawLevel() * STAFF_PROGRESS_PER_DRAW_LEVEL
				+ safeFocus.hematicFocusLevel() * STAFF_PROGRESS_PER_FOCUS_LEVEL;
		if (safeFocus.vesperMemoryAwakened()) {
			progress += VESPER_PROGRESS_BONUS;
			progress += safeFocus.vespersRefusalLevel() * VESPER_REFUSAL_PROGRESS_BONUS;
		}
		return Mth.clamp(progress, STAFF_BASE_PROGRESS_PER_TICK, STAFF_MAX_PROGRESS_PER_TICK);
	}
}
