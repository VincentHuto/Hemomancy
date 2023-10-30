package com.vincenthuto.hemomancy.common.data;

import java.util.Map;
import java.util.stream.Collectors;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.registry.BlockInit;

import net.minecraft.data.loot.packs.VanillaBlockLoot;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;

public class HemoBlockLootTableProvider extends VanillaBlockLoot {

    @Override
    protected void generate() {
    	for(Block reg : BlockInit.getAllBlockEntries()) {
    		dropSelf(reg);
    	}
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return ForgeRegistries.BLOCKS.getEntries().stream()
                .filter(e -> e.getKey().location().getNamespace().equals(Hemomancy.MOD_ID))
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());
    }
}
