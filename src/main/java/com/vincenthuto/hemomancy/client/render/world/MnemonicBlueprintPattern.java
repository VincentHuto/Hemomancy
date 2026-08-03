package com.vincenthuto.hemomancy.client.render.world;

import com.vincenthuto.hutoslib.math.MultiblockPattern;
import com.vincenthuto.hutoslib.math.MultiblockPatternKey;
import com.vincenthuto.hemomancy.common.recipe.CardinalRiteRecipe;
import com.vincenthuto.hemomancy.common.rite.floor.CardinalRiteFloorDefinition;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
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

	public static MnemonicBlueprintPattern fromFloor(CardinalRiteFloorDefinition floor) {
		return floor == null ? empty() : translate(from(floor.pattern()), floor.focus().multiply(-1));
	}

	public static MnemonicBlueprintPattern fromRite(CardinalRiteRecipe rite) {
		if (rite == null) return empty();
		if (!rite.hasLayeredStation()) return from(rite.getPattern());
		MnemonicBlueprintPattern floor = from(rite.getFloorPattern());
		MultiblockPattern upperPattern = rite.getRequiredStructure();
		if (upperPattern == null) {
			return translate(floor, riteFloorFocus(rite).multiply(-1));
		}
		MnemonicBlueprintPattern upper = from(upperPattern);
		BlockPos upperAnchor = new BlockPos(upperPattern.getBlockPattern().getWidth() / 2,
				upperPattern.getBlockPattern().getHeight() - 1,
				upperPattern.getBlockPattern().getDepth() / 2);
		return composeRiteLayers(floor, riteFloorFocus(rite), upper, upperAnchor);
	}

	static MnemonicBlueprintPattern composeRiteLayers(MnemonicBlueprintPattern floor, BlockPos floorFocus,
			MnemonicBlueprintPattern upper, BlockPos upperAnchor) {
		List<Cell> combined = new ArrayList<>();
		BlockPos safeFloorFocus = floorFocus == null ? BlockPos.ZERO : floorFocus;
		BlockPos safeUpperAnchor = upperAnchor == null ? BlockPos.ZERO : upperAnchor;
		for (Cell cell : floor.cells()) {
			combined.add(new Cell(cell.localPos().subtract(safeFloorFocus), cell.key()));
		}
		for (Cell cell : upper.cells()) {
			combined.add(new Cell(cell.localPos().subtract(safeUpperAnchor).above(), cell.key()));
		}
		return withBounds(combined);
	}

	private static BlockPos riteFloorFocus(CardinalRiteRecipe rite) {
		return com.vincenthuto.hemomancy.common.rite.floor.CardinalRiteFloorRegistry.get(rite.getFloorId())
				.map(CardinalRiteFloorDefinition::focus).orElse(BlockPos.ZERO);
	}

	private static MnemonicBlueprintPattern translate(MnemonicBlueprintPattern source, BlockPos offset) {
		List<Cell> translated = source.cells().stream()
				.map(cell -> new Cell(cell.localPos().offset(offset), cell.key())).toList();
		return withBounds(translated);
	}

	private static MnemonicBlueprintPattern withBounds(List<Cell> cells) {
		if (cells.isEmpty()) return empty();
		int minX = cells.stream().mapToInt(cell -> cell.localPos().getX()).min().orElse(0);
		int maxX = cells.stream().mapToInt(cell -> cell.localPos().getX()).max().orElse(0);
		int minZ = cells.stream().mapToInt(cell -> cell.localPos().getZ()).min().orElse(0);
		int maxZ = cells.stream().mapToInt(cell -> cell.localPos().getZ()).max().orElse(0);
		return new MnemonicBlueprintPattern(cells, new MnemonicBlueprintPlacement.Bounds(minX, maxX, minZ, maxZ));
	}

	private static MnemonicBlueprintPattern empty() {
		return new MnemonicBlueprintPattern(List.of(), new MnemonicBlueprintPlacement.Bounds(0, 0, 0, 0));
	}

	public List<com.vincenthuto.hutoslib.math.BlockPosBlockPair> displayBlockPairs(long cycleIndex) {
		return cells.stream().map(cell -> new com.vincenthuto.hutoslib.math.BlockPosBlockPair(
				cell.key().displayBlock(cycleIndex), cell.localPos())).toList();
	}

	public List<MaterialCount> materialCounts(boolean sortAscending) {
		record Mutable(MultiblockPatternKey key, int count) {
			Mutable increment() { return new Mutable(key, count + 1); }
		}
		Map<String, Mutable> grouped = new LinkedHashMap<>();
		for (Cell cell : cells) {
			MultiblockPatternKey key = cell.key();
			if (key == null || key.isAir()) continue;
			String identity = key.isTag() ? "tag:" + key.tagId()
					: "block:" + BuiltInRegistries.BLOCK.getKey(key.fallbackBlock());
			grouped.compute(identity, (ignored, value) -> value == null ? new Mutable(key, 1) : value.increment());
		}
		java.util.Comparator<MaterialCount> comparator = java.util.Comparator.comparingInt(MaterialCount::count);
		if (!sortAscending) comparator = comparator.reversed();
		comparator = comparator.thenComparing(count -> count.key().displayLabel());
		return grouped.values().stream().map(value -> new MaterialCount(value.key(), value.count()))
				.sorted(comparator).toList();
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

	public record MaterialCount(MultiblockPatternKey key, int count) {
	}
}
