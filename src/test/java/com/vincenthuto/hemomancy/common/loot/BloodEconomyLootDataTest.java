package com.vincenthuto.hemomancy.common.loot;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class BloodEconomyLootDataTest {
	private static final Path DATA_ROOT = Path.of("src/main/resources/data");

	private BloodEconomyLootDataTest() {
	}

	public static void main(String[] args) throws IOException {
		String globalModifiers = read(DATA_ROOT.resolve("forge/loot_modifiers/global_loot_modifiers.json"));
		assertContains("global modifiers include venous stone blood rocks", globalModifiers,
				"hemomancy:blood_rocks_from_venous_stone");
		assertContains("global modifiers include blood mob bloody jugs", globalModifiers,
				"hemomancy:bloody_jugs_from_blood_mobs");

		String bloodRockModifier = read(DATA_ROOT.resolve("hemomancy/loot_modifier/blood_rocks_from_venous_stone.json"));
		assertContains("blood rocks come from venous stone", bloodRockModifier, "\"block\": \"hemomancy:venous_stone\"");
		assertContains("blood rock modifier drops blood rocks", bloodRockModifier, "\"item\": \"hemomancy:blood_rock\"");
		assertContains("blood rock drop chance is rare", bloodRockModifier, "\"chance\": 0.025");

		String jugModifier = read(DATA_ROOT.resolve("hemomancy/loot_modifier/bloody_jugs_from_blood_mobs.json"));
		assertContains("bloody jug drops require player kills", jugModifier, "\"condition\": \"minecraft:killed_by_player\"");
		assertContains("bloody jug modifier uses blood mob tag", jugModifier, "\"type\": \"#hemomancy:bloody_jug_drop_candidates\"");
		assertContains("bloody jug modifier drops bloody jugs", jugModifier, "\"item\": \"hemomancy:bloody_jug\"");
		assertContains("bloody jug drop chance is rare", jugModifier, "\"chance\": 0.02");

		String jugCandidates = read(DATA_ROOT.resolve("hemomancy/tags/entity_type/bloody_jug_drop_candidates.json"));
		assertContains("thirsters can drop bloody jugs", jugCandidates, "hemomancy:thirster");
		assertContains("blood drunk puppeteers can drop bloody jugs", jugCandidates, "hemomancy:blood_drunk_puppeteer");
	}

	private static String read(Path path) throws IOException {
		return Files.readString(path).replace("\r\n", "\n");
	}

	private static void assertContains(String label, String text, String expected) {
		if (!text.contains(expected)) {
			throw new AssertionError(label + ": missing " + expected);
		}
	}
}
