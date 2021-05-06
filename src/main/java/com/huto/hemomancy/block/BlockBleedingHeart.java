package com.huto.hemomancy.block;

import com.huto.hemomancy.init.PotionInit;

import net.minecraft.block.FlowerBlock;
import net.minecraft.potion.Effect;

public class BlockBleedingHeart extends FlowerBlock {

	public BlockBleedingHeart(Effect effect, int effectDuration, Properties properties) {
		super(effect, effectDuration, properties);
	}

	@Override
	public Effect getStewEffect() {
		return PotionInit.blood_rush.get();
	}
}
