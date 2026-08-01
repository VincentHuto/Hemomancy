package com.vincenthuto.hemomancy.common.init;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.recipe.*;
import com.vincenthuto.hemomancy.common.recipe.serializer.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class RecipeInit {
	public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS = DeferredRegister
			.create(Registries.RECIPE_SERIALIZER, Hemomancy.MOD_ID);

	public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES = DeferredRegister
			.create(Registries.RECIPE_TYPE, Hemomancy.MOD_ID);

	// Types

	public static final DeferredHolder<RecipeType<?>, RecipeType<ScarRecipe>> chisel_recipe = RECIPE_TYPES.register("scar_recipe",
			() -> RecipeType.simple(Hemomancy.rloc("scar_recipe")));

	public static final DeferredHolder<RecipeType<?>, RecipeType<DistillationRecipe>> distillation_recipe_type = RECIPE_TYPES
			.register("distillation_recipe", () -> RecipeType.simple(Hemomancy.rloc("distillation_recipe")));

	public static final DeferredHolder<RecipeType<?>, RecipeType<MemoryWeavingRecipe>> memory_weaving_type = RECIPE_TYPES
			.register("memory_weaving_type", () -> RecipeType.simple(Hemomancy.rloc("memory_weaving")));

	public static final DeferredHolder<RecipeType<?>, RecipeType<BloodStructureRecipe>> blood_structure_recipe_type = RECIPE_TYPES
			.register("blood_structure_recipe", () -> RecipeType.simple(Hemomancy.rloc("blood_structure_recipe")));

	public static final DeferredHolder<RecipeType<?>, RecipeType<CardinalRiteRecipe>> cardinal_rite_recipe_type = RECIPE_TYPES
			.register("cardinal_rite_recipe", () -> RecipeType.simple(Hemomancy.rloc("cardinal_rite_recipe")));

	public static final DeferredHolder<RecipeType<?>, RecipeType<IncubatorRecipe>> incubator_recipe_type = RECIPE_TYPES
			.register("incubator", () -> RecipeType.simple(Hemomancy.rloc("incubator")));

	public static final DeferredHolder<RecipeType<?>, RecipeType<MorphicNectarRecipe>> morphic_nectar_recipe_type = RECIPE_TYPES
			.register("morphic_nectar", () -> RecipeType.simple(Hemomancy.rloc("morphic_nectar")));

	public static final DeferredHolder<RecipeType<?>, RecipeType<WhiteHumorPurificationRecipe>> white_humor_purification_recipe_type = RECIPE_TYPES
			.register("white_humor_purification", () -> RecipeType.simple(Hemomancy.rloc("white_humor_purification")));

	public static final DeferredHolder<RecipeType<?>, RecipeType<FungalScarCultivationRecipe>> fungal_scar_cultivation_type = RECIPE_TYPES
			.register("fungal_scar_cultivation", () -> RecipeType.simple(Hemomancy.rloc("fungal_scar_cultivation")));

	public static final DeferredHolder<RecipeType<?>, RecipeType<EnzymeFruitingRecipe>> enzyme_fruiting_type = RECIPE_TYPES
			.register("enzyme_fruiting", () -> RecipeType.simple(Hemomancy.rloc("enzyme_fruiting")));

	public static final DeferredHolder<RecipeType<?>, RecipeType<ArmatureUpgradeRecipe>> armature_upgrade_type = RECIPE_TYPES
			.register("armature_upgrade", () -> RecipeType.simple(Hemomancy.rloc("armature_upgrade")));

	// Serialize
	public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<?>> distillation_recipe_serializer = SERIALIZERS.register("distillation_recipe",
			DistillationRecipeSerializer::new);

	public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<?>> morphling_jar_upgrade_serializer = SERIALIZERS
			.register("morphling_jar_upgrade", CopyMorphlingJarRecipe.Serializer::new);

	public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<?>> blood_gourd_upgrade_serializer = SERIALIZERS
			.register("blood_gourd_upgrade", CopyBloodGourdRecipe.Serializer::new);

	public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<?>> blood_gourd_fill_serializer = SERIALIZERS
			.register("blood_gourd_fill", FillBloodGourdRecipe.Serializer::new);

	public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<?>> memory_weaving_serializer = SERIALIZERS
			.register("memory_weaving", MemoryWeavingRecipeSerializer::new);

	public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<?>> blood_structure_recipe_serializer = SERIALIZERS
			.register("blood_structure_recipe", BloodStructureRecipeSerializer::new);

	public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<?>> chisel_recipe_serializer = SERIALIZERS
			.register("scar_recipe", ScarRecipeSerializer::new);

	public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<?>> cardinal_rite_recipe_serializer = SERIALIZERS
			.register("cardinal_rite_recipe", CardinalRiteRecipeSerializer::new);

	public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<?>> incubator_serializer = SERIALIZERS
			.register("incubator", IncubatorRecipeSerializer::new);

	public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<?>> morphic_nectar_serializer = SERIALIZERS
			.register("morphic_nectar", MorphicNectarRecipeSerializer::new);

	public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<?>> white_humor_purification_serializer = SERIALIZERS
			.register("white_humor_purification", WhiteHumorPurificationRecipeSerializer::new);

	public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<?>> fungal_scar_cultivation_serializer = SERIALIZERS
			.register("fungal_scar_cultivation", FungalScarCultivationSerializer::new);

	public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<?>> enzyme_fruiting_serializer = SERIALIZERS
			.register("enzyme_fruiting", EnzymeFruitingRecipeSerializer::new);

	public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<ArmatureUpgradeRecipe>> armature_upgrade_serializer =
			SERIALIZERS.register("armature_upgrade", ArmatureUpgradeRecipeSerializer::new);

	public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<?>> scale_grip_binding_serializer = SERIALIZERS
			.register("scale_grip_binding", ScaleGripBindingRecipe.Serializer::new);

}
