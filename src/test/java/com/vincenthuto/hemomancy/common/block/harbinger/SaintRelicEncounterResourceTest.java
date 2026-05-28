package com.vincenthuto.hemomancy.common.block.harbinger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class SaintRelicEncounterResourceTest {
	private static final Path SOURCE_ROOT = Path.of("src/main/java");
	private static final Path RESOURCE_ROOT = Path.of("src/main/resources");

	private static final Relic[] RELICS = {
			new Relic("saint_relic_hemorath", "HEMORATH", "Hemorath's Rib", "minecraft:bone",
					"hemomancy:vivacious_enzyme", "hemomancy:ruinous_enzyme"),
			new Relic("saint_relic_seraphae", "SERAPHAE", "Seraphae's Bound Eye", "minecraft:chain",
					"hemomancy:incandescent_enzyme", "hemomancy:neurotic_enzyme"),
			new Relic("saint_relic_putriciel", "PUTRICIEL", "Putriciel's Blighted Heart",
					"minecraft:rotten_flesh", "hemomancy:ruinous_enzyme", "hemomancy:fervent_enzyme"),
			new Relic("saint_relic_velorum", "VELORUM", "Velorum's Frozen Tongue", "minecraft:ice",
					"hemomancy:frigid_enzyme", "hemomancy:umbral_enzyme")
	};

	private SaintRelicEncounterResourceTest() {
	}

	public static void main(String[] args) throws IOException {
		String itemInit = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/init/ItemInit.java"));
		String sarcophagusBlock = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/block/harbinger/functional/SaintSarcophagusBlock.java"));
		String relicItem = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/item/harbinger/SaintRelicItem.java"));
		String lang = read(RESOURCE_ROOT.resolve("assets/hemomancy/lang/en_us.json"));
		String relicTag = read(RESOURCE_ROOT.resolve("data/hemomancy/tags/item/progression/saint_relics.json"));
		String mausoleumBiomes = read(RESOURCE_ROOT.resolve(
				"data/hemomancy/tags/worldgen/biome/has_structure/mausoleum.json"));

		assertContains("saint relic item stores a saint type", relicItem, "private final EnumSaintType saintType;");
		assertContains("saint relic item exposes its saint type", relicItem, "public EnumSaintType getSaintType()");
		assertContains("sarcophagus handles saint relics", sarcophagusBlock, "heldStack.getItem() instanceof SaintRelicItem relic");
		assertBefore("relic testing path should bypass consecration and offering gates", sarcophagusBlock,
				"heldStack.getItem() instanceof SaintRelicItem relic", "if (!sarcophagus.isConsecrated())");
		assertContains("sarcophagus sets the requested saint type", sarcophagusBlock,
				"sarcophagus.setSaintType(relic.getSaintType());");
		assertContains("sarcophagus consumes relics outside creative mode", sarcophagusBlock,
				"if (!player.getAbilities().instabuild)");
		assertContains("sarcophagus uses a relic-specific feedback message", sarcophagusBlock,
				"message.hemomancy.saint_sarcophagus.relic_summon");
		assertContains("sarcophagus reuses the existing encounter trigger", sarcophagusBlock,
				"triggerSaintEncounter(worldIn, pos, player, sarcophagus);");

		for (Relic relic : RELICS) {
			assertContains("item init registers " + relic.id, itemInit,
					relic.id + " = BASEITEMS.register(\"" + relic.id + "\"");
			assertContains("item init binds " + relic.id + " to " + relic.enumName, itemInit,
					"new SaintRelicItem(EnumSaintType." + relic.enumName);
			assertContains("language key for " + relic.id, lang,
					"\"item.hemomancy." + relic.id + "\": \"" + relic.displayName + "\"");
			assertContains("saint relic tag includes " + relic.id, relicTag,
					"\"hemomancy:" + relic.id + "\"");

			Path modelPath = RESOURCE_ROOT.resolve("assets/hemomancy/models/item/" + relic.id + ".json");
			Path texturePath = RESOURCE_ROOT.resolve("assets/hemomancy/textures/item/" + relic.id + ".png");
			Path recipePath = RESOURCE_ROOT.resolve("data/hemomancy/recipe/" + relic.id + ".json");
			assertExists("item model for " + relic.id, modelPath);
			assertExists("custom item texture for " + relic.id, texturePath);
			assertExists("crafting recipe for " + relic.id, recipePath);

			String model = read(modelPath);
			String recipe = read(recipePath);
			assertContains("item model points at custom texture for " + relic.id, model,
					"\"layer0\": \"hemomancy:item/" + relic.id + "\"");
			assertContains("recipe outputs " + relic.id, recipe,
					"\"id\": \"hemomancy:" + relic.id + "\"");
			assertContains("recipe uses first catalyst ingredient for " + relic.id, recipe,
					"\"item\": \"" + relic.ingredientA + "\"");
			assertContains("recipe uses second catalyst ingredient for " + relic.id, recipe,
					"\"item\": \"" + relic.ingredientB + "\"");
			assertContains("recipe uses third catalyst ingredient for " + relic.id, recipe,
					"\"item\": \"" + relic.ingredientC + "\"");
		}

		assertContains("mausoleum biome tag remains present but has no spawn biomes", mausoleumBiomes,
				"\"values\": []");
		assertExists("mausoleum structure data stays in place",
				RESOURCE_ROOT.resolve("data/hemomancy/worldgen/structure/mausoleum.json"));
		assertExists("mausoleum structure set data stays in place",
				RESOURCE_ROOT.resolve("data/hemomancy/worldgen/structure_set/mausoleum.json"));
	}

	private static String read(Path path) throws IOException {
		if (!Files.exists(path)) {
			throw new AssertionError("missing " + path);
		}
		return Files.readString(path).replace("\r\n", "\n");
	}

	private static void assertExists(String label, Path path) {
		if (!Files.exists(path)) {
			throw new AssertionError(label + ": missing " + path);
		}
	}

	private static void assertContains(String label, String text, String expected) {
		if (!text.contains(expected)) {
			throw new AssertionError(label + ": missing " + expected);
		}
	}

	private static void assertBefore(String label, String text, String first, String second) {
		int firstIndex = text.indexOf(first);
		int secondIndex = text.indexOf(second);
		if (firstIndex < 0) {
			throw new AssertionError(label + ": missing " + first);
		}
		if (secondIndex < 0) {
			throw new AssertionError(label + ": missing " + second);
		}
		if (firstIndex >= secondIndex) {
			throw new AssertionError(label + ": expected " + first + " before " + second);
		}
	}

	private record Relic(String id, String enumName, String displayName, String ingredientA, String ingredientB,
			String ingredientC) {
	}
}
