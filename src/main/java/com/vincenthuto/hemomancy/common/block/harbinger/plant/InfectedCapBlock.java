package com.vincenthuto.hemomancy.common.block.harbinger.plant;

import net.minecraft.world.level.block.SnowyDirtBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

/** Infected cap ground; it never spreads or decays on its own. */
public class InfectedCapBlock extends SnowyDirtBlock {
	public InfectedCapBlock(BlockBehaviour.Properties pProperties) {
		super(pProperties);
	}
}
