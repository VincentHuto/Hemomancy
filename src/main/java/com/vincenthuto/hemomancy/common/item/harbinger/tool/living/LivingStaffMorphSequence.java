package com.vincenthuto.hemomancy.common.item.harbinger.tool.living;

import net.minecraft.util.Mth;

public final class LivingStaffMorphSequence {
	public static final int PHASE_TICKS = 8;

	private LivingStaffMorphSequence() {
	}

	public static int durationTicks(boolean hasOutgoing, boolean hasIncoming) {
		return (hasOutgoing ? PHASE_TICKS : 0) + (hasIncoming ? PHASE_TICKS : 0);
	}

	public static boolean hasChangedStack(boolean mainChanged, boolean mainPresent,
			boolean offChanged, boolean offPresent) {
		return mainChanged && mainPresent || offChanged && offPresent;
	}

	public static Phase phase(float elapsed, boolean hasOutgoing, boolean hasIncoming) {
		if (elapsed < 0.0F) elapsed = 0.0F;
		if (hasOutgoing && elapsed < PHASE_TICKS) return Phase.DISSOLVE;
		float formationStart = hasOutgoing ? PHASE_TICKS : 0.0F;
		if (hasIncoming && elapsed < formationStart + PHASE_TICKS) return Phase.FORM;
		return Phase.COMPLETE;
	}

	public static float phaseProgress(float elapsed, boolean hasOutgoing, boolean hasIncoming) {
		Phase phase = phase(elapsed, hasOutgoing, hasIncoming);
		if (phase == Phase.COMPLETE) return 1.0F;
		float phaseStart = phase == Phase.FORM && hasOutgoing ? PHASE_TICKS : 0.0F;
		return Mth.clamp((elapsed - phaseStart) / PHASE_TICKS, 0.0F, 1.0F);
	}

	public enum Phase {
		DISSOLVE,
		FORM,
		COMPLETE
	}
}
