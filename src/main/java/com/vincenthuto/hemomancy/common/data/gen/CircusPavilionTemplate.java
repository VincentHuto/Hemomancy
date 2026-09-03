package com.vincenthuto.hemomancy.common.data.gen;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class CircusPavilionTemplate {
	static final int WIDTH = 33;
	static final int HEIGHT = 12;
	static final int DEPTH = 33;
	static final int CENTER = 16;

	private CircusPavilionTemplate() {
	}

	static List<BlockPlacement> blocks() {
		Map<Position, BlockPlacement> blocks = new LinkedHashMap<>();

		for (int x = 0; x < WIDTH; x++) {
			for (int z = 0; z < DEPTH; z++) {
				int dx = x - CENTER;
				int dz = z - CENTER;
				if (dx * dx + dz * dz <= 225 || (x >= CENTER - 1 && x <= CENTER + 1 && z <= CENTER)) {
					place(blocks, x, 0, z, "hemomancy:polished_venous_stone");
				}
			}
		}

		int[][] supports = {
				{ 4, 16 }, { 28, 16 }, { 12, 4 }, { 20, 4 }, { 16, 28 },
				{ 8, 8 }, { 24, 8 }, { 8, 24 }, { 24, 24 }
		};
		for (int[] support : supports) {
			place(blocks, support[0], 0, support[1], "hemomancy:venous_stone");
			for (int y = 1; y <= 8; y++) {
				place(blocks, support[0], y, support[1], "hemomancy:hematic_iron_pillar");
			}
		}

		for (int x = 1; x < WIDTH - 1; x++) {
			for (int z = 1; z < DEPTH - 1; z++) {
				int dx = x - CENTER;
				int dz = z - CENTER;
				int distanceSqr = dx * dx + dz * dz;
				if (distanceSqr > 225) continue;
				int y = distanceSqr >= 169 ? 7 : distanceSqr >= 100 ? 8
						: distanceSqr >= 49 ? 9 : distanceSqr >= 36 ? 10 : 11;
				String cloth = (x + z) % 17 == 0 ? "minecraft:blue_wool"
						: (x - z + 34) % 13 == 0 ? "minecraft:red_wool"
						: "hemomancy:puppeteers_wool";
				place(blocks, x, y, z, cloth);
			}
		}

		place(blocks, 12, 1, 16, "hemomancy:puppeteers_spindle");
		for (int y = 6; y <= 10; y++) {
			place(blocks, 12, y, 16, "minecraft:chain");
			place(blocks, 20, y, 16, "minecraft:chain");
		}
		place(blocks, 12, 5, 16, "minecraft:lantern");
		place(blocks, 20, 5, 16, "minecraft:lantern");

		for (int y = 1; y <= 3; y++) {
			place(blocks, 9, y, 24, y == 2 ? "minecraft:blue_wool" : "minecraft:red_wool");
			place(blocks, 23, y, 24, y == 2 ? "minecraft:red_wool" : "minecraft:blue_wool");
		}
		for (int x = 9; x <= 23; x++) {
			place(blocks, x, 7, 21, "hemomancy:hematic_iron_pillar");
		}
		for (int z = 21; z <= 24; z++) {
			place(blocks, 8, 7, z, "hemomancy:hematic_iron_pillar");
			place(blocks, 24, 7, z, "hemomancy:hematic_iron_pillar");
		}
		place(blocks, 12, 1, 3, "minecraft:bone_block");
		place(blocks, 20, 1, 3, "minecraft:bone_block");
		place(blocks, 23, 2, 24, "minecraft:target");
		for (int y = 6; y <= 10; y++) {
			place(blocks, 20, y, 12, "minecraft:chain");
		}

		return List.copyOf(blocks.values());
	}

	static List<PerformerPlacement> performers() {
		return List.of(
				new PerformerPlacement("hemomancy:circus_fire_eater", 12.5D, 1.0D, 12.5D),
				new PerformerPlacement("hemomancy:circus_acrobat", 20.5D, 1.0D, 12.5D),
				new PerformerPlacement("hemomancy:circus_stilt_walker", 12.5D, 1.0D, 20.5D),
				new PerformerPlacement("hemomancy:circus_knife_thrower", 20.5D, 1.0D, 20.5D));
	}

	static PerformerPlacement carousel() {
		return new PerformerPlacement("hemomancy:circus_carousel", 16.5D, 1.0D, 16.5D);
	}

	static PerformerPlacement ringmaster() {
		return new PerformerPlacement("hemomancy:circus_ringmaster", 16.5D, 8.0D, 21.5D);
	}

	private static void place(Map<Position, BlockPlacement> blocks, int x, int y, int z, String name) {
		blocks.put(new Position(x, y, z), new BlockPlacement(x, y, z, name));
	}

	record BlockPlacement(int x, int y, int z, String name) {
	}

	record PerformerPlacement(String entityId, double x, double y, double z) {
	}

	private record Position(int x, int y, int z) {
	}
}
