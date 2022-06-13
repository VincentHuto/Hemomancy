package com.vincenthuto.hemomancy.recipe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Predicate;

import com.vincenthuto.hemomancy.init.BlockInit;
import com.vincenthuto.hemomancy.init.ItemInit;
import com.vincenthuto.hutoslib.client.render.block.MultiblockPattern;

import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.block.state.pattern.BlockPattern;
import net.minecraft.world.level.block.state.pattern.BlockPatternBuilder;
import net.minecraft.world.level.block.state.predicate.BlockStatePredicate;

public class BloodCraftingRecipes {

	public static List<RecipeBaseBloodCrafting> RECIPES = new ArrayList<>();

	public static RecipeSacrificialBloodCrafting liber_sanguinum_recipe;
	public static RecipeBaseBloodCrafting living_staff_recipe;
	public static RecipeBaseBloodCrafting living_grip_recipe;
	public static RecipeBaseBloodCrafting ssc_recipe;
	public static RecipeBaseBloodCrafting hematic_iron_recipe;
	public static RecipeBaseBloodCrafting unstained_pillar_recipe;
	public static RecipeBaseBloodCrafting morphling_incubator_recipe;

	public static void initRecipes() {
		liber_sanguinum_recipe = registerRecipe(new RecipeSacrificialBloodCrafting(ItemInit.liber_sanguinum.get(), 100,
				ItemInit.sanguine_formation.get(), Blocks.BOOKSHELF, liber_sanguinum_pattern, 10));
		living_staff_recipe = registerRecipe(new RecipeBaseBloodCrafting(ItemInit.living_staff.get(), 150,
				ItemInit.sanguine_formation.get(), Blocks.IRON_BARS, living_staff_pattern));
		living_grip_recipe = registerRecipe(new RecipeBaseBloodCrafting(ItemInit.living_grasp.get(), 150, Items.BUCKET,
				BlockInit.venous_stone.get(), living_grip_pattern));
		ssc_recipe = registerRecipe(new RecipeBaseBloodCrafting(BlockInit.semi_sentient_construct.get(), 250,
				BlockInit.befouling_ash_trail.get().asItem(), BlockInit.conscious_mass.get(), ssc_pattern));
		hematic_iron_recipe = registerRecipe(new RecipeBaseBloodCrafting(BlockInit.hematic_iron_block.get(), 50,
				Items.INK_SAC, Blocks.IRON_BLOCK, hematic_iron_pattern));
		unstained_pillar_recipe = registerRecipe(new RecipeBaseBloodCrafting(BlockInit.unstained_podium.get(), 50,
				Items.GLOWSTONE_DUST, BlockInit.hematic_iron_block.get(), unstained_pillar_pattern));
		morphling_incubator_recipe = registerRecipe(new RecipeBaseBloodCrafting(BlockInit.morphling_incubator.get(), 50,
				ItemInit.morphling_polyp.get(), BlockInit.hematic_iron_block.get(), morphling_polyp_pattern));

	}

	static List<MultiblockPattern> BUNDELDPATTERNS = new ArrayList<>();
	static MultiblockPattern liber_sanguinum_pattern;
	static MultiblockPattern living_staff_pattern;
	static MultiblockPattern living_grip_pattern;
	static MultiblockPattern ssc_pattern;
	static MultiblockPattern hematic_iron_pattern;
	static MultiblockPattern unstained_pillar_pattern;
	static MultiblockPattern morphling_polyp_pattern;

	public static void initPatterns() {
		liber_sanguinum_pattern = registerPattern(generateBlockPatternFromArray(bookSymbolList, bookPatternArray));
		living_staff_pattern = registerPattern(generateBlockPatternFromArray(staffSymbolList, staffPatternArray));
		living_grip_pattern = registerPattern(generateBlockPatternFromArray(gripSymbolList, gripPatternArray));
		ssc_pattern = registerPattern(generateBlockPatternFromArray(sscSymbolList, sscArray));
		hematic_iron_pattern = registerPattern(generateBlockPatternFromArray(tIronSymbolList, tIronPatternArray));
		unstained_pillar_pattern = registerPattern(
				generateBlockPatternFromArray(unsPillarSymbolList, unsPillarPatternArray));
		morphling_polyp_pattern = registerPattern(
				generateBlockPatternFromArray(morphIncSymbolList, morphIncPatternArray));
	}

	// Morphling Incubator Block Pattern
	@SuppressWarnings("serial")
	 static HashMap<Character, Block> morphIncSymbolList = new HashMap<Character, Block>() {
		{
			put('G', BlockInit.sanguine_glass.get());
			put('T', BlockInit.hematic_iron_block.get());
			put('I', BlockInit.infested_venous_stone.get());
			put('A', Blocks.AIR);
		}
	};
	 static String[][] morphIncPatternArray = { { "TGT", "TGT", "AAA" }, { "GGG", "GIG", "ATA" },
			{ "TGT", "TGT", "AAA" } };

	// Unstained Pillar Block Pattern
	@SuppressWarnings("serial")
	static HashMap<Character, Block> unsPillarSymbolList = new HashMap<Character, Block>() {
		{
			put('P', Blocks.QUARTZ_PILLAR);
			put('S', Blocks.QUARTZ_BLOCK);
			put('T', BlockInit.hematic_iron_block.get());
			put('A', Blocks.AIR);

		}
	};
	static String[][] unsPillarPatternArray = { { "AAA", "AAA", "SSS" }, { "ATA", "APA", "SPS" },
			{ "AAA", "AAA", "SSS" } };

	// Tainted Iron Block Pattern
	@SuppressWarnings("serial")
	static HashMap<Character, Block> tIronSymbolList = new HashMap<Character, Block>() {
		{
			put('B', BlockInit.active_befouling_ash_trail.get());
			put('I', Blocks.IRON_BLOCK);
			put('A', Blocks.AIR);
		}
	};
	static String[][] tIronPatternArray = { { "AAA", "BBB" }, { "ABA", "BIB" }, { "AAA", "BBB" } };

	// SSC Pattern
	@SuppressWarnings("serial")
	static HashMap<Character, Block> sscSymbolList = new HashMap<Character, Block>() {
		{
			put('V', BlockInit.venous_stone.get());
			put('A', Blocks.AIR);
			put('T', BlockInit.hematic_iron_block.get());
			put('B', BlockInit.active_befouling_ash_trail.get());
			put('S', BlockInit.active_smouldering_ash_trail.get());
			put('C', BlockInit.conscious_mass.get());

		}
	};
	static String[][] sscArray = { { "AAAAAA", "SSSSSS" }, { "AABBAA", "SBTTBS" }, { "ABCCBA", "STVVTS" },
			{ "ABCCBA", "STVVTS" }, { "AABBAA", "SBTTBS" }, { "AAAAAA", "SSSSSS" } };

	// Grip Pattern
	@SuppressWarnings("serial")
	static HashMap<Character, Block> gripSymbolList = new HashMap<Character, Block>() {
		{
			put('R', BlockInit.active_befouling_ash_trail.get());
			put('V', BlockInit.venous_stone.get());
			put('A', Blocks.AIR);
		}
	};
	static String[][] gripPatternArray = { { "AAAAA", "ARRRA" }, { "AVAVA", "RVRVR" }, { "AAVAA", "RRRRR" },
			{ "AVAVA", "RVRVR" }, { "AAAAA", "ARRRA" } };

	// Book Pattern
	@SuppressWarnings("serial")
	static HashMap<Character, Block> bookSymbolList = new HashMap<Character, Block>() {
		{
			put('R', BlockInit.befouling_ash_trail.get());
			put('B', Blocks.BOOKSHELF);
		}
	};
	static String[][] bookPatternArray = { { "RRR" }, { "RBR" }, { "RRR" } };

	// Living Staff Pattern
	@SuppressWarnings("serial")
	static HashMap<Character, Block> staffSymbolList = new HashMap<Character, Block>() {
		{
			put('I', Blocks.IRON_BARS);
			put('V', BlockInit.venous_stone.get());

		}
	};
	static String[][] staffPatternArray = { { "V", "I", "I", "V" } };

	static RecipeSacrificialBloodCrafting registerRecipe(RecipeSacrificialBloodCrafting recipe) {
		RECIPES.add(recipe);
		return recipe;
	}

	static RecipeBaseBloodCrafting registerRecipe(RecipeBaseBloodCrafting recipe) {
		RECIPES.add(recipe);
		return recipe;
	}

	static MultiblockPattern registerPattern(MultiblockPattern pattern) {
		BUNDELDPATTERNS.add(pattern);
		return pattern;
	}

	public static MultiblockPattern generateBlockPatternFromArray(HashMap<Character, Block> symbolList,
			String[][] schematic) {
		BlockPatternBuilder builder = null;
		if (builder == null) {
			builder = BlockPatternBuilder.start();
			for (int aisle = 0; aisle < schematic.length; aisle++) {
				builder.aisle(schematic[aisle]);
				for (int z = 0; z < schematic[aisle].length; z++) {
					List<Character> distinct = getDistinctChars(schematic[aisle][z]);
					for (int c = 0; c < distinct.size(); c++) {
						builder.where(distinct.get(c), blockPredFromHash(symbolList, distinct.get(c)));
					}
				}

			}
		}
		BlockPattern pattern = builder.build();
		return new MultiblockPattern(pattern, symbolList, schematic);
	}

	static Predicate<BlockInWorld> blockPredFromHash(HashMap<Character, Block> hash, char c) {
		return (BlockInWorld.hasState(BlockStatePredicate.forBlock(hash.get(c))));
	}

	public static List<Character> getDistinctChars(String chars) {
		List<Character> distinct = new ArrayList<Character>();
		for (int i = 0; i < chars.length(); i++) {
			if (!distinct.contains(chars.charAt(i))) {
				distinct.add(chars.charAt(i));
			}
		}
		return distinct;

	}


}
