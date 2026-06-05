package com.vincenthuto.hemomancy.common.network.capa.harbinger;

public final class BloodStructureVisiblePositionRules {
	private BloodStructureVisiblePositionRules() {
	}

	public static boolean isVisiblePatternCell(String[][] patternArray, int x, int y, int z) {
		if (patternArray == null || z < 0 || z >= patternArray.length) {
			return false;
		}
		String[] aisle = patternArray[z];
		if (aisle == null || y < 0 || y >= aisle.length) {
			return false;
		}
		String row = aisle[y];
		if (row == null || x < 0 || x >= row.length()) {
			return false;
		}
		return row.charAt(x) != ' ';
	}
}
