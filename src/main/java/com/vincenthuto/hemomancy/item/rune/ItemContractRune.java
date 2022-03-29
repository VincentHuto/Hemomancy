package com.vincenthuto.hemomancy.item.rune;

import com.vincenthuto.hemomancy.capa.player.rune.RuneType;
import com.vincenthuto.hemomancy.capa.player.tendency.EnumBloodTendency;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;

public class ItemContractRune extends ItemRune {

	public ItemContractRune(Properties properties, EnumBloodTendency tendencyIn, float deepenAmount) {
		super(properties, tendencyIn, deepenAmount);

	}

	@Override
	public Rarity getRarity(ItemStack stack) {
		return Rarity.RARE;
	}

	@Override
	public boolean isFoil(ItemStack stack) {

		return true;
	}

	@Override
	public RuneType getRuneType() {
		return RuneType.CONTRACT;
	}

}