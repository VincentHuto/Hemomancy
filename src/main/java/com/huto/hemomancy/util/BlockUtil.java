package com.huto.hemomancy.util;

import net.minecraft.block.BeetrootBlock;
import net.minecraft.block.CropsBlock;
import net.minecraft.block.IGrowable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;

public class BlockUtil {

	public static boolean isCrop(IWorldReader worldIn, BlockPos blockPos) {
		if (worldIn.getBlockState(blockPos).getBlock() instanceof IGrowable) {
			if (worldIn.getBlockState(blockPos).getBlock() instanceof CropsBlock) {
				return true;
			}
		}
		return false;

	}

	public static boolean isCropFullyGrown(IWorldReader worldIn, BlockPos blockPos) {
		CropsBlock crop = (CropsBlock) worldIn.getBlockState(blockPos).getBlock();
		if (worldIn.getBlockState(blockPos).getProperties().contains(CropsBlock.AGE)) {
			if (worldIn.getBlockState(blockPos).get(CropsBlock.AGE) >= crop.getMaxAge()) {
				return true;
			}
		}
		return false;
	}

	public static boolean isBeetFullyGrown(IWorldReader worldIn, BlockPos blockPos) {
		CropsBlock crop = (CropsBlock) worldIn.getBlockState(blockPos).getBlock();
		if ((crop instanceof BeetrootBlock)) {
			BeetrootBlock beets = (BeetrootBlock) crop;
			if (worldIn.getBlockState(blockPos).getProperties().contains(BeetrootBlock.BEETROOT_AGE)) {
				if (worldIn.getBlockState(blockPos).get(BeetrootBlock.BEETROOT_AGE) == beets.getMaxAge()) {
					return true;
				}
			}
		}
		return false;

	}

}
