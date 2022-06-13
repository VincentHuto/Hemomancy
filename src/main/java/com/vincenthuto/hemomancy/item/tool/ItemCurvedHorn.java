package com.vincenthuto.hemomancy.item.tool;

import com.vincenthuto.hemomancy.Hemomancy.HemomancyItemGroup;
import com.vincenthuto.hemomancy.item.EnumBloodGourdTiers;

import net.minecraft.world.item.Item;

public class ItemCurvedHorn extends ItemBloodGourd {

	public ItemCurvedHorn(EnumBloodGourdTiers tierIn) {
		super(new Item.Properties().tab(HemomancyItemGroup.instance).stacksTo(1).durability(3), tierIn);

	}
	
	

}
