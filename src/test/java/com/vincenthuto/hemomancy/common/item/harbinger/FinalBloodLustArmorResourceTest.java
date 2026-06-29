package com.vincenthuto.hemomancy.common.item.harbinger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class FinalBloodLustArmorResourceTest {
	private static final Path SOURCE_ROOT = Path.of("src/main/java");
	private static final Path RESOURCE_ROOT = Path.of("src/main/resources");
	private static final String[] LINEAGES = {
			"edacious_blood_lust",
			"sheolic_blood_lust",
			"phantasmal_blood_lust"
	};
	private static final String[] SLOTS = {"helm", "chestplate", "leggings", "boots"};

	private FinalBloodLustArmorResourceTest() {
	}

	public static void main(String[] args) throws IOException {
		String itemInit = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/init/ItemInit.java"));
		String lang = read(RESOURCE_ROOT.resolve("assets/hemomancy/lang/en_us.json"));

		for (String lineage : LINEAGES) {
			for (String slot : SLOTS) {
				String id = lineage + "_" + resultSlot(slot);
				assertContains(id + " should be registered", itemInit,
						"BASEITEMS.register(\"" + id + "\"");
				assertContains(id + " should use Blood Lust armor behavior", itemInit,
						"new BloodLustArmorItem(EnumModArmorTiers.BLOODLUST.holder(), ArmorItem.Type."
								+ armorType(slot));
				assertContains(id + " should have a lang entry", lang,
						"\"item.hemomancy." + id + "\":");
				assertExists(RESOURCE_ROOT.resolve("assets/hemomancy/models/item/" + id + ".json"));
			}
		}

		assertNotContains("smiling mask should not be a dedicated item", itemInit, "smiling_mask");
		assertNotContains("smiling mask should not have a lang entry", lang, "item.hemomancy.smiling_mask");
		assertMissing(RESOURCE_ROOT.resolve("assets/hemomancy/models/item/smiling_mask.json"));
	}

	private static String resultSlot(String slot) {
		return switch (slot) {
			case "chestplate" -> "chest";
			case "leggings" -> "legs";
			case "boots" -> "boots";
			default -> "helm";
		};
	}

	private static String armorType(String slot) {
		return switch (slot) {
			case "chestplate" -> "CHESTPLATE";
			case "leggings" -> "LEGGINGS";
			case "boots" -> "BOOTS";
			default -> "HELMET";
		};
	}

	private static String read(Path path) throws IOException {
		if (!Files.exists(path)) {
			throw new AssertionError("missing " + path);
		}
		return Files.readString(path).replace("\r\n", "\n");
	}

	private static void assertExists(Path path) {
		if (!Files.exists(path)) {
			throw new AssertionError("missing " + path);
		}
	}

	private static void assertMissing(Path path) {
		if (Files.exists(path)) {
			throw new AssertionError("unexpected " + path);
		}
	}

	private static void assertContains(String label, String text, String expected) {
		if (!text.contains(expected)) {
			throw new AssertionError(label + ": missing " + expected);
		}
	}

	private static void assertNotContains(String label, String text, String unexpected) {
		if (text.contains(unexpected)) {
			throw new AssertionError(label + ": still contains " + unexpected);
		}
	}
}
