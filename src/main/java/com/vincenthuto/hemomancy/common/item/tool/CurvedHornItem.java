package com.vincenthuto.hemomancy.common.item.tool;

import com.vincenthuto.hemomancy.common.item.EnumBloodGourdTiers;

import net.minecraft.world.item.Item;

public class CurvedHornItem extends BloodGourdItem {

	public CurvedHornItem(EnumBloodGourdTiers tierIn) {
		super(new Item.Properties().stacksTo(1).durability(3), tierIn);

	}



}
