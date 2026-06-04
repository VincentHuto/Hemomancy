package com.vincenthuto.hemomancy.common.item.harbinger.tool.living;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;

import javax.annotation.Nullable;

public interface ITendencyAlignedWeapon {
	@Nullable
	EnumBloodTendency hemomancy$getWeaponTendency();
}

