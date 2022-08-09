package com.vincenthuto.hemomancy.block;

import com.vincenthuto.hemomancy.init.PotionInit;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.level.block.FlowerBlock;

public class BlockInfectedFungus extends FlowerBlock {

	public BlockInfectedFungus(MobEffect effect, int effectDuration, Properties properties) {
		super(effect, effectDuration, properties);
	}

	@Override
	public MobEffect getSuspiciousStewEffect() {
		return PotionInit.blood_loss.get();
	}
}
