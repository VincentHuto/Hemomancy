package com.vincenthuto.hemomancy.block;

import com.vincenthuto.hemomancy.init.PotionInit;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.level.block.FlowerBlock;

public class BleedingHeartBlock extends FlowerBlock {

	public BleedingHeartBlock(MobEffect effect, int effectDuration, Properties properties) {
		super(effect, effectDuration, properties);
	}

	@Override
	public MobEffect getSuspiciousEffect() {
		return PotionInit.blood_rush.get();
	}
}
