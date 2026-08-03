package com.vincenthuto.hemomancy.client.render.world;

import com.vincenthuto.hutoslib.math.MultiblockPattern;
import com.vincenthuto.hutoslib.math.MultiblockPatternKey;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public record MnemonicBlueprintPattern(List<Cell> cells, MnemonicBlueprintPlacement.Bounds bounds) {
	public MnemonicBlueprintPattern {
		cells = List.copyOf(cells);
	}

	public static MnemonicBlueprintPattern from(MultiblockPattern pattern) {
		if (pattern == null || pattern.getPatternArray() == null) {
			return new MnemonicBlueprintPattern(List.of(), new MnemonicBlueprintPlacement.Bounds(0, 0, 0, 0));
		}
		Map<String, MultiblockPatternKey> keys = pattern.getKeyList();
		List<Cell> cells = new ArrayList<>();
		int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE;
		int minZ = Integer.MAX_VALUE, maxZ = Integer.MIN_VALUE;
		String[][] aisles = pattern.getPatternArray();
		for (BlockPos position : coordinates(aisles, symbol -> {
			MultiblockPatternKey key = keys.get(symbol);
			return key != null && !key.isAir();
		})) {
			String[] aisle = aisles[position.getZ()];
			String row = aisle[aisle.length - position.getY() - 1];
			MultiblockPatternKey key = keys.get(String.valueOf(row.charAt(position.getX())));
			cells.add(new Cell(position, key));
			minX = Math.min(minX, position.getX()); maxX = Math.max(maxX, position.getX());
			minZ = Math.min(minZ, position.getZ()); maxZ = Math.max(maxZ, position.getZ());
		}
		if (cells.isEmpty()) minX = maxX = minZ = maxZ = 0;
		return new MnemonicBlueprintPattern(cells,
				new MnemonicBlueprintPlacement.Bounds(minX, maxX, minZ, maxZ));
	}

	public static boolean matches(MultiblockPatternKey key, BlockState state) {
		if (key == null || state == null) return false;
		if (key.isTag()) {
			TagKey<Block> tag = TagKey.create(Registries.BLOCK, key.tagId());
			return matchDecision(true, state.is(tag), state.is(key.fallbackBlock()));
		}
		return matchDecision(false, false, state.is(key.fallbackBlock()));
	}

	public static boolean matchDecision(boolean tagKey, boolean matchesTag, boolean matchesFallback) {
		return tagKey ? matchesTag || matchesFallback : matchesFallback;
	}

	public static List<BlockPos> coordinates(String[][] aisles, Predicate<String> includeSymbol) {
		List<BlockPos> positions = new ArrayList<>();
		if (aisles == null || includeSymbol == null) return positions;
		for (int z = 0; z < aisles.length; z++) {
			String[] aisle = aisles[z];
			if (aisle == null) continue;
			int height = aisle.length;
			for (int rowIndex = 0; rowIndex < height; rowIndex++) {
				String row = aisle[rowIndex];
				if (row == null) continue;
				for (int x = 0; x < row.length(); x++) {
					String symbol = String.valueOf(row.charAt(x));
					if (includeSymbol.test(symbol)) positions.add(new BlockPos(x, height - rowIndex - 1, z));
				}
			}
		}
		return positions;
	}

	public record Cell(BlockPos localPos, MultiblockPatternKey key) {
	}
}
