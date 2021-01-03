package com.huto.hemomancy.item.runes;

import com.huto.hemomancy.capabilities.mindrunes.IRune;
import com.huto.hemomancy.capabilities.mindrunes.RuneType;
import com.huto.hemomancy.capabilities.tendency.EnumBloodTendency;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Rarity;

public class ItemContractRune extends ItemRune implements IRune {

	public ItemContractRune(Properties properties, EnumBloodTendency tendencyIn, float deepenAmount) {
		super(properties, tendencyIn, deepenAmount);

	}

	@Override
	public Rarity getRarity(ItemStack stack) {
		return Rarity.RARE;
	}

	@Override
	public boolean hasEffect(ItemStack stack) {

		return true;
	}

	@Override
	public RuneType getRuneType() {
		return RuneType.CONTRACT;
	}

}
