package com.vincenthuto.hemomancy.client.screen.skilltree.shared;

import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.rite.floor.CardinalRiteFloorDefinition;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

final class RitualFloorIcon {
	private RitualFloorIcon() {}

	static ItemStack resolve(CardinalRiteFloorDefinition floor) {
		Map<String, Block> symbols = floor.pattern().getSymbolList();
		String focusSymbol = symbols.entrySet().stream()
				.filter(entry -> entry.getValue() == BlockInit.cardinal_focus.get())
				.map(Map.Entry::getKey)
				.findFirst()
				.orElse(null);
		String dominant = dominantSymbol(floor.pattern().getPatternArray(), focusSymbol, symbols.keySet());
		Block block = dominant == null ? null : symbols.get(dominant);
		return block == null ? ItemStack.EMPTY : new ItemStack(block);
	}

	static String dominantSymbol(String[][] pattern, String excludedSymbol, Set<String> allowedSymbols) {
		Map<String, Integer> counts = new LinkedHashMap<>();
		for (String[] slice : pattern) {
			for (String row : slice) {
				for (int index = 0; index < row.length(); index++) {
					String symbol = String.valueOf(row.charAt(index));
					if (symbol.isBlank() || symbol.equals(excludedSymbol) || !allowedSymbols.contains(symbol)) continue;
					counts.merge(symbol, 1, Integer::sum);
				}
			}
		}
		String dominant = null;
		int highestCount = 0;
		for (Map.Entry<String, Integer> entry : counts.entrySet()) {
			if (entry.getValue() > highestCount) {
				dominant = entry.getKey();
				highestCount = entry.getValue();
			}
		}
		return dominant;
	}
}
