package com.vincenthuto.hemomancy.common.recipe;

import java.util.List;

import net.minecraft.world.item.Item;

public class RecipePolyp {
	List<Item> ingr;
	Item output;

	public RecipePolyp(List<Item> ingrIn, Item outputIn) {
		this.ingr = ingrIn;
		this.output = outputIn;
	}

	public List<Item> getIngr() {
		return ingr;
	}

	public Item getOutput() {
		return output;
	}
}
