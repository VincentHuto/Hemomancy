package com.vincenthuto.hemomancy.item.tool;

import com.vincenthuto.hemomancy.item.EnumBloodGourdTiers;

import net.minecraft.world.item.Item;

public class CurvedHornItem extends BloodGourdItem {

	public CurvedHornItem(EnumBloodGourdTiers tierIn) {
		super(new Item.Properties().stacksTo(1).durability(3), tierIn);

	}



}
