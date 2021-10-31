package com.vincenthuto.hemomancy.recipe;

import com.vincenthuto.hutoslib.client.render.block.render.LabeledBlockPattern;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class RecipeSacrificialBloodCrafting extends RecipeBaseBloodCrafting {

	private float minHp;

	public RecipeSacrificialBloodCrafting(Item creation, float cost, Item heldItem, Block hitBlock,
			LabeledBlockPattern pattern, float minHp) {
		super(creation, cost, heldItem, hitBlock, pattern);
		this.minHp = minHp;
	}

	public RecipeSacrificialBloodCrafting(Block creation, float cost, Item heldItem, Block hitBlock,
			LabeledBlockPattern pattern, float minHp) {
		super(creation, cost, heldItem, hitBlock, pattern);
		this.minHp = minHp;
	}

	public float getMinHp() {
		return minHp;
	}

	public void setMinHp(float minHp) {
		this.minHp = minHp;
	}

}
