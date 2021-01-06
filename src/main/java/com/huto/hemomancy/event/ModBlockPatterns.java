package com.huto.hemomancy.event;

import net.minecraft.block.Blocks;
import net.minecraft.block.pattern.BlockPattern;
import net.minecraft.block.pattern.BlockPatternBuilder;
import net.minecraft.block.pattern.BlockStateMatcher;
import net.minecraft.util.CachedBlockInfo;

public class ModBlockPatterns {


	public static BlockPattern getBookPatten() {
		 BlockPattern bookPatten = null;
		if (bookPatten == null) {
			bookPatten= BlockPatternBuilder.start()
					.aisle("RRR").where('R', CachedBlockInfo.hasState(BlockStateMatcher.forBlock(Blocks.REDSTONE_WIRE)))
					.aisle("RBR").where('B', CachedBlockInfo.hasState(BlockStateMatcher.forBlock(Blocks.BOOKSHELF))).where('R', CachedBlockInfo.hasState(BlockStateMatcher.forBlock(Blocks.REDSTONE_WIRE)))
					.aisle("RRR").where('R', CachedBlockInfo.hasState(BlockStateMatcher.forBlock(Blocks.REDSTONE_WIRE)))
					.build();
		}
		return bookPatten;
	}

}
