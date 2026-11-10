package com.vincenthuto.hemomancy.common.item.harbinger.tool.living;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.EnumBloodTemper;

import javax.annotation.Nullable;

public interface ITemperAlignedWeapon {
	@Nullable
	EnumBloodTemper hemomancy$getWeaponTemper();
}

