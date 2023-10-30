package com.vincenthuto.hemomancy.common.block;

import com.vincenthuto.hemomancy.common.registry.PotionInit;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.level.block.FlowerBlock;

public class InfectedFungusBlock extends FlowerBlock {

	public InfectedFungusBlock(MobEffect effect, int effectDuration, Properties properties) {
		super(effect, effectDuration, properties);
	}

	@Override
	public MobEffect getSuspiciousEffect() {
		return PotionInit.blood_loss.get();
	}
}
