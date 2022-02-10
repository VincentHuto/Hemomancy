package com.vincenthuto.hemomancy.recipe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Predicate;

import com.vincenthuto.hemomancy.init.BlockInit;
import com.vincenthuto.hemomancy.init.ItemInit;
import com.vincenthuto.hutoslib.client.render.block.LabeledBlockPattern;

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

	static List<LabeledBlockPattern> BUNDELDPATTERNS = new ArrayList<>();
	static LabeledBlockPattern liber_sanguinum_pattern;
	static LabeledBlockPattern living_staff_pattern;
	static LabeledBlockPattern living_grip_pattern;
	static LabeledBlockPattern ssc_pattern;
	static LabeledBlockPattern hematic_iron_pattern;
	static LabeledBlockPattern unstained_pillar_pattern;
	static LabeledBlockPattern morphling_polyp_pattern;

	public static void initPatterns() {
		liber_sanguinum_pattern = registerPattern(
				new LabeledBlockPattern(getBookPattern(), bookSymbolList, bookPatternArray));
		living_staff_pattern = registerPattern(
				new LabeledBlockPattern(getLivingStaffPattern(), staffSymbolList, staffPatternArray));
		living_grip_pattern = registerPattern(
				new LabeledBlockPattern(getGripPattern(), gripSymbolList, gripPatternArray));
		ssc_pattern = registerPattern(new LabeledBlockPattern(getSSCPattern(), sscSymbolList, sscArray));
		hematic_iron_pattern = registerPattern(
				new LabeledBlockPattern(getTaintedBlockPattern(), tIronSymbolList, tIronPatternArray));
		unstained_pillar_pattern = registerPattern(
				new LabeledBlockPattern(getUnsPillarBlockPattern(), unsPillarSymbolList, unsPillarPatternArray));
		morphling_polyp_pattern = registerPattern(
				new LabeledBlockPattern(getMorphIncBlockPattern(), morphIncSymbolList, morphIncPatternArray));
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

	static BlockPattern getMorphIncBlockPattern() {
		BlockPattern morphIncPattern = null;
		if (morphIncPattern == null) {
			// 3x3x3
			morphIncPattern = BlockPatternBuilder.start()

					.aisle("TGT", "TGT", "AAA").where('T', blockPredFromHash(morphIncSymbolList, 'T'))
					.where('A', blockPredFromHash(morphIncSymbolList, 'A'))
					.where('G', blockPredFromHash(morphIncSymbolList, 'G')).aisle("GGG", "GIG", "ATA")
					.where('T', blockPredFromHash(morphIncSymbolList, 'T'))
					.where('A', blockPredFromHash(morphIncSymbolList, 'A'))
					.where('I', blockPredFromHash(morphIncSymbolList, 'I'))
					.where('G', blockPredFromHash(morphIncSymbolList, 'G')).aisle("TGT", "TGT", "AAA")
					.where('T', blockPredFromHash(morphIncSymbolList, 'T'))
					.where('A', blockPredFromHash(morphIncSymbolList, 'A'))
					.where('G', blockPredFromHash(morphIncSymbolList, 'G')).build();
		}
		return morphIncPattern;
	}

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

	static BlockPattern getUnsPillarBlockPattern() {
		BlockPattern unsPillarPattern = null;
		if (unsPillarPattern == null) {
			// 3x3x3
			unsPillarPattern = BlockPatternBuilder.start()

					.aisle("AAA", "AAA", "SSS").where('S', blockPredFromHash(unsPillarSymbolList, 'S'))
					.where('A', blockPredFromHash(unsPillarSymbolList, 'A')).aisle("ATA", "APA", "SPS")
					.where('T', blockPredFromHash(unsPillarSymbolList, 'T'))
					.where('A', blockPredFromHash(unsPillarSymbolList, 'A'))
					.where('P', blockPredFromHash(unsPillarSymbolList, 'P'))
					.where('S', blockPredFromHash(unsPillarSymbolList, 'S')).aisle("AAA", "AAA", "SSS")
					.where('S', blockPredFromHash(unsPillarSymbolList, 'S'))
					.where('A', blockPredFromHash(unsPillarSymbolList, 'A')).build();
		}
		return unsPillarPattern;
	}

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

	static BlockPattern getTaintedBlockPattern() {
		BlockPattern tIronPattern = null;
		if (tIronPattern == null) {
			// 3x2x3
			tIronPattern = BlockPatternBuilder.start().aisle("AAA", "BBB")
					.where('B', blockPredFromHash(tIronSymbolList, 'B'))
					.where('A', blockPredFromHash(tIronSymbolList, 'A')).aisle("ABA", "BIB")
					.where('I', blockPredFromHash(tIronSymbolList, 'I'))
					.where('A', blockPredFromHash(tIronSymbolList, 'A'))
					.where('B', blockPredFromHash(tIronSymbolList, 'B')).aisle("AAA", "BBB")
					.where('B', blockPredFromHash(tIronSymbolList, 'B'))
					.where('A', blockPredFromHash(tIronSymbolList, 'A')).build();
		}
		return tIronPattern;
	}

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

	static BlockPattern getSSCPattern() {
		BlockPattern sscPattern = null;
		if (sscPattern == null) {
			// 6x2x6
			sscPattern = BlockPatternBuilder.start().aisle("AAAAAA", "SSSSSS")
					.where('S', blockPredFromHash(sscSymbolList, 'S')).where('A', blockPredFromHash(sscSymbolList, 'A'))
					.aisle("AABBAA", "SBTTBS").where('S', blockPredFromHash(sscSymbolList, 'S'))
					.where('A', blockPredFromHash(sscSymbolList, 'A')).where('B', blockPredFromHash(sscSymbolList, 'B'))
					.where('T', blockPredFromHash(sscSymbolList, 'T')).aisle("ABCCBA", "STVVTS")
					.where('S', blockPredFromHash(sscSymbolList, 'S')).where('A', blockPredFromHash(sscSymbolList, 'A'))
					.where('B', blockPredFromHash(sscSymbolList, 'B')).where('T', blockPredFromHash(sscSymbolList, 'T'))
					.where('C', blockPredFromHash(sscSymbolList, 'C')).where('V', blockPredFromHash(sscSymbolList, 'V'))
					.aisle("ABCCBA", "STVVTS").where('S', blockPredFromHash(sscSymbolList, 'S'))
					.where('A', blockPredFromHash(sscSymbolList, 'A')).where('B', blockPredFromHash(sscSymbolList, 'B'))
					.where('T', blockPredFromHash(sscSymbolList, 'T')).where('C', blockPredFromHash(sscSymbolList, 'C'))
					.where('V', blockPredFromHash(sscSymbolList, 'V')).aisle("AABBAA", "SBTTBS")
					.where('S', blockPredFromHash(sscSymbolList, 'S')).where('A', blockPredFromHash(sscSymbolList, 'A'))
					.where('B', blockPredFromHash(sscSymbolList, 'B')).where('T', blockPredFromHash(sscSymbolList, 'T'))
					.aisle("AAAAAA", "SSSSSS").where('S', blockPredFromHash(sscSymbolList, 'S'))
					.where('A', blockPredFromHash(sscSymbolList, 'A')).build();
		}
		return sscPattern;
	}

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

	static BlockPattern getGripPattern() {
		BlockPattern gripPattern = null;
		if (gripPattern == null) {
			// 5x2x5
			gripPattern = BlockPatternBuilder.start().aisle("AAAAA", "ARRRA")
					.where('R', blockPredFromHash(gripSymbolList, 'R'))
					.where('A', blockPredFromHash(gripSymbolList, 'A')).aisle("AVAVA", "RVRVR")
					.where('V', blockPredFromHash(gripSymbolList, 'V'))
					.where('A', blockPredFromHash(gripSymbolList, 'A'))
					.where('R', blockPredFromHash(gripSymbolList, 'R')).aisle("AAVAA", "RRRRR")
					.where('R', blockPredFromHash(gripSymbolList, 'R'))
					.where('A', blockPredFromHash(gripSymbolList, 'A')).aisle("AVAVA", "RVRVR")
					.where('V', blockPredFromHash(gripSymbolList, 'V'))
					.where('A', blockPredFromHash(gripSymbolList, 'A'))
					.where('R', blockPredFromHash(gripSymbolList, 'R')).aisle("AAAAA", "ARRRA")
					.where('R', blockPredFromHash(gripSymbolList, 'R'))
					.where('A', blockPredFromHash(gripSymbolList, 'A')).build();
		}
		return gripPattern;
	}

	// Book Pattern
	@SuppressWarnings("serial")
	static HashMap<Character, Block> bookSymbolList = new HashMap<Character, Block>() {
		{
			put('R', BlockInit.befouling_ash_trail.get());
			put('B', Blocks.BOOKSHELF);
		}
	};
	static String[][] bookPatternArray = { { "RRR" }, { "RBR" }, { "RRR" } };

	static BlockPattern getBookPattern() {
		BlockPattern bookPattern = null;
		if (bookPattern == null) {
			// 3x1x3
			bookPattern = BlockPatternBuilder.start().aisle("RRR").where('R', blockPredFromHash(bookSymbolList, 'R'))
					.aisle("RBR").where('B', blockPredFromHash(bookSymbolList, 'B'))
					.where('R', blockPredFromHash(bookSymbolList, 'R')).aisle("RRR")
					.where('R', blockPredFromHash(bookSymbolList, 'R')).build();
		}
		return bookPattern;
	}

	// Living Staff Pattern
	@SuppressWarnings("serial")
	static HashMap<Character, Block> staffSymbolList = new HashMap<Character, Block>() {
		{
			put('I', Blocks.IRON_BARS);
			put('V', BlockInit.venous_stone.get());

		}
	};
	static String[][] staffPatternArray = { { "V", "I", "I", "V" } };

	static BlockPattern getLivingStaffPattern() {
		BlockPattern bookPattern = null;
		if (bookPattern == null) {
			bookPattern = BlockPatternBuilder.start().aisle("V", "I", "I", "V")
					.where('I', blockPredFromHash(staffSymbolList, 'I'))
					.where('V', blockPredFromHash(staffSymbolList, 'V')).build();
		}
		return bookPattern;
	}

	static Predicate<BlockInWorld> blockPredFromHash(HashMap<Character, Block> hash, char c) {
		return (BlockInWorld.hasState(BlockStatePredicate.forBlock(hash.get(c))));
	}

	static RecipeSacrificialBloodCrafting registerRecipe(RecipeSacrificialBloodCrafting recipe) {
		RECIPES.add(recipe);
		return recipe;
	}

	static RecipeBaseBloodCrafting registerRecipe(RecipeBaseBloodCrafting recipe) {
		RECIPES.add(recipe);
		return recipe;
	}

	static LabeledBlockPattern registerPattern(LabeledBlockPattern pattern) {
		BUNDELDPATTERNS.add(pattern);
		return pattern;
	}
}
