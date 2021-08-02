package com.huto.hemomancy.block;

import com.huto.hemomancy.init.PotionInit;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.level.block.FlowerBlock;

public class BlockBleedingHeart extends FlowerBlock {

	public BlockBleedingHeart(MobEffect effect, int effectDuration, Properties properties) {
		super(effect, effectDuration, properties);
	}

	@Override
	public MobEffect getSuspiciousStewEffect() {
		return PotionInit.blood_rush.get();
	}
}
