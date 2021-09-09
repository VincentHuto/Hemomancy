package com.vincenthuto.hemomancy.recipe;

import com.vincenthuto.hutoslib.client.render.block.render.LabeledBlockPattern;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class BaseBloodCraftingRecipe {

	float cost;
	Item heldItem;
	Block hitBlock;
	Item creation;
	LabeledBlockPattern pattern;

	public BaseBloodCraftingRecipe(Item creation, float cost, Item heldItem, Block hitBlock,
			LabeledBlockPattern pattern) {
		this.cost = cost;
		this.heldItem = heldItem;
		this.hitBlock = hitBlock;
		this.pattern = pattern;
		this.creation = creation;
	}

	public BaseBloodCraftingRecipe(Block creation, float cost, Item heldItem, Block hitBlock,
			LabeledBlockPattern pattern) {
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

	public LabeledBlockPattern getBundledPattern() {
		return pattern;
	}

	public Item getCreation() {
		return creation;
	}

}
