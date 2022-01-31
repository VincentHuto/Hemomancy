package com.vincenthuto.hemomancy.init;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.recipe.CopyBloodGourdDataRecipe;
import com.vincenthuto.hemomancy.recipe.CopyMorphlingJarDataRecipe;
import com.vincenthuto.hemomancy.recipe.CopyRuneBinderDataRecipe;
import com.vincenthuto.hemomancy.recipe.EarthlyTransfuserDataRecipe;
import com.vincenthuto.hemomancy.recipe.EarthlyTransfuserSerializer;
import com.vincenthuto.hemomancy.recipe.FillBloodGourdDataRecipe;
import com.vincenthuto.hemomancy.recipe.JuiceinatorDataRecipe;
import com.vincenthuto.hemomancy.recipe.JuiceinatorSerializer;

import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class RecipeTypeInit {
	public static final DeferredRegister<RecipeSerializer<?>> RECIPES = DeferredRegister
			.create(ForgeRegistries.RECIPE_SERIALIZERS, Hemomancy.MOD_ID);

	public static final RecipeType<JuiceinatorDataRecipe> juiceinator_recipe_type = RecipeType
			.register(Hemomancy.MOD_ID + ":juiceinator");
	public static final RegistryObject<RecipeSerializer<?>> juiceinator_serializer = RECIPES.register("juiceinator",
			JuiceinatorSerializer::new);

	public static final RecipeType<EarthlyTransfuserDataRecipe> earthly_transfuser_recipe_type = RecipeType
			.register(Hemomancy.MOD_ID + ":earthly_transfuser");
	public static final RegistryObject<RecipeSerializer<?>> earthly_transfuser_serializer = RECIPES
			.register("earthly_transfuser", EarthlyTransfuserSerializer::new);

	public static final RegistryObject<RecipeSerializer<?>> morphling_jar_upgrade_serializer = RECIPES
			.register("morphling_jar_upgrade", CopyMorphlingJarDataRecipe.Serializer::new);
	public static final RegistryObject<RecipeSerializer<?>> rune_binder_upgrade_serializer = RECIPES
			.register("rune_binder_upgrade", CopyRuneBinderDataRecipe.Serializer::new);
	public static final RegistryObject<RecipeSerializer<?>> blood_gourd_upgrade_serializer = RECIPES
			.register("blood_gourd_upgrade", CopyBloodGourdDataRecipe.Serializer::new);
	public static final RegistryObject<RecipeSerializer<?>> blood_gourd_fill_serializer = RECIPES
			.register("blood_gourd_fill", FillBloodGourdDataRecipe.Serializer::new);

}
