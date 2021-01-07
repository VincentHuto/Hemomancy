package com.huto.hemomancy.recipes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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

	public static void initRecipes() {
		liber_sanguinum_recipe = new BaseBloodCraftingRecipe(ItemInit.liber_sanguinum.get(), 100,
				ItemInit.sanguine_formation.get(), Blocks.BOOKSHELF, liber_sanguinum_pattern);
		PATTERNS.add(liber_sanguinum_recipe);
		living_staff_recipe = new BaseBloodCraftingRecipe(ItemInit.living_staff.get(), 250, Items.STICK,
				Blocks.IRON_BARS,living_staff_pattern);
		PATTERNS.add(living_staff_recipe);
		living_grip_recipe = new BaseBloodCraftingRecipe(ItemInit.living_grasp.get(), 250, Items.BUCKET,
				BlockInit.venous_stone.get(),living_grip_pattern);
		PATTERNS.add(living_grip_recipe);
	}

	public static List<BloodCraftingBundledPattern> BUNDELDPATTERNS = new ArrayList<>();
	public static BloodCraftingBundledPattern liber_sanguinum_pattern;
	public static BloodCraftingBundledPattern living_staff_pattern;
	public static BloodCraftingBundledPattern living_grip_pattern;

	public static void initPatterns() {
		liber_sanguinum_pattern = new BloodCraftingBundledPattern(getBookPattern(), bookSymbolList, bookPatternArray);
		BUNDELDPATTERNS.add(liber_sanguinum_pattern);
		living_staff_pattern = new BloodCraftingBundledPattern(getLivingStaffPattern(), staffSymbolList,
				staffPatternArray);
		BUNDELDPATTERNS.add(living_staff_pattern);
		living_grip_pattern = new BloodCraftingBundledPattern(getGripPattern(), gripSymbolList,
				gripPatternArray);
		BUNDELDPATTERNS.add(living_grip_pattern);

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
					.aisle("AAAAA", "ARRRA").where('R', CachedBlockInfo.hasState(BlockStateMatcher.forBlock(Blocks.REDSTONE_WIRE))).where('A', CachedBlockInfo.hasState(BlockStateMatcher.forBlock(Blocks.AIR)))
					.aisle("AVAVA","RVRVR").where('V', CachedBlockInfo.hasState(BlockStateMatcher.forBlock(BlockInit.venous_stone.get()))).where('A', CachedBlockInfo.hasState(BlockStateMatcher.forBlock(Blocks.AIR))).where('R', CachedBlockInfo.hasState(BlockStateMatcher.forBlock(Blocks.REDSTONE_WIRE)))
					.aisle("AAVAA","RRRRR").where('R', CachedBlockInfo.hasState(BlockStateMatcher.forBlock(Blocks.REDSTONE_WIRE))).where('A', CachedBlockInfo.hasState(BlockStateMatcher.forBlock(Blocks.AIR)))
					.aisle("AVAVA","RVRVR").where('V', CachedBlockInfo.hasState(BlockStateMatcher.forBlock(BlockInit.venous_stone.get()))).where('A', CachedBlockInfo.hasState(BlockStateMatcher.forBlock(Blocks.AIR))).where('R', CachedBlockInfo.hasState(BlockStateMatcher.forBlock(Blocks.REDSTONE_WIRE)))
					.aisle("AAAAA", "ARRRA").where('R', CachedBlockInfo.hasState(BlockStateMatcher.forBlock(Blocks.REDSTONE_WIRE))).where('A', CachedBlockInfo.hasState(BlockStateMatcher.forBlock(Blocks.AIR)))
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
					.where('R', CachedBlockInfo.hasState(BlockStateMatcher.forBlock(Blocks.REDSTONE_WIRE))).aisle("RBR")
					.where('B', CachedBlockInfo.hasState(BlockStateMatcher.forBlock(Blocks.BOOKSHELF)))
					.where('R', CachedBlockInfo.hasState(BlockStateMatcher.forBlock(Blocks.REDSTONE_WIRE))).aisle("RRR")
					.where('R', CachedBlockInfo.hasState(BlockStateMatcher.forBlock(Blocks.REDSTONE_WIRE))).build();
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
					.where('I', CachedBlockInfo.hasState(BlockStateMatcher.forBlock(Blocks.IRON_BARS)))
					.where('V', CachedBlockInfo.hasState(BlockStateMatcher.forBlock(BlockInit.venous_stone.get())))
					.build();
		}
		return bookPattern;
	}

}
