package com.huto.hemomancy.item.tool;

import java.util.Set;

import com.google.common.collect.Sets;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.IItemTier;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolItem;

public class ItemKnapper extends ToolItem {
	private static final Set<Block> EFFECTIVE_ON = Sets.newHashSet(Blocks.OBSIDIAN);
	private float speed;

	public ItemKnapper(float speedIn, float attackDamageIn, float attackSpeedIn, IItemTier tier, Properties builderIn) {
		super(attackDamageIn, -2.8f, tier, EFFECTIVE_ON, builderIn);
		this.speed = speedIn;
	}

	@Override
	public boolean canHarvestBlock(BlockState blockIn) {
		if (EFFECTIVE_ON.contains(blockIn.getBlock())) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public float getDestroySpeed(ItemStack stack, BlockState state) {
		if (EFFECTIVE_ON.contains(state.getBlock())) {
			return speed;
		} else {
			return 0.5f;
		}
	}

}
