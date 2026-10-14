package com.vincenthuto.hemomancy.common.block.harbinger.rite;

import net.minecraft.world.level.block.state.BlockBehaviour;

/** Smouldering ash: a faintly glowing trail that, like redstone dust, needs ground beneath it. */
public class SmoulderingAshTrailBlock extends AshTrailBlock {
	public SmoulderingAshTrailBlock(BlockBehaviour.Properties properties) {
		super(properties.lightLevel(state -> 4));
	}
}
