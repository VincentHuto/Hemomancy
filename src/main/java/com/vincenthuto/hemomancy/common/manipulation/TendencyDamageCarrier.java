package com.vincenthuto.hemomancy.common.manipulation;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;

import javax.annotation.Nullable;

public interface TendencyDamageCarrier {
	@Nullable
	EnumBloodTendency getDamageTendency();

	@Nullable
	EnumBloodTendency getSecondaryDamageTendency();
}
