package com.vincenthuto.hemomancy.recipe;

import com.vincenthuto.hutoslib.client.render.block.MultiblockPattern;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.pattern.BlockPattern;

public class RecipeBaseBloodCrafting {

	float cost;
	Item heldItem;
	Block hitBlock;
	Item creation;
	MultiblockPattern pattern;

	public RecipeBaseBloodCrafting(Item creation, float cost, Item heldItem, Block hitBlock,
			MultiblockPattern pattern) {
		this.cost = cost;
		this.heldItem = heldItem;
		this.hitBlock = hitBlock;
		this.pattern = pattern;
		this.creation = creation;
	}

	public RecipeBaseBloodCrafting(Block creation, float cost, Item heldItem, Block hitBlock,
			MultiblockPattern pattern) {
		this.cost = cost;
		this.heldItem = heldItem;
		this.hitBlock = hitBlock;
		this.pattern = pattern;
		this.creation = creation.asItem();
	}

	public Item getHeldItem() {
		return heldItem;
	}

	public float getCost() {
		return cost;
	}

	public Block getHitBlock() {
		return hitBlock;
	}

	public MultiblockPattern getMultiblockPattern() {
		return pattern;
	}

	public BlockPattern geBlockPattern() {
		return pattern.getBlockPattern();
	}

	public Item getCreation() {
		return creation;
	}

}
