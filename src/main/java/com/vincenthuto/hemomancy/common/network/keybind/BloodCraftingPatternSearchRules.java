package com.vincenthuto.hemomancy.common.network.keybind;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

final class BloodCraftingPatternSearchRules {
	private static final int ORIENTATION_COUNT = 24;

	private BloodCraftingPatternSearchRules() {
	}

	static long estimatedWorstCaseBlockChecks(int width, int height, int depth) {
		int radius = searchRadius(width, height, depth);
		long diameter = radius * 2L + 1L;
		long candidates = diameter * diameter * diameter;
		long volume = Math.max(1L, (long) width * height * depth);
		return candidates * ORIENTATION_COUNT * volume;
	}

	static int compareDimensionsBySearchCost(
			int leftWidth, int leftHeight, int leftDepth,
			int rightWidth, int rightHeight, int rightDepth) {
		int byCost = Long.compare(
				estimatedWorstCaseBlockChecks(leftWidth, leftHeight, leftDepth),
				estimatedWorstCaseBlockChecks(rightWidth, rightHeight, rightDepth));
		if (byCost != 0) {
			return byCost;
		}
		int byMaxDim = Integer.compare(
				maxDimension(leftWidth, leftHeight, leftDepth),
				maxDimension(rightWidth, rightHeight, rightDepth));
		if (byMaxDim != 0) {
			return byMaxDim;
		}
		return Integer.compare(leftWidth * leftHeight * leftDepth, rightWidth * rightHeight * rightDepth);
	}

	static boolean patternUsesSymbol(String[][] patternArray, String symbol) {
		if (patternArray == null || symbol == null || symbol.length() != 1) {
			return false;
		}
		char target = symbol.charAt(0);
		for (String[] aisle : patternArray) {
			if (aisle == null) {
				continue;
			}
			for (String row : aisle) {
				if (row != null && row.indexOf(target) >= 0) {
					return true;
				}
			}
		}
		return false;
	}

	static <T> List<T> sortedByPatternSearchCost(List<T> values, Function<T, String[][]> patternGetter) {
		List<T> sorted = new ArrayList<>(values);
		sorted.sort(Comparator.comparingLong(value -> estimatedWorstCaseBlockChecks(patternGetter.apply(value))));
		return sorted;
	}

	private static int searchRadius(int width, int height, int depth) {
		return Math.max(0, maxDimension(width, height, depth) - 1);
	}

	private static int maxDimension(int width, int height, int depth) {
		return Math.max(Math.max(width, height), depth);
	}

	private static long estimatedWorstCaseBlockChecks(String[][] patternArray) {
		return estimatedWorstCaseBlockChecks(width(patternArray), height(patternArray), depth(patternArray));
	}

	private static int depth(String[][] patternArray) {
		return patternArray == null ? 0 : patternArray.length;
	}

	private static int height(String[][] patternArray) {
		if (patternArray == null) {
			return 0;
		}
		int height = 0;
		for (String[] aisle : patternArray) {
			if (aisle != null && aisle.length > height) {
				height = aisle.length;
			}
		}
		return height;
	}

	private static int width(String[][] patternArray) {
		if (patternArray == null) {
			return 0;
		}
		int width = 0;
		for (String[] aisle : patternArray) {
			if (aisle == null) {
				continue;
			}
			for (String row : aisle) {
				if (row != null && row.length() > width) {
					width = row.length();
				}
			}
		}
		return width;
	}
}
