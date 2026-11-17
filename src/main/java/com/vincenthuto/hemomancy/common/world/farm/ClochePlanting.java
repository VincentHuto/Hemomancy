package com.vincenthuto.hemomancy.common.world.farm;

import com.vincenthuto.hemomancy.mixin.core.StemBlockAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Tier 3 of fed ground (core-loop Track 7): what the Mycelial Lantern's cloche grows when seeds sit in its culture
 * slot instead of spores. Any crop block (vanilla or another mod's {@link CropBlock}) and any stem grows there, one
 * stage every {@link BloodFedGrowthRules#CLOCHE_STAGE_TICKS} for {@link BloodFedGrowthRules#CLOCHE_STAGE_ML}. A ripe
 * crop is harvested by its own loot table and starts again; a ripe stem keeps fruiting.
 */
public final class ClochePlanting {
	private ClochePlanting() {
	}

	/** The plant {@code stack} sets in the cloche, at its first stage, or null when it plants nothing. */
	@Nullable
	public static BlockState seedling(ItemStack stack) {
		if (stack.isEmpty() || !(stack.getItem() instanceof BlockItem item)) return null;
		Block block = item.getBlock();
		if (block instanceof CropBlock crop) return crop.getStateForAge(0);
		if (block instanceof StemBlock) return block.defaultBlockState().setValue(StemBlock.AGE, 0);
		return null;
	}

	public static boolean plantable(ItemStack stack) {
		return seedling(stack) != null;
	}

	/** The plant's last stage. */
	public static int lastStage(BlockState seedling) {
		if (seedling.getBlock() instanceof CropBlock crop) return crop.getMaxAge();
		return StemBlock.MAX_AGE;
	}

	/** The plant at {@code stage}, clamped to its stages. */
	public static BlockState atStage(BlockState seedling, int stage) {
		int at = Math.max(0, Math.min(lastStage(seedling), stage));
		if (seedling.getBlock() instanceof CropBlock crop) return crop.getStateForAge(at);
		return seedling.setValue(StemBlock.AGE, at);
	}

	/** Whether a stem stays standing and fruits again once ripe, rather than being cut and resown. */
	public static boolean keepsFruiting(BlockState seedling) {
		return seedling.getBlock() instanceof StemBlock;
	}

	/**
	 * What one ripe plant gives: a crop's own drops for its ripe state, less the one seed it was sown from (the seed in
	 * the slot is not used up); a stem's fruit. Only the first kind of item fits the output, so the harvest is that
	 * kind: the crop itself where its drops include more than the seed.
	 */
	public static ItemStack harvest(ServerLevel level, BlockPos pos, BlockState ripe, ItemStack sown) {
		if (ripe.getBlock() instanceof StemBlock stem) {
			Block fruit = BuiltInRegistries.BLOCK.get(((StemBlockAccessor) stem).hemomancy$fruit());
			return fruit == null ? ItemStack.EMPTY : new ItemStack(fruit);
		}
		List<ItemStack> drops = Block.getDrops(ripe, level, pos, null);
		ItemStack crop = ItemStack.EMPTY;
		int seeds = 0;
		for (ItemStack drop : drops) {
			if (ItemStack.isSameItem(drop, sown)) {
				seeds += drop.getCount();
			} else if (crop.isEmpty()) {
				crop = drop.copy();
			} else if (ItemStack.isSameItemSameComponents(crop, drop)) {
				crop.grow(drop.getCount());
			}
		}
		if (!crop.isEmpty()) return crop;
		// A crop that is its own seed (a carrot, a potato): keep one back as the next sowing.
		return seeds > 1 ? sown.copyWithCount(seeds - 1) : ItemStack.EMPTY;
	}
}
