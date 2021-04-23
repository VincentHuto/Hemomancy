package com.huto.hemomancy.recipe;

import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;

public class BlockPosBlockPair {
	Block block;
	BlockPos pos;

	public BlockPosBlockPair(Block block, BlockPos pos) {
		this.block = block;
		this.pos = pos;
	}

	public Block getBlock() {
		return block;
	}

	public BlockPos getPos() {
		return pos;
	}

	@Override
	public String toString() {
		return block.toString() + "," + pos.toString();
	}
}