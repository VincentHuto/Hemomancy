package com.huto.hemomancy.recipes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.huto.hemomancy.init.ItemInit;

import net.minecraft.item.Items;

public class PolypRecipes {

	public static List<PolypRecipe> POLYPRECIPES = new ArrayList<>();
	static PolypRecipe fungalPolyp, pestPolyp, serpentPolyp, leechPolyp, symbiotePolyp;

	public static void initRecipes() {
		fungalPolyp = registerRecipe(
				new PolypRecipe(Arrays.asList(Items.RED_MUSHROOM, Items.BROWN_MUSHROOM, ItemInit.living_will.get()),
						ItemInit.morphling_fungal.get()));
		pestPolyp = registerRecipe(new PolypRecipe(Arrays.asList(Items.BEETROOT_SEEDS, ItemInit.living_will.get()),
				ItemInit.morphling_pests.get()));
		serpentPolyp = registerRecipe(
				new PolypRecipe(Arrays.asList(ItemInit.serpent_scale.get(), ItemInit.living_will.get()),
						ItemInit.morphling_serpent.get()));
		leechPolyp = registerRecipe(
				new PolypRecipe(Arrays.asList(ItemInit.swollen_leech.get(), ItemInit.living_will.get()),
						ItemInit.morphling_leeches.get()));
		symbiotePolyp = registerRecipe(new PolypRecipe(
				Arrays.asList(Items.SPIDER_EYE, ItemInit.chitinous_husk.get(), ItemInit.living_will.get()),
				ItemInit.morphling_symbiote.get()));

	}

	public static PolypRecipe registerRecipe(PolypRecipe recipe) {
		POLYPRECIPES.add(recipe);
		return recipe;
	}

}
