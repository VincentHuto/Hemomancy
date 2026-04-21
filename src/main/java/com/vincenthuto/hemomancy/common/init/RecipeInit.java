package com.vincenthuto.hemomancy.common.init;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.recipe.BloodStructureRecipe;
import com.vincenthuto.hemomancy.common.recipe.CardinalRiteRecipe;
import com.vincenthuto.hemomancy.common.recipe.ScarRecipe;
import com.vincenthuto.hemomancy.common.recipe.CopyBloodGourdRecipe;
import com.vincenthuto.hemomancy.common.recipe.CopyMorphlingJarRecipe;
import com.vincenthuto.hemomancy.common.recipe.FillBloodGourdRecipe;
import com.vincenthuto.hemomancy.common.recipe.IncubatorRecipe;
import com.vincenthuto.hemomancy.common.recipe.DistillationRecipe;
import com.vincenthuto.hemomancy.common.recipe.MemoryWeavingRecipe;
import com.vincenthuto.hemomancy.common.recipe.serializer.BloodStructureRecipeSerializer;
import com.vincenthuto.hemomancy.common.recipe.serializer.CardinalRiteRecipeSerializer;
import com.vincenthuto.hemomancy.common.recipe.serializer.ScarRecipeSerializer;
import com.vincenthuto.hemomancy.common.recipe.serializer.IncubatorRecipeSerializer;
import com.vincenthuto.hemomancy.common.recipe.serializer.DistillationRecipeSerializer;
import com.vincenthuto.hemomancy.common.recipe.serializer.MemoryWeavingRecipeSerializer;

import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.ForgeRegistries;
import net.neoforged.neoforge.registries.RegistryObject;

@Mod.EventBusSubscriber(modid = Hemomancy.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class RecipeInit {
	public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS = DeferredRegister
			.create(ForgeRegistries.RECIPE_SERIALIZERS, Hemomancy.MOD_ID);

	public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES = DeferredRegister
			.create(ForgeRegistries.RECIPE_TYPES, Hemomancy.MOD_ID);

	// Types

	public static final RegistryObject<RecipeType<ScarRecipe>> chisel_recipe = RECIPE_TYPES.register("scar_recipe",
			() -> RecipeType.simple(Hemomancy.rloc("scar_recipe")));

	public static final RegistryObject<RecipeType<DistillationRecipe>> distillation_recipe_type = RECIPE_TYPES
			.register("distillation_recipe", () -> RecipeType.simple(Hemomancy.rloc("distillation_recipe")));

	public static final RegistryObject<RecipeType<MemoryWeavingRecipe>> memory_weaving_type = RECIPE_TYPES
			.register("memory_weaving_type", () -> RecipeType.simple(Hemomancy.rloc("memory_weaving")));

	public static final RegistryObject<RecipeType<BloodStructureRecipe>> blood_structure_recipe_type = RECIPE_TYPES
			.register("blood_structure_recipe", () -> RecipeType.simple(Hemomancy.rloc("blood_structure_recipe")));

	public static final RegistryObject<RecipeType<CardinalRiteRecipe>> cardinal_rite_recipe_type = RECIPE_TYPES
			.register("cardinal_rite_recipe", () -> RecipeType.simple(Hemomancy.rloc("cardinal_rite_recipe")));

	public static final RegistryObject<RecipeType<IncubatorRecipe>> incubator_recipe_type = RECIPE_TYPES
			.register("incubator", () -> RecipeType.simple(Hemomancy.rloc("incubator")));

	// Serialize
	public static final RegistryObject<RecipeSerializer<?>> distillation_recipe_serializer = SERIALIZERS.register("distillation_recipe",
			DistillationRecipeSerializer::new);

	public static final RegistryObject<RecipeSerializer<?>> morphling_jar_upgrade_serializer = SERIALIZERS
			.register("morphling_jar_upgrade", CopyMorphlingJarRecipe.Serializer::new);

	public static final RegistryObject<RecipeSerializer<?>> blood_gourd_upgrade_serializer = SERIALIZERS
			.register("blood_gourd_upgrade", CopyBloodGourdRecipe.Serializer::new);

	public static final RegistryObject<RecipeSerializer<?>> blood_gourd_fill_serializer = SERIALIZERS
			.register("blood_gourd_fill", FillBloodGourdRecipe.Serializer::new);

	public static final RegistryObject<RecipeSerializer<?>> memory_weaving_serializer = SERIALIZERS
			.register("memory_weaving", MemoryWeavingRecipeSerializer::new);

	public static final RegistryObject<RecipeSerializer<?>> blood_structure_recipe_serializer = SERIALIZERS
			.register("blood_structure_recipe", BloodStructureRecipeSerializer::new);

	public static final RegistryObject<RecipeSerializer<?>> chisel_recipe_serializer = SERIALIZERS
			.register("scar_recipe", ScarRecipeSerializer::new);

	public static final RegistryObject<RecipeSerializer<?>> cardinal_rite_recipe_serializer = SERIALIZERS
			.register("cardinal_rite_recipe", CardinalRiteRecipeSerializer::new);

	public static final RegistryObject<RecipeSerializer<?>> incubator_serializer = SERIALIZERS
			.register("incubator", IncubatorRecipeSerializer::new);

}
