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
		fungalPolyp = new PolypRecipe(
				Arrays.asList(Items.RED_MUSHROOM, Items.BROWN_MUSHROOM, ItemInit.living_will.get()),
				ItemInit.morphling_fungal.get());
		POLYPRECIPES.add(fungalPolyp);
		pestPolyp = new PolypRecipe(Arrays.asList(Items.BEETROOT_SEEDS, ItemInit.living_will.get()),
				ItemInit.morphling_pests.get());
		POLYPRECIPES.add(pestPolyp);
		serpentPolyp = new PolypRecipe(Arrays.asList(ItemInit.serpent_scale.get(), ItemInit.living_will.get()),
				ItemInit.morphling_serpent.get());
		POLYPRECIPES.add(serpentPolyp);
		leechPolyp = new PolypRecipe(Arrays.asList(ItemInit.swollen_leech.get(), ItemInit.living_will.get()),
				ItemInit.morphling_leeches.get());
		POLYPRECIPES.add(leechPolyp);
		symbiotePolyp = new PolypRecipe(
				Arrays.asList(Items.SPIDER_EYE, ItemInit.chitinous_husk.get(), ItemInit.living_will.get()),
				ItemInit.morphling_symbiote.get());
		POLYPRECIPES.add(symbiotePolyp);

	}

}
