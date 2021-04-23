package com.huto.hemomancy.recipe;

import net.minecraft.block.Block;
import net.minecraft.item.Item;

public class BaseBloodCraftingRecipe {

	float cost;
	Item heldItem;
	Block hitBlock;
	Item creation;
	BloodCraftingBundledPattern pattern;

	public BaseBloodCraftingRecipe(Item creation, float cost, Item heldItem, Block hitBlock,
			BloodCraftingBundledPattern pattern) {
		this.cost = cost;
		this.heldItem = heldItem;
		this.hitBlock = hitBlock;
		this.pattern = pattern;
		this.creation = creation;
	}

	public BaseBloodCraftingRecipe(Block creation, float cost, Item heldItem, Block hitBlock,
			BloodCraftingBundledPattern pattern) {
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

	public BloodCraftingBundledPattern getBundledPattern() {
		return pattern;
	}

	public Item getCreation() {
		return creation;
	}

}
