package com.huto.hemomancy.recipes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Predicate;

import com.huto.hemomancy.init.BlockInit;
import com.huto.hemomancy.init.ItemInit;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.pattern.BlockPattern;
import net.minecraft.block.pattern.BlockPatternBuilder;
import net.minecraft.block.pattern.BlockStateMatcher;
import net.minecraft.item.Items;
import net.minecraft.util.CachedBlockInfo;

public class ModBloodCraftingRecipes {

	public static List<BaseBloodCraftingRecipe> PATTERNS = new ArrayList<>();

	public static BaseBloodCraftingRecipe liber_sanguinum_recipe;
	public static BaseBloodCraftingRecipe living_staff_recipe;
	public static BaseBloodCraftingRecipe living_grip_recipe;
	public static BaseBloodCraftingRecipe ssc_recipe;
	public static BaseBloodCraftingRecipe tainted_iron_recipe;

	public static void initRecipes() {
		liber_sanguinum_recipe = new BaseBloodCraftingRecipe(ItemInit.liber_sanguinum.get(), 100,
				ItemInit.sanguine_formation.get(), Blocks.BOOKSHELF, liber_sanguinum_pattern);
		PATTERNS.add(liber_sanguinum_recipe);
		living_staff_recipe = new BaseBloodCraftingRecipe(ItemInit.living_staff.get(), 150, Items.STICK,
				Blocks.IRON_BARS,living_staff_pattern);
		PATTERNS.add(living_staff_recipe);
		living_grip_recipe = new BaseBloodCraftingRecipe(ItemInit.living_grasp.get(), 150, Items.BUCKET,
				BlockInit.venous_stone.get(),living_grip_pattern);
		PATTERNS.add(living_grip_recipe);
		ssc_recipe = new BaseBloodCraftingRecipe(BlockInit.semi_sentient_construct.get(), 250, BlockInit.befouling_ash_trail.get().asItem(),
				BlockInit.conscious_mass.get(),ssc_pattern);
		PATTERNS.add(ssc_recipe);
		
		tainted_iron_recipe = new BaseBloodCraftingRecipe(BlockInit.tainted_iron_block.get(), 50, Items.INK_SAC,
				Blocks.IRON_BLOCK,tainted_iron_pattern);
		PATTERNS.add(tainted_iron_recipe);
		
	}

	public static List<BloodCraftingBundledPattern> BUNDELDPATTERNS = new ArrayList<>();
	public static BloodCraftingBundledPattern liber_sanguinum_pattern;
	public static BloodCraftingBundledPattern living_staff_pattern;
	public static BloodCraftingBundledPattern living_grip_pattern;
	public static BloodCraftingBundledPattern ssc_pattern;
	public static BloodCraftingBundledPattern tainted_iron_pattern;

	public static void initPatterns() {
		liber_sanguinum_pattern = new BloodCraftingBundledPattern(getBookPattern(), bookSymbolList, bookPatternArray);
		BUNDELDPATTERNS.add(liber_sanguinum_pattern);
		living_staff_pattern = new BloodCraftingBundledPattern(getLivingStaffPattern(), staffSymbolList,
				staffPatternArray);
		BUNDELDPATTERNS.add(living_staff_pattern);
		living_grip_pattern = new BloodCraftingBundledPattern(getGripPattern(), gripSymbolList,
				gripPatternArray);
		BUNDELDPATTERNS.add(living_grip_pattern);
		ssc_pattern = new BloodCraftingBundledPattern(getSSCPattern(), sscSymbolList,
				sscPatternArray);
		BUNDELDPATTERNS.add(ssc_pattern);
		
		
		tainted_iron_pattern = new BloodCraftingBundledPattern(geTaintedBlockPattern(), tIronSymbolList,
				tIronPatternArray);
		BUNDELDPATTERNS.add(tainted_iron_pattern);

	}
	
	// Tainted Iron Block Pattern
	@SuppressWarnings("serial")
	public static HashMap<Character, Block> tIronSymbolList = new HashMap<Character, Block>() {
		{
			put('B', BlockInit.befouling_ash_trail.get());
			put('I', Blocks.IRON_BLOCK);
			put('A', Blocks.AIR);
		}
	};
	public static String[][] tIronPatternArray = { {"AAA","BBB" }, { "ABA","BIB"  }, { "AAA","BBB" } };

	public static BlockPattern geTaintedBlockPattern() {
		BlockPattern tIronPattern = null;
		if (tIronPattern == null) {
			// 3x2x3
			tIronPattern = BlockPatternBuilder.start()
					.aisle("AAA","BBB").where('B',blockPredFromHash(tIronSymbolList,'B')).where('A',blockPredFromHash(tIronSymbolList,'A'))
					.aisle("ABA","BIB").where('I',blockPredFromHash(tIronSymbolList,'I')).where('A',blockPredFromHash(tIronSymbolList,'A')).where('B',blockPredFromHash(tIronSymbolList,'B'))
					.aisle("AAA","BBB").where('B',blockPredFromHash(tIronSymbolList,'B')).where('A',blockPredFromHash(tIronSymbolList,'A'))
					.build();
		}
		return tIronPattern;
	}
	
	
	
	
	//SSC Pattern
	@SuppressWarnings("serial")
	public static HashMap<Character, Block> sscSymbolList = new HashMap<Character, Block>() {
		{
			put('V', BlockInit.venous_stone.get());
			put('A', Blocks.AIR);
			put('T', BlockInit.tainted_iron_block.get());
			put('B', BlockInit.befouling_ash_trail.get());
			put('S', BlockInit.smouldering_ash_trail.get());
			put('C', BlockInit.conscious_mass.get());

		}
	};
	public static String[][] sscPatternArray = {
			{"AAAAAA", "SSSSSS"},
			{"AABBAA","SBTTBS"},
			{"ABCCBA","STVVTS"},
			{"ABCCBA","STVVTS"},
			{"AABBAA","SBTTBS"},
			{"AAAAAA", "SSSSSS"}};
	public static BlockPattern getSSCPattern() {
		BlockPattern sscPattern = null;
		if (sscPattern == null) {
			//6x2x6
			sscPattern = BlockPatternBuilder.start()
					.aisle("AAAAAA", "SSSSSS").where('S',blockPredFromHash(sscSymbolList,'S')).where('A',blockPredFromHash(sscSymbolList,'A'))
					.aisle("AABBAA","SBTTBS").where('S',blockPredFromHash(sscSymbolList,'S')).where('A',blockPredFromHash(sscSymbolList,'A')).where('B',blockPredFromHash(sscSymbolList,'B')).where('T',blockPredFromHash(sscSymbolList,'T'))
					.aisle("ABCCBA","STVVTS").where('S',blockPredFromHash(sscSymbolList,'S')).where('A',blockPredFromHash(sscSymbolList,'A')).where('B',blockPredFromHash(sscSymbolList,'B')).where('T',blockPredFromHash(sscSymbolList,'T')).where('C',blockPredFromHash(sscSymbolList,'C')).where('V',blockPredFromHash(sscSymbolList,'V'))
					.aisle("ABCCBA","STVVTS").where('S',blockPredFromHash(sscSymbolList,'S')).where('A',blockPredFromHash(sscSymbolList,'A')).where('B',blockPredFromHash(sscSymbolList,'B')).where('T',blockPredFromHash(sscSymbolList,'T')).where('C',blockPredFromHash(sscSymbolList,'C')).where('V',blockPredFromHash(sscSymbolList,'V'))
					.aisle("AABBAA","SBTTBS").where('S',blockPredFromHash(sscSymbolList,'S')).where('A',blockPredFromHash(sscSymbolList,'A')).where('B',blockPredFromHash(sscSymbolList,'B')).where('T',blockPredFromHash(sscSymbolList,'T'))
					.aisle("AAAAAA", "SSSSSS").where('S',blockPredFromHash(sscSymbolList,'S')).where('A',blockPredFromHash(sscSymbolList,'A'))
					.build();
		}
		return sscPattern;
	}
	
	
	//Grip Pattern
	@SuppressWarnings("serial")
	public static HashMap<Character, Block> gripSymbolList = new HashMap<Character, Block>() {
		{
			put('R', Blocks.REDSTONE_WIRE);
			put('V', BlockInit.venous_stone.get());
			put('A', Blocks.AIR);
		}
	};
	public static String[][] gripPatternArray = { {"AAAAA", "ARRRA"},{"AVAVA","RVRVR"}, {"AAVAA","RRRRR"},{"AVAVA","RVRVR"},{"AAAAA", "ARRRA"}};
	public static BlockPattern getGripPattern() {
		BlockPattern gripPattern = null;
		if (gripPattern == null) {
			//5x2x5
			gripPattern = BlockPatternBuilder.start()
					.aisle("AAAAA", "ARRRA").where('R',blockPredFromHash(gripSymbolList,'R')).where('A',blockPredFromHash(gripSymbolList,'A'))
					.aisle("AVAVA","RVRVR").where('V',blockPredFromHash(gripSymbolList,'V')).where('A',blockPredFromHash(gripSymbolList,'A')).where('R',blockPredFromHash(gripSymbolList,'R'))
					.aisle("AAVAA","RRRRR").where('R',blockPredFromHash(gripSymbolList,'R')).where('A',blockPredFromHash(gripSymbolList,'A'))
					.aisle("AVAVA","RVRVR").where('V',blockPredFromHash(gripSymbolList,'V')).where('A',blockPredFromHash(gripSymbolList,'A')).where('R',blockPredFromHash(gripSymbolList,'R'))
					.aisle("AAAAA", "ARRRA").where('R',blockPredFromHash(gripSymbolList,'R')).where('A',blockPredFromHash(gripSymbolList,'A'))
					.build();
		}
		return gripPattern;
	}
	
	
	
	// Book Pattern
	@SuppressWarnings("serial")
	public static HashMap<Character, Block> bookSymbolList = new HashMap<Character, Block>() {
		{
			put('R', Blocks.REDSTONE_WIRE);
			put('B', Blocks.BOOKSHELF);
		}
	};
	public static String[][] bookPatternArray = { { "RRR" }, { "RBR" }, { "RRR" } };

	public static BlockPattern getBookPattern() {
		BlockPattern bookPattern = null;
		if (bookPattern == null) {
			// 3x1x3
			bookPattern = BlockPatternBuilder.start().aisle("RRR")
					.where('R',blockPredFromHash(bookSymbolList,'R')).aisle("RBR")
					.where('B',blockPredFromHash(bookSymbolList,'B'))
					.where('R',blockPredFromHash(bookSymbolList,'R')).aisle("RRR")
					.where('R',blockPredFromHash(bookSymbolList,'R')).build();
		}
		return bookPattern;
	}

	// Living Staff Pattern
	@SuppressWarnings("serial")
	public static HashMap<Character, Block> staffSymbolList = new HashMap<Character, Block>() {
		{
			put('I', Blocks.IRON_BARS);
			put('V', BlockInit.venous_stone.get());

		}
	};
	public static String[][] staffPatternArray = { { "V", "I", "I", "V" } };

	public static BlockPattern getLivingStaffPattern() {
		BlockPattern bookPattern = null;
		if (bookPattern == null) {
			bookPattern = BlockPatternBuilder.start().aisle("V", "I", "I", "V")
					.where('I',blockPredFromHash(staffSymbolList,'I'))
					.where('V',blockPredFromHash(staffSymbolList,'V'))
					.build();
		}
		return bookPattern;
	}

	public static Predicate<CachedBlockInfo> blockPredFromHash(HashMap<Character, Block> hash, char c){
		return(CachedBlockInfo.hasState(BlockStateMatcher.forBlock(hash.get(c))));
	}
	
}
