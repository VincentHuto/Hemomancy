package com.huto.hemomancy.recipes;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Preconditions;
import com.huto.hemomancy.init.ItemInit;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;

public class ModChiselRecipes {
	public static final List<RecipeChiselStation> runeRecipies = new ArrayList<>();

	public static RecipeChiselStation recipeClawMark, recipeMetamorphosis, recipeOedon, recipeLake, recipeRapture,
			recipeEye, recipeBeast, recipeCommunion, recipeGuidance, recipeHeir, recipeMetamorphosisCW, recipeMoon;

	public static RecipeChiselStation recipeBeastContract, recipeImpurityContract, recipeCorruptionContract,
			recipeMilkweedContract, recipeRadianceContract, recipeHunterContract;

	public static void init() {

		recipeMoon = registerChiselRecipe(new ResourceLocation("recipemoon"),
				new ItemStack(ItemInit.rune_moon.get(), 1), new ArrayList<Integer>() {
					/**
					 * 
					 */
					private static final long serialVersionUID = 1L;

					{
						add(3);
						add(10);
						add(11);
						add(12);
						add(13);
						add(17);
						add(19);
						add(22);
						add(24);
						add(25);
						add(26);
						add(27);
						add(28);
						add(29);
						add(30);
						add(31);
						add(32);
						add(33);
						add(34);
						add(35);
						add(36);
						add(37);
						add(38);
						add(39);
						add(41);
						add(43);
						add(46);
						add(51);
						add(53);
					}
				}, Ingredient.fromItems(ItemInit.rune_blank.get()), Ingredient.fromItems(Items.REDSTONE));

		recipeMetamorphosisCW = registerChiselRecipe(new ResourceLocation("recipemetamorphosiscw"),
				new ItemStack(ItemInit.rune_metamorphosis_cw.get(), 1), new ArrayList<Integer>() {
					/**
					 * 
					 */
					private static final long serialVersionUID = 1L;

					{
						add(3);
						add(10);
						add(19);
						add(21);
						add(22);
						add(24);
						add(28);
						add(31);
						add(32);
						add(35);
						add(39);
						add(41);
						add(42);
						add(44);
						add(53);
						add(60);
					}
				}, Ingredient.fromItems(ItemInit.rune_blank.get()), Ingredient.fromItems(Items.GUNPOWDER));

		recipeHeir = registerChiselRecipe(new ResourceLocation("recipeheir"),
				new ItemStack(ItemInit.rune_heir.get(), 1), new ArrayList<Integer>() {
					/**
					 * 
					 */
					private static final long serialVersionUID = 1L;

					{
						add(0);
						add(1);
						add(8);
						add(10);
						add(14);
						add(16);
						add(19);
						add(21);
						add(23);
						add(24);
						add(25);
						add(26);
						add(27);
						add(28);
						add(29);
						add(30);
						add(31);
						add(32);
						add(33);
						add(34);
						add(35);
						add(36);
						add(37);
						add(38);
						add(39);
						add(40);
						add(43);
						add(45);
						add(47);
						add(48);
						add(50);
						add(54);
						add(56);
						add(57);
					}
				}, Ingredient.fromItems(ItemInit.rune_blank.get()), Ingredient.fromItems(Items.STRING));

		recipeGuidance = registerChiselRecipe(new ResourceLocation("recipeguidance"),
				new ItemStack(ItemInit.rune_guidance.get(), 1), new ArrayList<Integer>() {
					/**
					 * 
					 */
					private static final long serialVersionUID = 1L;

					{
						add(2);
						add(11);
						add(16);
						add(19);
						add(21);
						add(26);
						add(27);
						add(28);
						add(30);
						add(34);
						add(35);
						add(39);
						add(42);
						add(45);
						add(47);
						add(48);
						add(49);
						add(54);
					}
				}, Ingredient.fromItems(ItemInit.rune_blank.get()), Ingredient.fromItems(Items.ENDER_EYE));

		recipeEye = registerChiselRecipe(new ResourceLocation("recipeeye"), new ItemStack(ItemInit.rune_eye.get(), 1),
				new ArrayList<Integer>() {
					/**
					 * 
					 */
					private static final long serialVersionUID = 1L;

					{
						add(2);
						add(7);
						add(10);
						add(11);
						add(13);
						add(14);
						add(18);
						add(19);
						add(20);
						add(21);
						add(24);
						add(25);
						add(26);
						add(27);
						add(28);
						add(32);
						add(33);
						add(34);
						add(35);
						add(36);
						add(42);
						add(43);
						add(44);
						add(45);
						add(50);
						add(51);
						add(53);
						add(54);
						add(58);
						add(63);

					}
				}, Ingredient.fromItems(ItemInit.rune_blank.get()),
				Ingredient.fromItems(ItemInit.sanguine_formation.get()));

		recipeCommunion = registerChiselRecipe(new ResourceLocation("recipecommunion"),
				new ItemStack(ItemInit.rune_communion.get(), 1), new ArrayList<Integer>() {
					/**
					 * 
					 */
					private static final long serialVersionUID = 1L;

					{
						add(5);
						add(10);
						add(11);
						add(17);
						add(20);
						add(24);
						add(29);
						add(30);
						add(31);
						add(32);
						add(37);
						add(38);
						add(39);
						add(41);
						add(44);
						add(50);
						add(51);
						add(61);
					}
				}, Ingredient.fromItems(ItemInit.rune_blank.get()), Ingredient.fromItems(ItemInit.mind_spike.get()));

		recipeBeast = registerChiselRecipe(new ResourceLocation("recipebeast"),
				new ItemStack(ItemInit.rune_beast.get(), 1), new ArrayList<Integer>() {
					/**
					 * 
					 */
					private static final long serialVersionUID = 1L;

					{
						add(9);
						add(18);
						add(24);
						add(26);
						add(27);
						add(28);
						add(30);
						add(33);
						add(34);
						add(35);
						add(37);
						add(39);
						add(43);
						add(49);
						add(50);
					}
				}, Ingredient.fromItems(ItemInit.rune_blank.get()), Ingredient.fromItems(Items.BEEF));

		recipeClawMark = registerChiselRecipe(new ResourceLocation("recipeclawmark"),
				new ItemStack(ItemInit.rune_clawmark.get(), 1), new ArrayList<Integer>() {
					/**
					 * 
					 */
					private static final long serialVersionUID = 1L;

					{
						add(7);
						add(11);
						add(12);
						add(14);
						add(21);
						add(28);
						add(30);
						add(31);
						add(32);
						add(33);
						add(35);
						add(42);
						add(49);
						add(51);
						add(52);
						add(56);
					}
				}, Ingredient.fromItems(ItemInit.rune_blank.get()), Ingredient.fromItems(Items.FLINT));

		recipeMetamorphosis = registerChiselRecipe(new ResourceLocation("recipemetamorphosis"),
				new ItemStack(ItemInit.rune_metamorphosis.get(), 1), new ArrayList<Integer>() {
					/**
					 * 
					 */
					private static final long serialVersionUID = 1L;

					{
						add(4);
						add(13);
						add(17);
						add(18);
						add(20);
						add(24);
						add(27);
						add(31);
						add(32);
						add(36);
						add(39);
						add(43);
						add(45);
						add(46);
						add(50);
						add(59);
					}
				}, Ingredient.fromItems(ItemInit.rune_blank.get()), Ingredient.fromItems(Items.SUGAR));
		recipeOedon = registerChiselRecipe(new ResourceLocation("recipeoedon"),
				new ItemStack(ItemInit.rune_oedon.get(), 1), new ArrayList<Integer>() {
					/**
					 * 
					 */
					private static final long serialVersionUID = 1L;

					{
						add(1);
						add(10);
						add(16);
						add(18);
						add(19);
						add(20);
						add(21);
						add(22);
						add(23);
						add(27);
						add(35);
						add(40);
						add(42);
						add(43);
						add(44);
						add(45);
						add(46);
						add(47);
						add(50);
						add(57);
					}
				}, Ingredient.fromItems(ItemInit.rune_blank.get()), Ingredient.fromItems(Items.REDSTONE));

		recipeLake = registerChiselRecipe(new ResourceLocation("recipelake"),
				new ItemStack(ItemInit.rune_lake.get(), 1), new ArrayList<Integer>() {

					/**
					 * 
					 */
					private static final long serialVersionUID = 1L;

					{
						add(4);
						add(12);
						add(13);
						add(14);
						add(20);
						add(28);
						add(29);
						add(30);
						add(34);
						add(35);
						add(36);
						add(44);
						add(51);
						add(52);
						add(53);
						add(60);
					}
				}, Ingredient.fromItems(ItemInit.rune_blank.get()), Ingredient.fromItems(Items.WATER_BUCKET));

		recipeRapture = registerChiselRecipe(new ResourceLocation("reciperapture"),
				new ItemStack(ItemInit.rune_rapture.get(), 1), new ArrayList<Integer>() {
					/**
					 * 
					 */
					private static final long serialVersionUID = 1L;

					{
						add(0);
						add(3);
						add(4);
						add(7);
						add(9);
						add(11);
						add(12);
						add(14);
						add(18);
						add(21);
						add(24);
						add(25);
						add(26);
						add(29);
						add(30);
						add(31);
						add(32);
						add(33);
						add(34);
						add(37);
						add(38);
						add(39);
						add(42);
						add(45);
						add(49);
						add(51);
						add(52);
						add(54);
						add(56);
						add(59);
						add(60);
						add(63);

					}
				}, Ingredient.fromItems(ItemInit.rune_blank.get()), Ingredient.fromItems(Items.GLOWSTONE_DUST));

		// Contract runes

		recipeBeastContract = registerChiselRecipe(new ResourceLocation("recipebeastcontract"),
				new ItemStack(ItemInit.rune_beast_c.get(), 1), new ArrayList<Integer>() {
					/**
					 * 
					 */
					private static final long serialVersionUID = 1L;

					{
						add(12);
						add(14);
						add(21);
						add(28);
						add(30);
						add(33);
						add(34);
						add(39);
						add(40);
						add(43);
						add(47);
						add(49);
						add(54);
						add(58);
						add(59);
						add(60);
						add(61);
					}
				}, Ingredient.fromItems(ItemInit.rune_beast.get()), Ingredient.fromItems(Items.DIAMOND));

		recipeImpurityContract = registerChiselRecipe(new ResourceLocation("recipeimpuritycontract"),
				new ItemStack(ItemInit.rune_impurity_c.get(), 1), new ArrayList<Integer>() {
					/**
					 * 
					 */
					private static final long serialVersionUID = 1L;

					{
						add(1);
						add(10);
						add(12);
						add(14);
						add(19);
						add(24);
						add(25);
						add(27);
						add(28);
						add(29);
						add(30);
						add(31);
						add(32);
						add(33);
						add(35);
						add(36);
						add(37);
						add(38);
						add(39);
						add(43);
						add(50);
						add(52);
						add(54);
						add(57);
					}
				}, Ingredient.fromItems(ItemInit.rune_oedon.get()), Ingredient.fromItems(Items.DIAMOND));
		recipeCorruptionContract = registerChiselRecipe(new ResourceLocation("recipecorruptioncontract"),
				new ItemStack(ItemInit.rune_corruption_c.get(), 1), new ArrayList<Integer>() {
					/**
					 * 
					 */
					private static final long serialVersionUID = 1L;

					{
						add(10);
						add(12);
						add(17);
						add(19);
						add(21);
						add(24);
						add(26);
						add(27);
						add(28);
						add(29);
						add(30);
						add(31);
						add(32);
						add(34);
						add(35);
						add(36);
						add(37);
						add(38);
						add(39);
						add(41);
						add(43);
						add(45);
						add(50);
						add(52);
					}
				}, Ingredient.fromItems(ItemInit.rune_clawmark.get()), Ingredient.fromItems(Items.DIAMOND));

		recipeMilkweedContract = registerChiselRecipe(new ResourceLocation("recipemilkweedcontract"),
				new ItemStack(ItemInit.rune_milkweed_c.get(), 1), new ArrayList<Integer>() {
					/**
					 * 
					 */
					private static final long serialVersionUID = 1L;

					{
						add(1);
						add(2);
						add(3);
						add(4);
						add(5);
						add(6);
						add(11);
						add(16);
						add(17);
						add(18);
						add(19);
						add(20);
						add(21);
						add(22);
						add(27);
						add(33);
						add(34);
						add(35);
						add(36);
						add(37);
						add(38);
						add(43);
						add(49);
						add(50);
						add(51);
						add(52);
						add(53);
						add(54);
						add(55);
						add(57);
						add(58);
						add(59);
					}
				}, Ingredient.fromItems(ItemInit.rune_lake.get()), Ingredient.fromItems(Items.DIAMOND));

		recipeHunterContract = registerChiselRecipe(new ResourceLocation("recipehuntercontract"),
				new ItemStack(ItemInit.rune_hunter_c.get(), 1), new ArrayList<Integer>() {

					/**
					 * 
					 */
					private static final long serialVersionUID = 1L;

					{
						add(6);
						add(13);
						add(15);
						add(20);
						add(24);
						add(25);
						add(26);
						add(27);
						add(28);
						add(29);
						add(30);
						add(31);
						add(32);
						add(33);
						add(34);
						add(35);
						add(36);
						add(37);
						add(38);
						add(39);
						add(44);
						add(53);
						add(55);
						add(62);
					}
				}, Ingredient.fromItems(ItemInit.rune_guidance.get()), Ingredient.fromItems(Items.DIAMOND));

		recipeRadianceContract = registerChiselRecipe(new ResourceLocation("reciperadiancecontract"),
				new ItemStack(ItemInit.rune_radiance_c.get(), 1), new ArrayList<Integer>() {
					/**
					 * 
					 */
					private static final long serialVersionUID = 1L;

					{
						add(4);
						add(5);
						add(6);
						add(7);
						add(10);
						add(11);
						add(15);
						add(17);
						add(19);
						add(20);
						add(23);
						add(24);
						add(27);
						add(29);
						add(31);
						add(32);
						add(35);
						add(37);
						add(39);
						add(41);
						add(43);
						add(44);
						add(47);
						add(50);
						add(51);
						add(55);
						add(60);
						add(61);
						add(62);
						add(63);
					}
				}, Ingredient.fromItems(ItemInit.rune_rapture.get()), Ingredient.fromItems(Items.NETHER_STAR));
	}

	public static List<RecipeChiselStation> getRunerecipies() {
		return runeRecipies;
	}

	public static RecipeChiselStation registerChiselRecipe(ResourceLocation rl, ItemStack output,
			ArrayList<Integer> runesIn, Ingredient... inputs) {
		Preconditions.checkArgument(inputs.length <= 2);
		RecipeChiselStation recipe = new RecipeChiselStation(rl, output, runesIn, inputs);
		runeRecipies.add(recipe);
		return recipe;
	}

}
