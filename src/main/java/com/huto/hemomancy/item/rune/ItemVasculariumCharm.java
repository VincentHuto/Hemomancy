package com.huto.hemomancy.item.rune;

import com.huto.hemomancy.capa.rune.IRune;
import com.huto.hemomancy.capa.rune.RuneType;
import com.huto.hemomancy.capa.tendency.EnumBloodTendency;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Rarity;

public class ItemVasculariumCharm extends ItemRune implements IRune {

	public ItemVasculariumCharm(Properties properties, EnumBloodTendency tendencyIn, float deepenAmount) {
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
		return RuneType.VASC;
	}

}
