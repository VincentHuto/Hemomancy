package com.huto.hemomancy.recipes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.pattern.BlockPattern;
import net.minecraft.util.math.BlockPos;

public class BloodCraftingBundledPattern {

	BlockPattern pattern;
	HashMap<Character, Block> symbolList;
	// This array is formatted with each String[] is 1 aisle in the BlockPattern
	// While this may seem redundant when already using BlockPattern, it is so I can
	// get relative block placement when rendering in GUI
	// AKA the book pattern of RRR RBR RRR would need 3 String[1] "3 Aisle thick, 1
	// tall"
	// The pattern for the living staff would be 1 String[3] "1 Aisle thick, 3 tall"
	String[][] patternArray;

	public BloodCraftingBundledPattern(BlockPattern pattern, HashMap<Character, Block> symbolList,
			String[][] patternArray) {
		this.pattern = pattern;
		this.symbolList = symbolList;
		this.patternArray = patternArray;
	}

	public BlockPattern getBlockPattern() {
		return pattern;
	}

	public void setPattern(BlockPattern pattern) {
		this.pattern = pattern;
	}

	public HashMap<Character, Block> getSymbolList() {
		return symbolList;
	}

	public String[][] getPatternArray() {
		return patternArray;
	}

	public List<BlockPos> getRelativeBlockPosList() {
		List<BlockPos> blockList = new ArrayList<>();
		for (int T = 0; T < patternArray.length; T++) {
			String[] currentAisle = patternArray[T];
			int height = currentAisle.length;
			System.out.println();
			for (int i = 0; i < height; i++) {
				for (int j = 0; j < currentAisle[i].toCharArray().length; j++) {
					blockList.add(new BlockPos(j, (height - i - 1), T));
				}
			}
		}
		return blockList;
	}

	public List<Block> getRelativeBlockList() {
		List<Block> blockList = new ArrayList<>();
		for (int T = 0; T < patternArray.length; T++) {
			String[] currentAisle = patternArray[T];
			int height = currentAisle.length;
			System.out.println();
			for (int i = 0; i < height; i++) {
				for (int j = 0; j < currentAisle[i].toCharArray().length; j++) {
					blockList.add(symbolList.get(currentAisle[i].toCharArray()[j]));
				}
			}
		}
		return blockList;
	}

	public List<BlockPosBlockPair> getBlockPosBlockList() {
		List<BlockPosBlockPair> list = new ArrayList<BlockPosBlockPair>();
		// Block
		for (int T = 0; T < patternArray.length; T++) {
			String[] currentAisle = patternArray[T];
			int height = currentAisle.length;
			for (int i = 0; i < height; i++) {
				for (int j = 0; j < currentAisle[i].toCharArray().length; j++) {
					list.add(new BlockPosBlockPair(symbolList.get(currentAisle[i].toCharArray()[j]),
							new BlockPos(j, (height - i - 1), T)));
				}
			}
		}
		return list;
	}

	public void printMultiblockLayout() {
		for (int T = 0; T < patternArray.length; T++) {
			String[] currentAisle = patternArray[T];
			int height = currentAisle.length;
			System.out.println();
			for (int i = 0; i < height; i++) {
				for (int j = 0; j < currentAisle[i].toCharArray().length; j++) {
					String coords = "(X:" + j + ",Y:" + (height - i - 1) + ",Z:" + T + ")";
					System.out.print(coords + ": " + symbolList.get(currentAisle[i].toCharArray()[j]) + "\t");
				}
				System.out.println("");
			}
		}
	}

}
