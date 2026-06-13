package com.vincenthuto.hemomancy.common.loot;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class EntityLootTableCoverageResourceTest {
	private static final Path ENTITY_LOOT_ROOT = Path
			.of("src/main/resources/data/hemomancy/loot_table/entities");
	private static final Pattern MIN_PATTERN = Pattern.compile("\"min\"\\s*:\\s*([0-9]+(?:\\.[0-9]+)?)");

	private static final GuaranteedDrop[] GUARANTEED_MATERIAL_DROPS = {
			new GuaranteedDrop("abhorent_thought", "hemomancy:sanguine_formation"),
			new GuaranteedDrop("abyssal_siphon", "hemomancy:abyssal_ichor"),
			new GuaranteedDrop("barbed_urchin", "hemomancy:calcified_blood_spine"),
			new GuaranteedDrop("blood_drunk_puppeteer", "hemomancy:puppeteering_thread"),
			new GuaranteedDrop("blood_thrall", "hemomancy:sanguine_formation"),
			new GuaranteedDrop("chitinite", "hemomancy:chitinous_husk"),
			new GuaranteedDrop("chthonian", "hemomancy:chitinous_husk"),
			new GuaranteedDrop("chthonian_queen", "hemomancy:chitinous_husk"),
			new GuaranteedDrop("crimson_doe", "hemomancy:bleeding_bulb"),
			new GuaranteedDrop("cruor_fiend", "hemomancy:molten_scab"),
			new GuaranteedDrop("dessicant", "hemomancy:desiccated_membrane"),
			new GuaranteedDrop("enthralled_doll", "hemomancy:bleeding_bulb"),
			new GuaranteedDrop("erythromycelium_eruptus", "hemomancy:spore_sac"),
			new GuaranteedDrop("fargone", "hemomancy:sanguine_formation"),
			new GuaranteedDrop("fervent_chitinite", "hemomancy:fervent_husk"),
			new GuaranteedDrop("frozen_clot", "hemomancy:frozen_clot"),
			new GuaranteedDrop("fungling", "hemomancy:spore_sac"),
			new GuaranteedDrop("harbinger_alchemist", "hemomancy:hemolytic_solution"),
			new GuaranteedDrop("harbinger_vicar", "hemomancy:sanguine_formation"),
			new GuaranteedDrop("hematic_construct", "hemomancy:hematic_iron_scrap"),
			new GuaranteedDrop("hemojelly", "hemomancy:vivacious_enzyme"),
			new GuaranteedDrop("hemolymphopoda", "hemomancy:cleansing_hemolymph"),
			new GuaranteedDrop("hollow_vessel", "hemomancy:sanguine_quintessence"),
			new GuaranteedDrop("leech", "hemomancy:swollen_leech"),
			new GuaranteedDrop("lump_of_thought", "hemomancy:sanguine_formation"),
			new GuaranteedDrop("morphling_polyp", "hemomancy:morphling_polyp"),
			new GuaranteedDrop("mycophant", "hemomancy:mycophant_tendril"),
			new GuaranteedDrop("myelin_borer", "hemomancy:nerve_bundle"),
			new GuaranteedDrop("scarlet_serpent", "hemomancy:serpent_scale"),
			new GuaranteedDrop("seraphae", "hemomancy:hallowed_residuum_seraphae"),
			new GuaranteedDrop("seraphae_fragment", "hemomancy:sanguine_formation"),
			new GuaranteedDrop("synapse_hound", "hemomancy:sanguine_formation"),
			new GuaranteedDrop("thirster", "hemomancy:sanguine_formation"),
			new GuaranteedDrop("tooth_pecks", "minecraft:bone_meal"),
			new GuaranteedDrop("venous_strider", "hemomancy:vivacious_spores"),
			new GuaranteedDrop("vesper_evening_star", "hemomancy:memory_of_vesper"),
			new GuaranteedDrop("void_drinker", "hemomancy:void_ichor") };

	private EntityLootTableCoverageResourceTest() {
	}

	public static void main(String[] args) throws IOException {
		assertNoLegacyLootingFunctions();
		for (GuaranteedDrop drop : GUARANTEED_MATERIAL_DROPS) {
			assertGuaranteedBaseDrop(drop);
		}
	}

	private static void assertNoLegacyLootingFunctions() throws IOException {
		try (var paths = Files.list(ENTITY_LOOT_ROOT)) {
			for (Path path : paths.filter(path -> path.toString().endsWith(".json")).toList()) {
				String lootTable = read(path);
				assertNotContains(path.getFileName() + " uses a 1.21 loot function", lootTable,
						"minecraft:looting_enchant");
			}
		}
	}

	private static void assertGuaranteedBaseDrop(GuaranteedDrop drop) throws IOException {
		Path path = ENTITY_LOOT_ROOT.resolve(drop.entity() + ".json");
		String lootTable = read(path);
		String entry = itemEntry(lootTable, drop.item());
		assertNotContains(drop.entity() + " has a real item entry", entry, "\"type\": \"minecraft:empty\"");

		String setCount = functionEntry(entry, "minecraft:set_count");
		if (setCount.isEmpty()) {
			return;
		}

		Matcher minMatcher = MIN_PATTERN.matcher(setCount);
		if (!minMatcher.find()) {
			throw new AssertionError(drop.entity() + " " + drop.item() + " set_count is missing a min value");
		}
		double min = Double.parseDouble(minMatcher.group(1));
		if (min < 1.0D) {
			throw new AssertionError(drop.entity() + " " + drop.item()
					+ " should guarantee at least one base drop, but min was " + min);
		}
	}

	private static String itemEntry(String lootTable, String item) {
		int nameIndex = lootTable.indexOf("\"name\": \"" + item + "\"");
		if (nameIndex < 0) {
			throw new AssertionError("missing entity loot item " + item);
		}
		int entryStart = lootTable.lastIndexOf('{', nameIndex);
		int entryEnd = findMatchingBrace(lootTable, entryStart);
		return lootTable.substring(entryStart, entryEnd + 1);
	}

	private static String functionEntry(String entry, String function) {
		int functionIndex = entry.indexOf("\"function\": \"" + function + "\"");
		if (functionIndex < 0) {
			return "";
		}
		int functionStart = entry.lastIndexOf('{', functionIndex);
		int functionEnd = findMatchingBrace(entry, functionStart);
		return entry.substring(functionStart, functionEnd + 1);
	}

	private static int findMatchingBrace(String text, int start) {
		if (start < 0 || text.charAt(start) != '{') {
			throw new AssertionError("invalid JSON object start");
		}
		int depth = 0;
		boolean inString = false;
		boolean escaped = false;
		for (int i = start; i < text.length(); i++) {
			char c = text.charAt(i);
			if (escaped) {
				escaped = false;
				continue;
			}
			if (c == '\\') {
				escaped = true;
				continue;
			}
			if (c == '"') {
				inString = !inString;
				continue;
			}
			if (inString) {
				continue;
			}
			if (c == '{') {
				depth++;
			} else if (c == '}') {
				depth--;
				if (depth == 0) {
					return i;
				}
			}
		}
		throw new AssertionError("unclosed JSON object");
	}

	private static String read(Path path) throws IOException {
		if (!Files.exists(path)) {
			throw new AssertionError("missing " + path);
		}
		return Files.readString(path).replace("\r\n", "\n");
	}

	private static void assertNotContains(String label, String text, String forbidden) {
		if (text.contains(forbidden)) {
			throw new AssertionError(label + ": found " + forbidden);
		}
	}

	private record GuaranteedDrop(String entity, String item) {
	}
}
