package com.vincenthuto.hemomancy.init;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.recipe.JuiceinatorDataRecipe;
import com.vincenthuto.hemomancy.recipe.JuiceinatorSerializer;

import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.fmllegacy.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class RecipeTypeInit {
	public static final DeferredRegister<RecipeSerializer<?>> RECIPES = DeferredRegister
			.create(ForgeRegistries.RECIPE_SERIALIZERS, Hemomancy.MOD_ID);
	
	public static final RecipeType<JuiceinatorDataRecipe> juiceinator_recipe_type = RecipeType
			.register(Hemomancy.MOD_ID + ":juiceinator");

	public static final RegistryObject<RecipeSerializer<?>> juiceinator_serializer = RECIPES.register("juiceinator",
			JuiceinatorSerializer::new);
}
