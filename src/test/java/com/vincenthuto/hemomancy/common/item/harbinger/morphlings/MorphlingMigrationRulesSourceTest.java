package com.vincenthuto.hemomancy.common.item.harbinger.morphlings;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class MorphlingMigrationRulesSourceTest {
	private static final Path SOURCE = Path.of(
			"src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/morphlings/MorphlingMigrationRules.java");
	private static final Pattern ENTRY = Pattern.compile(
			"entry\\(\"([^\"]+)\",\\s*\"([^\"]+)\"\\)");
	private static final String[][] EXPECTED_ID_MIGRATIONS = {
			{ "morphling_leeches", "morphling_deadmans_purse" },
			{ "morphling_fungal", "morphling_gravecap" },
			{ "morphling_bat", "morphling_witchs_ear" },
			{ "morphling_cuttlefish", "morphling_lumenlace" },
			{ "morphling_foxfire", "morphling_lumenlace" },
			{ "morphling_spider", "morphling_bootlace" },
			{ "morphling_mole", "morphling_irontooth" },
			{ "morphling_serpent", "morphling_emberfang" },
			{ "morphling_centipede", "morphling_winter_shroud" },
			{ "morphling_chitinite", "morphling_winter_shroud" },
			{ "morphling_urchin", "morphling_winter_shroud" },
			{ "morphling_pests", "morphling_gravecap" },
			{ "morphling_tick", "morphling_deadmans_purse" }
	};

	private MorphlingMigrationRulesSourceTest() {
	}

	public static void main(String[] args) throws IOException {
		allLegacyMorphlingIdsHaveExplicitTargets();
		cutMorphlingsAreCoveredBySurvivorTargets();
		stackMigrationPreservesCountAndAllComponents();
		layerFamilyMigrationSupportsStoredBareNamesAndItemPathNames();
		layerFamilyMigrationSupportsSlashPathishIds();
		migrationDoesNotClampProgressionData();
	}

	private static void allLegacyMorphlingIdsHaveExplicitTargets() throws IOException {
		Map<String, String> entries = migrationEntries(readSource());
		for (String[] migration : EXPECTED_ID_MIGRATIONS) {
			assertEquals("migration for " + migration[0], migration[1], entries.get(migration[0]));
		}
		assertEquals("legacy morphling IDs have explicit migration sources",
				EXPECTED_ID_MIGRATIONS.length, entries.size());
	}

	private static void cutMorphlingsAreCoveredBySurvivorTargets() throws IOException {
		Map<String, String> entries = migrationEntries(readSource());
		assertEquals("chitinite cuts to winter shroud", "morphling_winter_shroud",
				entries.get("morphling_chitinite"));
		assertEquals("urchin cuts to winter shroud", "morphling_winter_shroud",
				entries.get("morphling_urchin"));
		assertEquals("pests cuts to gravecap", "morphling_gravecap",
				entries.get("morphling_pests"));
		assertEquals("tick cuts to deadman's purse", "morphling_deadmans_purse",
				entries.get("morphling_tick"));
	}

	private static void stackMigrationPreservesCountAndAllComponents() throws IOException {
		String source = readSource();
		assertContains("stack migration API", source, "ItemStack migrateStack(ItemStack old)");
		assertContains("empty stacks are returned unchanged", source, "old.isEmpty()");
		assertContains("unmapped stacks are returned unchanged", source, "return old;");
		assertContains("migrated stack uses original count", source, "old.getCount()");
		assertContains("migrated stack copies all data component changes", source, "old.getComponentsPatch()");
		assertContains("migrated stack applies the copied component patch", source, "applyComponents");
	}

	private static void layerFamilyMigrationSupportsStoredBareNamesAndItemPathNames() throws IOException {
		String source = readSource();
		assertContains("layer migration API", source, "String migrateLayerFamily(String old)");
		assertContains("bare tick family maps to bare deadman's purse", source,
				"entry(\"tick\", \"deadmans_purse\")");
		assertContains("bare pests family maps to bare gravecap", source,
				"entry(\"pests\", \"gravecap\")");
		assertContains("full centipede path maps to bare winter shroud", source,
				"entry(\"morphling_centipede\", \"winter_shroud\")");
		assertContains("morphling prefix is normalized", source, "morphling_");
	}

	private static void layerFamilyMigrationSupportsSlashPathishIds() throws IOException {
		String source = readSource();
		assertContains("slash path-ish layer values are reduced to the final path segment", source,
				"lastIndexOf('/')");
	}

	private static void migrationDoesNotClampProgressionData() throws IOException {
		String source = readSource();
		assertContains("enzyme power key is intentionally preserved by component copy", source, "EnzymePower");
		assertContains("primal marker key is intentionally preserved by component copy", source, "Primalized");
		assertContains("wild-bound marker key is intentionally preserved by component copy", source, "WildBound");
		assertDoesNotContain("migration must not recalculate maturity", source, "setPrimalized(");
		assertDoesNotContain("migration must not recalculate wild-bound data", source, "setWildBound(");
		assertDoesNotContain("migration must not write enzyme power", source, "putFloat(\"EnzymePower\"");
	}

	private static Map<String, String> migrationEntries(String source) {
		Matcher matcher = ENTRY.matcher(idMigrationMapSource(source));
		Map<String, String> entries = new HashMap<>();
		while (matcher.find()) {
			String oldPath = matcher.group(1);
			if (oldPath.startsWith("morphling_")) {
				entries.put(oldPath, matcher.group(2));
			}
		}
		return entries;
	}

	private static String idMigrationMapSource(String source) {
		int start = source.indexOf("ID_MIGRATIONS");
		if (start < 0) {
			throw new AssertionError("missing ID_MIGRATIONS map");
		}
		int end = source.indexOf(");", start);
		if (end < 0) {
			throw new AssertionError("unterminated ID_MIGRATIONS map");
		}
		return source.substring(start, end);
	}

	private static String readSource() throws IOException {
		if (!Files.exists(SOURCE)) {
			throw new AssertionError("missing " + SOURCE);
		}
		return Files.readString(SOURCE).replace("\r\n", "\n");
	}

	private static void assertContains(String label, String text, String expected) {
		if (!text.contains(expected)) {
			throw new AssertionError(label + ": missing " + expected);
		}
	}

	private static void assertDoesNotContain(String label, String text, String unexpected) {
		if (text.contains(unexpected)) {
			throw new AssertionError(label + ": found " + unexpected);
		}
	}

	private static void assertEquals(String label, Object expected, Object actual) {
		if (!expected.equals(actual)) {
			throw new AssertionError(label + ": expected " + expected + " but got " + actual);
		}
	}
}
