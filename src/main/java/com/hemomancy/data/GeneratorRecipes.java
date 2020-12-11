package com.hemomancy.data;

import java.util.function.Consumer;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.data.RecipeProvider;

	public class GeneratorRecipes extends RecipeProvider {
		public GeneratorRecipes(DataGenerator generator) {
			super(generator);
		}
		@Override
		protected void registerRecipes(Consumer<IFinishedRecipe> consumer) {

}
	}
