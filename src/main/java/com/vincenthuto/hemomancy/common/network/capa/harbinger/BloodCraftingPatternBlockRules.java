package com.vincenthuto.hemomancy.common.network.capa.harbinger;

import com.vincenthuto.hutoslib.math.MultiblockPattern;
import com.vincenthuto.hutoslib.math.MultiblockPatternKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Map;

final class BloodCraftingPatternBlockRules {
	private BloodCraftingPatternBlockRules() {
	}

	static boolean patternMayContainBlock(MultiblockPattern pattern, BlockState hitState) {
		if (pattern == null || hitState == null) {
			return true;
		}
		Map<String, MultiblockPatternKey> keyList = pattern.getKeyList();
		String[][] patternArray = pattern.getPatternArray();
		if (keyList == null || keyList.isEmpty() || patternArray == null) {
			return true;
		}

		for (Map.Entry<String, MultiblockPatternKey> entry : keyList.entrySet()) {
			String symbol = entry.getKey();
			MultiblockPatternKey key = entry.getValue();
			if (key == null || key.isAir()
					|| !BloodCraftingPatternSearchRules.patternUsesSymbol(patternArray, symbol)) {
				continue;
			}
			if (keyMatchesState(key, hitState)) {
				return true;
			}
		}
		return false;
	}

	private static boolean keyMatchesState(MultiblockPatternKey key, BlockState state) {
		if (key.isTag()) {
			TagKey<Block> tag = TagKey.create(Registries.BLOCK, key.tagId());
			return state.is(tag) || state.is(key.fallbackBlock());
		}
		return state.is(key.fallbackBlock());
	}
}
