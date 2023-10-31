package com.vincenthuto.hemomancy.common.registry;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.recipe.BloodStructureRecipe;
import com.vincenthuto.hemomancy.common.recipe.ChiselRecipe;
import com.vincenthuto.hemomancy.common.recipe.CopyBloodGourdRecipe;
import com.vincenthuto.hemomancy.common.recipe.CopyMorphlingJarRecipe;
import com.vincenthuto.hemomancy.common.recipe.FillBloodGourdRecipe;
import com.vincenthuto.hemomancy.common.recipe.JuiceinatorRecipe;
import com.vincenthuto.hemomancy.common.recipe.RecallerRecipe;
import com.vincenthuto.hemomancy.common.recipe.serializer.BloodStructureRecipeSerializer;
import com.vincenthuto.hemomancy.common.recipe.serializer.ChiselRecipeSerializer;
import com.vincenthuto.hemomancy.common.recipe.serializer.JuiceinatorSerializer;
import com.vincenthuto.hemomancy.common.recipe.serializer.RecallerRecipeSerializer;

import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod.EventBusSubscriber(modid = Hemomancy.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class RecipeInit {
	public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS = DeferredRegister
			.create(ForgeRegistries.RECIPE_SERIALIZERS, Hemomancy.MOD_ID);

	public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES = DeferredRegister
			.create(ForgeRegistries.RECIPE_TYPES, Hemomancy.MOD_ID);

	// Types

	public static final RegistryObject<RecipeType<ChiselRecipe>> chisel_recipe = RECIPE_TYPES.register("chisel_recipse",
			() -> RecipeType.simple(Hemomancy.rloc("chisel_recipe")));

	public static final RegistryObject<RecipeType<JuiceinatorRecipe>> juiceinator_recipe_type = RECIPE_TYPES
			.register("juiceinator_recipe_type", () -> RecipeType.simple(Hemomancy.rloc("juiceinator")));

	public static final RegistryObject<RecipeType<RecallerRecipe>> recaller_recipe_type = RECIPE_TYPES
			.register("recaller_recipe_type", () -> RecipeType.simple(Hemomancy.rloc("recaller_recipe")));

	public static final RegistryObject<RecipeType<BloodStructureRecipe>> blood_structure_recipe_type = RECIPE_TYPES
			.register("blood_structure_recipe", () -> RecipeType.simple(Hemomancy.rloc("blood_structure_recipe")));

	// Serialize
	public static final RegistryObject<RecipeSerializer<?>> juiceinator_serializer = SERIALIZERS.register("juiceinator",
			JuiceinatorSerializer::new);

	public static final RegistryObject<RecipeSerializer<?>> morphling_jar_upgrade_serializer = SERIALIZERS
			.register("morphling_jar_upgrade", CopyMorphlingJarRecipe.Serializer::new);

	public static final RegistryObject<RecipeSerializer<?>> blood_gourd_upgrade_serializer = SERIALIZERS
			.register("blood_gourd_upgrade", CopyBloodGourdRecipe.Serializer::new);

	public static final RegistryObject<RecipeSerializer<?>> blood_gourd_fill_serializer = SERIALIZERS
			.register("blood_gourd_fill", FillBloodGourdRecipe.Serializer::new);

	public static final RegistryObject<RecipeSerializer<?>> recaller_recipe_serializer = SERIALIZERS
			.register("recaller_recipe", RecallerRecipeSerializer::new);

	public static final RegistryObject<RecipeSerializer<?>> blood_structure_recipe_serializer = SERIALIZERS
			.register("blood_structure_recipe", BloodStructureRecipeSerializer::new);

	public static final RegistryObject<RecipeSerializer<?>> chisel_recipe_serializer = SERIALIZERS
			.register("chisel_recipe", ChiselRecipeSerializer::new);

}
