package com.huto.hemomancy.manipulation.quick.conjure;

import com.huto.hemomancy.capa.tendency.EnumBloodTendency;
import com.huto.hemomancy.capa.vascular.EnumVeinSections;
import com.huto.hemomancy.init.ItemInit;
import com.huto.hemomancy.manipulation.EnumManipulationRank;

import net.minecraft.item.Item;

public class ManipConjureCrossbow extends ManipBaseConjuration {

	public ManipConjureCrossbow(String name, double cost, double alignLevel, EnumManipulationRank rank,
			EnumBloodTendency tendency, EnumVeinSections section) {
		super(name, cost, alignLevel, rank, tendency, section);
	}

	@Override
	public Item getItem() {
		return ItemInit.living_crossbow.get();
	}

}
