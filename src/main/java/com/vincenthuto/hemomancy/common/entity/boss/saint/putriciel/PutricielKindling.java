package com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel;

import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintMeter;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintTrialEntity;

import javax.annotation.Nullable;

/**
 * The Kindling as the kit's Saint-owned meter: its maximum scales with the challengers and its drift (creep, hold,
 * burn-down, the Communion's drain) comes from Putriciel's live stage through {@link PutricielKindlingRules}. The
 * thresholds (full, the two Offerings, the tremor) are read by the Saint each tick, not by meter callbacks.
 */
public final class PutricielKindling implements SaintMeter.Rule {
	@Override
	public float max(int challengers) {
		return PutricielKindlingRules.max(challengers);
	}

	@Override
	public float drift(@Nullable SaintTrialEntity saint, float value, float max) {
		return saint instanceof PutricielEntity putriciel ? putriciel.kindlingDrift() : 0.0F;
	}
}
