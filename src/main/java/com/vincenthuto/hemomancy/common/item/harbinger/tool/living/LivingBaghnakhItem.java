package com.vincenthuto.hemomancy.common.item.harbinger.tool.living;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import net.minecraft.world.item.Tier;

public class LivingBaghnakhItem extends LivingToolItem {

	public LivingBaghnakhItem(float speedIn, float attackDamageIn, Tier tier, Properties builderIn) {
		super(speedIn, attackDamageIn, -1.8f, EnumBloodTendency.TENEBRIS, tier, builderIn);
	}

}
