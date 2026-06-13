package com.vincenthuto.hemomancy.common.resource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

public final class PhaseOneUtilityResourceTest {
	private static final Path ROOT = Path.of("src/main/resources");
	private static final Path DATA = ROOT.resolve("data/hemomancy");
	private static final Path ASSETS = ROOT.resolve("assets/hemomancy");

	private PhaseOneUtilityResourceTest() {
	}

	public static void main(String[] args) throws IOException {
		assertUnstainedLootDoesNotDropSanguineBlob();
		assertDuplicateIncubatorFungalScarsRemoved();
		assertDicentraSapIsLegacyOnly();
		assertPhaseOneUtilityAssetsExist();
	}

	private static void assertUnstainedLootDoesNotDropSanguineBlob() throws IOException {
		for (String entity : new String[] { "unstained_acolyte", "unstained_guardian", "unstained_zealot" }) {
			String loot = read(DATA.resolve("loot_table/entities/" + entity + ".json"));
			assertNotContains(entity + " should not drop the Harbinger cosmetic blob", loot,
					"hemomancy:sanguine_blob");
		}
	}

	private static void assertDuplicateIncubatorFungalScarsRemoved() {
		for (String id : new String[] { "respergillus", "talaromyces_minus", "noctifly_agaric", "lumina_devorans" }) {
			Path incubator = DATA.resolve("recipe/incubator/" + id + ".json");
			if (Files.exists(incubator)) {
				throw new AssertionError("duplicate incubator fungal scar recipe still exists: " + incubator);
			}
			Path crucible = DATA.resolve("recipe/fungal_scar/" + id + ".json");
			if (!Files.exists(crucible)) {
				throw new AssertionError("fungal scar crucible recipe missing: " + crucible);
			}
		}
	}

	private static void assertDicentraSapIsLegacyOnly() throws IOException {
		assertContains("legacy dicentra sap distillation should remain",
				read(DATA.resolve("recipe/distillation/dicentra_sap.json")), "hemomancy:bleeding_bulb");
		try (Stream<Path> paths = Files.walk(DATA)) {
			for (Path path : paths.filter(Files::isRegularFile)
					.filter(path -> path.toString().endsWith(".json")).toList()) {
				String normalized = path.toString().replace('\\', '/');
				String text = read(path);
				if (text.contains("hemomancy:dicentra_sap")
						&& !normalized.endsWith("recipe/distillation/dicentra_sap.json")) {
					throw new AssertionError("non-legacy dicentra sap data reference remains: " + path);
				}
			}
		}
	}

	private static void assertPhaseOneUtilityAssetsExist() throws IOException {
		for (String id : new String[] { "blood_chum", "ossuary_clock", "humoral_barometer" }) {
			assertJsonExists(DATA.resolve("recipe/" + id + ".json"));
			assertJsonExists(ASSETS.resolve("models/item/" + id + ".json"));
			assertFileExists(ASSETS.resolve("textures/item/" + id + ".png"));
		}
		for (String id : new String[] { "ossuary_clock", "humoral_barometer" }) {
			assertJsonExists(ASSETS.resolve("blockstates/" + id + ".json"));
			assertJsonExists(ASSETS.resolve("models/block/" + id + ".json"));
			assertFileExists(ASSETS.resolve("textures/block/" + id + ".png"));
		}
	}

	private static void assertJsonExists(Path path) throws IOException {
		assertFileExists(path);
		read(path);
	}

	private static void assertFileExists(Path path) {
		if (!Files.exists(path)) {
			throw new AssertionError("missing " + path);
		}
	}

	private static String read(Path path) throws IOException {
		return Files.readString(path).replace("\r\n", "\n");
	}

	private static void assertContains(String label, String text, String expected) {
		if (!text.contains(expected)) {
			throw new AssertionError(label + ": missing " + expected);
		}
	}

	private static void assertNotContains(String label, String text, String forbidden) {
		if (text.contains(forbidden)) {
			throw new AssertionError(label + ": found " + forbidden);
		}
	}
}
