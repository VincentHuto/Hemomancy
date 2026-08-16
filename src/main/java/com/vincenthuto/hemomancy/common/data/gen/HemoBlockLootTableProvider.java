package com.vincenthuto.hemomancy.common.data.gen;

import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.packs.VanillaBlockLoot;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

public class HemoBlockLootTableProvider extends VanillaBlockLoot {

	public HemoBlockLootTableProvider(HolderLookup.Provider provider) {
		super(provider);
	}

	@Override
	protected void generate() {
		for (Block reg : BlockInit.getAllBlockEntries()) {
			if (reg == BlockInit.bleeding_heart.get()) {
				add(reg, createSingleItemTableWithSilkTouch(reg, ItemInit.bleeding_bulb.get(),
						UniformGenerator.between(1.0F, 3.0F)));
			} else if (reg == BlockInit.potted_bleeding_heart.get()
					|| reg == BlockInit.potted_infected_fungus.get()
					|| reg == BlockInit.potted_stinkhorn_fungus.get()
					|| reg == BlockInit.potted_puffball_fungus.get()
					|| reg == BlockInit.potted_ghost_pipe.get()
					|| reg == BlockInit.potted_sarcodes.get()
					|| reg == BlockInit.potted_lethean_poppy.get()) {
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
