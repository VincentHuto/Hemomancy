package com.vincenthuto.hemomancy.common.item.rune;

import com.vincenthuto.hemomancy.common.capability.player.kinship.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.rune.RuneType;

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