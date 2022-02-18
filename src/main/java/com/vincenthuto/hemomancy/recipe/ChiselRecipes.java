package com.vincenthuto.hemomancy.recipe;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

import com.google.common.base.Preconditions;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.init.ItemInit;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;
import net.minecraftforge.registries.RegistryObject;

public class ChiselRecipes {

	public static final DeferredRegister<RecipeChiselStation> CHISELRECIPES = DeferredRegister
			.create(RecipeChiselStation.class, Hemomancy.MOD_ID);
	public static Supplier<IForgeRegistry<RecipeChiselStation>> CHISEL_TYPE_REGISTRY = CHISELRECIPES
			.makeRegistry("chisel_recipe_types", () -> new RegistryBuilder<RecipeChiselStation>()
					.setMaxID(Integer.MAX_VALUE - 1).setDefaultKey(new ResourceLocation(Hemomancy.MOD_ID, "null")));

	public static final List<RecipeChiselStation> runeRecipies = new ArrayList<>();

	// Base Runes

	public static final RegistryObject<RecipeChiselStation> recipeTranscendence = CHISELRECIPES.register(
			"recipetranscendence",
			() -> registerChiselRecipe(new ResourceLocation(Hemomancy.MOD_ID, "recipetranscendence"),
					new ItemStack(ItemInit.rune_transcendence.get(), 1),
					Arrays.asList(1, 8, 11, 12, 16, 18, 19, 2, 21, 22, 24, 25, 28, 31, 32, 33, 36, 39, 40, 42, 43, 45,
							46, 48, 51, 52, 57, 58, 8),
					Ingredient.of(ItemInit.rune_blank.get()), Ingredient.of(Items.GLOWSTONE_DUST)));

	public static final RegistryObject<RecipeChiselStation> recipeSol = CHISELRECIPES.register("recipesol",
			() -> registerChiselRecipe(new ResourceLocation(Hemomancy.MOD_ID, "recipesol"),
					new ItemStack(ItemInit.rune_sol.get(), 1),
					Arrays.asList(0, 3, 4, 7, 9, 11, 12, 14, 18, 21, 24, 25, 26, 29, 30, 31, 32, 33, 34, 37, 38, 39, 42,
							45, 49, 51, 52, 54, 56, 59, 60, 63),
					Ingredient.of(ItemInit.rune_blank.get()), Ingredient.of(Items.GLOWSTONE_DUST)));

	public static final RegistryObject<RecipeChiselStation> recipeHeart = CHISELRECIPES.register("recipeheart",
			() -> registerChiselRecipe(new ResourceLocation(Hemomancy.MOD_ID, "recipeheart"),
					new ItemStack(ItemInit.rune_heart.get(), 1),
					Arrays.asList(1, 13, 16, 2, 22, 25, 3, 31, 33, 39, 4, 40, 46, 48, 53, 57, 58, 59, 60, 8),
					Ingredient.of(ItemInit.rune_blank.get()), Ingredient.of(Items.GLOWSTONE_DUST)));

	public static final RegistryObject<RecipeChiselStation> recipeDescendence = CHISELRECIPES.register(
			"recipedescendence",
			() -> registerChiselRecipe(new ResourceLocation(Hemomancy.MOD_ID, "recipedescendence"),
					new ItemStack(ItemInit.rune_descendence.get(), 1),
					Arrays.asList(4, 5, 6, 7, 10, 11, 15, 17, 19, 20, 23, 24, 27, 29, 31, 32, 35, 37, 39, 41, 43, 44,
							47, 50, 51, 55, 60, 61, 62, 63),
					Ingredient.of(ItemInit.rune_blank.get()), Ingredient.of(Items.NETHER_STAR)));

	public static final RegistryObject<RecipeChiselStation> recipeMoon = CHISELRECIPES.register("recipemoon",
			() -> registerChiselRecipe(new ResourceLocation(Hemomancy.MOD_ID, "recipemoon"),
					new ItemStack(ItemInit.rune_moon.get(), 1),
					Arrays.asList(14, 16, 2, 23, 24, 27, 28, 3, 31, 32, 39, 4, 40, 42, 45, 47, 49, 5, 50, 53, 54, 9),
					Ingredient.of(ItemInit.rune_blank.get()), Ingredient.of(Items.NETHER_STAR)));

	public static final RegistryObject<RecipeChiselStation> recipeEye = CHISELRECIPES.register("recipeeye",
			() -> registerChiselRecipe(new ResourceLocation(Hemomancy.MOD_ID, "recipeeye"),
					new ItemStack(ItemInit.rune_eye.get(), 1),
					Arrays.asList(14, 16, 17, 2, 22, 23, 24, 25, 27, 28, 3, 30, 31, 32, 33, 35, 36, 38, 39, 4, 40, 41,
							46, 47, 49, 5, 54, 58, 59, 60, 61, 9),
					Ingredient.of(ItemInit.rune_blank.get()), Ingredient.of(Items.NETHER_STAR)));

	public static final RegistryObject<RecipeChiselStation> recipeFeral = CHISELRECIPES.register("recipeferal",
			() -> registerChiselRecipe(new ResourceLocation(Hemomancy.MOD_ID, "recipeferal"),
					new ItemStack(ItemInit.rune_feral.get(), 1),
					Arrays.asList(1, 3, 8, 10, 14, 16, 17, 19, 21, 23, 24, 26, 31, 32, 33, 35, 39, 40, 42, 47, 49, 53,
							54, 58, 59, 60),
					Ingredient.of(ItemInit.rune_blank.get()), Ingredient.of(Items.NETHER_STAR)));

//	public static final RegistryObject<RecipeChiselStation> recipeMoon = CHISELRECIPES.register("recipemoon",
//			() -> registerChiselRecipe(new ResourceLocation(Hemomancy.MOD_ID, "recipemoon"),
//					new ItemStack(ItemInit.rune_moon.get(), 1),
//					Arrays.asList(3, 10, 11, 12, 13, 17, 19, 22, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37,
//							38, 39, 41, 43, 46, 51, 53),
//					Ingredient.of(ItemInit.rune_blank.get()), Ingredient.of(Items.REDSTONE)));
//
//	public static final RegistryObject<RecipeChiselStation> recipeMetamorphosisCW = CHISELRECIPES.register(
//			"recipemetamorphosiscw",
//			() -> registerChiselRecipe(new ResourceLocation(Hemomancy.MOD_ID, "recipemetamorphosiscw"),
//					new ItemStack(ItemInit.rune_metamorphosis_cw.get(), 1),
//					Arrays.asList(3, 10, 19, 21, 22, 24, 28, 31, 32, 35, 39, 41, 42, 44, 53, 60),
//					Ingredient.of(ItemInit.rune_blank.get()), Ingredient.of(Items.GUNPOWDER)));
//
//	public static final RegistryObject<RecipeChiselStation> recipeHeir = CHISELRECIPES.register("recipeheir",
//			() -> registerChiselRecipe(new ResourceLocation(Hemomancy.MOD_ID, "recipeheir"),
//					new ItemStack(ItemInit.rune_heir.get(), 1),
//					Arrays.asList(0, 1, 8, 10, 14, 16, 19, 21, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36,
//							37, 38, 39, 40, 43, 45, 47, 48, 50, 54, 56, 57),
//					Ingredient.of(ItemInit.rune_blank.get()), Ingredient.of(Items.STRING)));
//
//	public static final RegistryObject<RecipeChiselStation> recipeGuidance = CHISELRECIPES.register("recipeguidance",
//			() -> registerChiselRecipe(new ResourceLocation(Hemomancy.MOD_ID, "recipeguidance"),
//					new ItemStack(ItemInit.rune_guidance.get(), 1),
//					Arrays.asList(2, 11, 16, 19, 21, 26, 27, 28, 30, 34, 35, 39, 42, 45, 47, 48, 49, 54),
//					Ingredient.of(ItemInit.rune_blank.get()), Ingredient.of(Items.ENDER_EYE)));
//
//	public static final RegistryObject<RecipeChiselStation> recipeEye = CHISELRECIPES.register("recipeeye",
//			() -> registerChiselRecipe(new ResourceLocation(Hemomancy.MOD_ID, "recipeeye"),
//					new ItemStack(ItemInit.rune_eye.get(), 1),
//					Arrays.asList(2, 7, 10, 11, 13, 14, 18, 19, 20, 21, 24, 25, 26, 27, 28, 32, 33, 34, 35, 36, 42, 43,
//							44, 45, 50, 51, 53, 54, 58, 63),
//					Ingredient.of(ItemInit.rune_blank.get()), Ingredient.of(ItemInit.sanguine_formation.get())));
//
//	public static final RegistryObject<RecipeChiselStation> recipeCommunion = CHISELRECIPES.register("recipecommunion",
//			() -> registerChiselRecipe(new ResourceLocation(Hemomancy.MOD_ID, "recipecommunion"),
//					new ItemStack(ItemInit.rune_communion.get(), 1),
//					Arrays.asList(5, 10, 11, 17, 20, 24, 29, 30, 31, 32, 37, 38, 39, 41, 44, 50, 51, 61),
//					Ingredient.of(ItemInit.rune_blank.get()), Ingredient.of(ItemInit.mind_spike.get())));
//
//	public static final RegistryObject<RecipeChiselStation> recipeBeast = CHISELRECIPES.register("recipebeast",
//			() -> registerChiselRecipe(new ResourceLocation(Hemomancy.MOD_ID, "recipebeast"),
//					new ItemStack(ItemInit.rune_beast.get(), 1),
//					Arrays.asList(9, 18, 24, 26, 27, 28, 30, 33, 34, 35, 37, 39, 43, 49, 50),
//					Ingredient.of(ItemInit.rune_blank.get()), Ingredient.of(Items.BEEF)));
//
//	public static final RegistryObject<RecipeChiselStation> recipeClawMark = CHISELRECIPES.register("recipeclawmark",
//			() -> registerChiselRecipe(new ResourceLocation(Hemomancy.MOD_ID, "recipeclawmark"),
//					new ItemStack(ItemInit.rune_clawmark.get(), 1),
//					Arrays.asList(7, 11, 12, 14, 21, 28, 30, 31, 32, 33, 35, 42, 49, 51, 52, 56),
//					Ingredient.of(ItemInit.rune_blank.get()), Ingredient.of(Items.FLINT)));
//
//	public static final RegistryObject<RecipeChiselStation> recipeMetamorphosis = CHISELRECIPES.register(
//			"recipemetamorphosis",
//			() -> registerChiselRecipe(new ResourceLocation(Hemomancy.MOD_ID, "recipemetamorphosis"),
//					new ItemStack(ItemInit.rune_metamorphosis.get(), 1),
//					Arrays.asList(4, 13, 17, 18, 20, 24, 27, 31, 32, 36, 39, 43, 45, 46, 50, 59),
//					Ingredient.of(ItemInit.rune_blank.get()), Ingredient.of(Items.SUGAR)));
//
//	public static final RegistryObject<RecipeChiselStation> recipeOedon = CHISELRECIPES
//			.register("recipeoedon",
//					() -> registerChiselRecipe(new ResourceLocation(Hemomancy.MOD_ID, "recipeoedon"),
//							new ItemStack(ItemInit.rune_oedon.get(), 1), Arrays.asList(1, 10, 16, 18, 19, 20, 21, 22,
//									23, 27, 35, 40, 42, 43, 44, 45, 46, 47, 50, 57),
//							Ingredient.of(ItemInit.rune_blank.get()), Ingredient.of(Items.REDSTONE)));
//
//	public static final RegistryObject<RecipeChiselStation> recipeLake = CHISELRECIPES.register("recipelake",
//			() -> registerChiselRecipe(new ResourceLocation(Hemomancy.MOD_ID, "recipelake"),
//					new ItemStack(ItemInit.rune_lake.get(), 1),
//					Arrays.asList(4, 12, 13, 14, 20, 28, 29, 30, 34, 35, 36, 44, 51, 52, 53, 60),
//					Ingredient.of(ItemInit.rune_blank.get()), Ingredient.of(Items.WATER_BUCKET)));
//
//	public static final RegistryObject<RecipeChiselStation> recipeRapture = CHISELRECIPES.register("reciperapture",
//			() -> registerChiselRecipe(new ResourceLocation(Hemomancy.MOD_ID, "reciperapture"),
//					new ItemStack(ItemInit.rune_rapture.get(), 1),
//					Arrays.asList(0, 3, 4, 7, 9, 11, 12, 14, 18, 21, 24, 25, 26, 29, 30, 31, 32, 33, 34, 37, 38, 39, 42,
//							45, 49, 51, 52, 54, 56, 59, 60, 63),
//					Ingredient.of(ItemInit.rune_blank.get()), Ingredient.of(Items.GLOWSTONE_DUST)));
//
//	// Contract Runes
//
//	public static final RegistryObject<RecipeChiselStation> recipeBeastContract = CHISELRECIPES.register(
//			"recipebeastcontract",
//			() -> registerChiselRecipe(new ResourceLocation(Hemomancy.MOD_ID, "recipebeastcontract"),
//					new ItemStack(ItemInit.rune_beast_c.get(), 1),
//					Arrays.asList(12, 14, 21, 28, 30, 33, 34, 39, 40, 43, 47, 49, 54, 58, 59, 60, 61),
//					Ingredient.of(ItemInit.rune_beast.get()), Ingredient.of(Items.DIAMOND)));
//
//	public static final RegistryObject<RecipeChiselStation> recipeImpurityContract = CHISELRECIPES.register(
//			"recipeimpuritycontract",
//			() -> registerChiselRecipe(new ResourceLocation(Hemomancy.MOD_ID, "recipeimpuritycontract"),
//					new ItemStack(ItemInit.rune_impurity_c.get(), 1), Arrays.asList(1, 10, 12, 14, 19, 24, 25, 27, 28,
//							29, 30, 31, 32, 33, 35, 36, 37, 38, 39, 43, 50, 52, 54, 57),
//					Ingredient.of(ItemInit.rune_oedon.get()), Ingredient.of(Items.DIAMOND)));
//
//	public static final RegistryObject<RecipeChiselStation> recipeCorruptionContract = CHISELRECIPES.register(
//			"recipecorruptioncontract",
//			() -> registerChiselRecipe(new ResourceLocation(Hemomancy.MOD_ID, "recipecorruptioncontract"),
//					new ItemStack(ItemInit.rune_corruption_c.get(), 1), Arrays.asList(10, 12, 17, 19, 21, 24, 26, 27,
//							28, 29, 30, 31, 32, 34, 35, 36, 37, 38, 39, 41, 43, 45, 50, 52),
//					Ingredient.of(ItemInit.rune_clawmark.get()), Ingredient.of(Items.DIAMOND)));
//
//	public static final RegistryObject<RecipeChiselStation> recipeMilkweedContract = CHISELRECIPES.register(
//			"recipemilkweedcontract",
//			() -> registerChiselRecipe(new ResourceLocation(Hemomancy.MOD_ID, "recipemilkweedcontract"),
//					new ItemStack(ItemInit.rune_milkweed_c.get(), 1),
//					Arrays.asList(1, 2, 3, 4, 5, 6, 11, 16, 17, 18, 19, 20, 21, 22, 27, 33, 34, 35, 36, 37, 38, 43, 49,
//							50, 51, 52, 53, 54, 55, 57, 58, 59),
//					Ingredient.of(ItemInit.rune_lake.get()), Ingredient.of(Items.DIAMOND)));
//
//	public static final RegistryObject<RecipeChiselStation> recipeHunterContract = CHISELRECIPES.register(
//			"recipehuntercontract",
//			() -> registerChiselRecipe(new ResourceLocation(Hemomancy.MOD_ID, "recipehuntercontract"),
//					new ItemStack(ItemInit.rune_hunter_c.get(), 1), Arrays.asList(6, 13, 15, 20, 24, 25, 26, 27, 28, 29,
//							30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 44, 53, 55, 62),
//					Ingredient.of(ItemInit.rune_guidance.get()), Ingredient.of(Items.DIAMOND)));
//
//	public static final RegistryObject<RecipeChiselStation> recipeRadianceContract = CHISELRECIPES.register(
//			"reciperadiancecontract",
//			() -> registerChiselRecipe(new ResourceLocation(Hemomancy.MOD_ID, "reciperadiancecontract"),
//					new ItemStack(ItemInit.rune_radiance_c.get(), 1),
//					Arrays.asList(4, 5, 6, 7, 10, 11, 15, 17, 19, 20, 23, 24, 27, 29, 31, 32, 35, 37, 39, 41, 43, 44,
//							47, 50, 51, 55, 60, 61, 62, 63),
//					Ingredient.of(ItemInit.rune_rapture.get()), Ingredient.of(Items.NETHER_STAR)));

	public static List<RecipeChiselStation> getRunerecipies() {
		return runeRecipies;
	}

	public static RecipeChiselStation registerChiselRecipe(ResourceLocation rl, ItemStack output, List<Integer> runesIn,
			Ingredient... inputs) {
		Preconditions.checkArgument(inputs.length <= 2);
		RecipeChiselStation recipe = new RecipeChiselStation(rl, output, runesIn, inputs);
		runeRecipies.add(recipe);
		return recipe;
	}

}
