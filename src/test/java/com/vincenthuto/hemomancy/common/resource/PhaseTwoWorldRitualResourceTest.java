package com.vincenthuto.hemomancy.common.resource;

import java.nio.file.Files;
import java.nio.file.Path;

public final class PhaseTwoWorldRitualResourceTest {
	private static final Path ROOT = Path.of("src/main/resources");
	private static final Path DATA = ROOT.resolve("data/hemomancy");
	private static final Path ASSETS = ROOT.resolve("assets/hemomancy");

	private PhaseTwoWorldRitualResourceTest() {
	}

	public static void main(String[] args) {
		for (String id : new String[] { "mnemonic_candle", "blood_wood_leaves" }) {
			require(ASSETS.resolve("blockstates/" + id + ".json"));
			require(ASSETS.resolve("models/block/" + id + ".json"));
			require(ASSETS.resolve("models/item/" + id + ".json"));
			require(ASSETS.resolve("textures/block/" + id + ".png"));
		}
		require(ASSETS.resolve("textures/item/mnemonic_candle.png"));
		require(ASSETS.resolve("textures/mob_effect/mnemonic_candle_aura.png"));
		require(DATA.resolve("recipe/mnemonic_candle.json"));
		require(DATA.resolve("loot_table/blocks/mnemonic_candle.json"));
		require(DATA.resolve("loot_table/blocks/blood_wood_leaves.json"));
	}

	private static void require(Path path) {
		if (!Files.exists(path)) {
			throw new AssertionError("missing " + path);
		}
	}
}
