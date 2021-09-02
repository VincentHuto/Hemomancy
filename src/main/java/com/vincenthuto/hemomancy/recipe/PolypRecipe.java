package com.vincenthuto.hemomancy.recipe;

import java.util.List;

import net.minecraft.world.item.Item;

public class PolypRecipe {
	List<Item> ingr;
	Item output;

	public PolypRecipe(List<Item> ingrIn, Item outputIn) {
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