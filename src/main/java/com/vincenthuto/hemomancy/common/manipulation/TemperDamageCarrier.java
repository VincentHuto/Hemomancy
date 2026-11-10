package com.vincenthuto.hemomancy.common.manipulation;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.EnumBloodTemper;

import javax.annotation.Nullable;

public interface TemperDamageCarrier {
	@Nullable
	EnumBloodTemper getDamageTemper();

	@Nullable
	EnumBloodTemper getSecondaryDamageTemper();
}
