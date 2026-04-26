package com.vincenthuto.hemomancy.common.item.tool;

import com.vincenthuto.hemomancy.common.item.EnumBloodGourdTiers;

public class HemorathRibItem extends BloodGourdItem {

	public HemorathRibItem(EnumBloodGourdTiers tierIn) {
		super(new Properties().stacksTo(1).durability(4), tierIn);

	}



}
