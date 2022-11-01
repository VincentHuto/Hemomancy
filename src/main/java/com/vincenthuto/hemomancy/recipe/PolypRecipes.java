package com.vincenthuto.hemomancy.recipe;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.vincenthuto.hemomancy.init.ItemInit;

import net.minecraft.world.item.Items;

public class PolypRecipes {

	public static List<RecipePolyp> POLYPRECIPES = new ArrayList<>();
	static RecipePolyp fungalPolyp, pestPolyp, serpentPolyp, leechPolyp, chitinitePolyp;

	public static void initRecipes() {
		fungalPolyp = registerRecipe(
				new RecipePolyp(Arrays.asList(Items.RED_MUSHROOM, Items.BROWN_MUSHROOM, Items.ROTTEN_FLESH),
						ItemInit.morphling_fungal.get()));
		pestPolyp = registerRecipe(new RecipePolyp(Arrays.asList(Items.BEETROOT_SEEDS, Items.ROTTEN_FLESH),
				ItemInit.morphling_pests.get()));
		serpentPolyp = registerRecipe(
				new RecipePolyp(Arrays.asList(ItemInit.serpent_scale.get(), Items.ROTTEN_FLESH),
						ItemInit.morphling_serpent.get()));
		leechPolyp = registerRecipe(
				new RecipePolyp(Arrays.asList(ItemInit.swollen_leech.get(), Items.ROTTEN_FLESH),
						ItemInit.morphling_leeches.get()));
		chitinitePolyp = registerRecipe(new RecipePolyp(
				Arrays.asList(Items.SPIDER_EYE, ItemInit.chitinous_husk.get(), Items.ROTTEN_FLESH),
				ItemInit.morphling_chitinite.get()));

	}

	public static RecipePolyp registerRecipe(RecipePolyp recipe) {
		POLYPRECIPES.add(recipe);
		return recipe;
	}

}
