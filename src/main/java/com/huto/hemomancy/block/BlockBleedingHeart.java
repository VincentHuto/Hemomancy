package com.huto.hemomancy.block;

import com.huto.hemomancy.init.PotionInit;

import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.effect.MobEffect;

import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

public class BlockBleedingHeart extends FlowerBlock {

	public BlockBleedingHeart(MobEffect effect, int effectDuration, Properties properties) {
		super(effect, effectDuration, properties);
	}

	@Override
	public MobEffect getStewEffect() {
		return PotionInit.blood_rush.get();
	}
}
