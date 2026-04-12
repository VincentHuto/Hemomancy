package com.vincenthuto.hemomancy.common.data.gen;

import com.vincenthuto.hemomancy.common.init.BlockInit;

import net.minecraft.data.loot.packs.VanillaBlockLoot;
import net.minecraft.world.level.block.Block;

public class HemoBlockLootTableProvider extends VanillaBlockLoot {

	@Override
	protected void generate() {
		for (Block reg : BlockInit.getAllBlockEntries()) {
			if (reg == BlockInit.potted_bleeding_heart.get() || reg == BlockInit.potted_infected_fungus.get()) {
				dropPottedContents(reg);
			} else {
				dropSelf(reg);
			}
		}
	}

	@Override
	protected Iterable<Block> getKnownBlocks() {
		return BlockInit.getAllBlockEntries();
	}
}
