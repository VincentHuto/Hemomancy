package com.huto.hemomancy.item.rune;

import com.huto.hemomancy.capa.rune.IRune;
import com.huto.hemomancy.capa.rune.RuneType;
import com.huto.hemomancy.capa.tendency.EnumBloodTendency;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;

import net.minecraft.world.item.Item.Properties;

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
