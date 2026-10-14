package com.vincenthuto.hemomancy.common.block.harbinger.rite;

import net.minecraft.world.level.block.state.BlockBehaviour;

/** Activated smouldering ash: glows like the inert trail, and breaks off and drops when its ground goes. */
public class ActiveSmoulderingAshTrailBlock extends AshTrailBlock {
	public ActiveSmoulderingAshTrailBlock(BlockBehaviour.Properties properties) {
		super(properties.lightLevel(state -> 4));
	}

	@Override
	protected boolean dropsWhenUnsupported() {
		return true;
	}
}
