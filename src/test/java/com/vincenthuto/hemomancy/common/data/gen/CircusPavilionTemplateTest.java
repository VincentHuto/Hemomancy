package com.vincenthuto.hemomancy.common.data.gen;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class CircusPavilionTemplateTest {
	public static void main(String[] args) {
		List<CircusPavilionTemplate.BlockPlacement> blocks = CircusPavilionTemplate.blocks();
		Set<String> positions = new HashSet<>();
		Set<String> palette = new HashSet<>();

		for (CircusPavilionTemplate.BlockPlacement block : blocks) {
			assert block.x() >= 0 && block.x() < CircusPavilionTemplate.WIDTH;
			assert block.y() >= 0 && block.y() < CircusPavilionTemplate.HEIGHT;
			assert block.z() >= 0 && block.z() < CircusPavilionTemplate.DEPTH;
			assert positions.add(block.x() + ":" + block.y() + ":" + block.z()) : "duplicate block position";
			palette.add(block.name());
		}

		assert palette.contains("hemomancy:puppeteers_wool");
		assert palette.contains("hemomancy:puppeteers_spindle");
		assert palette.contains("hemomancy:hematic_iron_pillar");
		assert palette.contains("hemomancy:polished_venous_stone");
		assert blocks.size() >= 700 : "pavilion is missing substantial physical structure";

		for (int z = 0; z <= CircusPavilionTemplate.CENTER; z++) {
			for (int y = 1; y <= 2; y++) {
				assert !positions.contains(CircusPavilionTemplate.CENTER + ":" + y + ":" + z)
						: "central entrance aisle is obstructed";
			}
		}
	}
}
