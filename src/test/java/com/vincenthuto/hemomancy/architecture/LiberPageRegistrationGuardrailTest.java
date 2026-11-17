package com.vincenthuto.hemomancy.architecture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.vincenthuto.hemomancy.support.RepoPaths;
import com.vincenthuto.hemomancy.support.ResourceReader;
import com.vincenthuto.hemomancy.support.SourceAssertions;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

/**
 * CQ-1 (core-loop round two P9): {@code MemoBookFilter} hides every Liber page that {@code LiberEntryDefinitions} does
 * not register, so a page written but never registered is a page no player can read. Every page file of both books is
 * registered; every older page has a Degree floor or an opener; and the eight school pages of the Manipulations chapter
 * name only memories that exist, and every memory that does.
 */
class LiberPageRegistrationGuardrailTest {
	private static final String DISCOVERY = "common/capability/player/shared/knowledge/discovery/";
	private static final Pattern SANGUINIUM = Pattern.compile("[^A-Za-z]entry\\(\"([a-z_/]+)\"\\)");
	private static final Pattern IMMACULATUS = Pattern.compile("immaculatusEntry\\(\"([a-z_/]+)\"\\)");
	private static final Pattern UPGRADE_LESSON = Pattern.compile("registerUpgradeLesson\\(\"[a-z_/]+\",\\s*\"([a-z_/]+)\"\\)");
	private static final Pattern CONSTANT = Pattern.compile("public static final ResourceLocation ([A-Z_]+) = (?:entry|immaculatusEntry)\\(");
	/** Families the definitions register in a loop over a table, so no page path is written out: the prefix and why. */
	private static final Map<String, String> LOOPED_FAMILIES = Map.of(
			"libersanguinium/the_hematic_order/pages/troupe_", "one page per CircusCurriculum lesson",
			"libersanguinium/the_infection/pages/antecedent_", "one page per AntecedentResearch.Evidence fact",
			"libersanguinium/the_infection/pages/clinical_", "one page per ClinicalBloodKnowledge lesson");
	private static final List<String> SCHOOL_PAGES = List.of("vivacious", "fervent", "neurotic", "ferric", "incandescent",
			"ruinous", "frigid", "umbral");
	/** A memory named with its cast type, the way the school pages list them: "Blood Shot (QUICK)". */
	private static final Pattern TYPED_NAME = Pattern.compile(
			"(?<![A-Za-z])([A-Z][A-Za-z']*(?:(?: of| the|:)? [A-Z][A-Za-z']*)*) \\((?:all |both )?(?:QUICK|PASSIVE|CONTINUOUS|CHARGED)");
	private static final Pattern REGISTERED_MEMORY = Pattern.compile("MANIPS\\.register\\(\"([a-z0-9_]+)\"");
	/**
	 * An mL price on a memory line: inside the memory's type brackets, a rate such as "150 mL per entry", or a plain
	 * charge such as "costs 150 mL" or "150 mL to cast".
	 */
	private static final Pattern MEMORY_PRICE = Pattern.compile(
			"\\((?:all |both )?(?:QUICK|PASSIVE|CONTINUOUS|CHARGED)[^)]*\\bmL\\b|\\d\\s*mL (?:per|a|an|each) "
					+ "|\\bcost(?:s|ing)?\\s+(?:about\\s+)?\\d[\\d,]*\\s*mL\\b|\\d\\s*mL to (?:cast|hold|channel|use)\\b");
	/** A held form cut on 2026-11-12: no memory jets, sprays or plants a strand now (CQ-47, CQ-23). */
	private static final Pattern CUT_HELD_FORM = Pattern.compile(
			"held it (?:sprays|puts out a (?:thin )?(?:iron )?jet|puts out a jet)|plants a strand|\\bbrands it\\b");

	@Test
	void everyPageFileOfBothBooksIsRegistered() throws Exception {
		String definitions = SourceAssertions.readMainJava(DISCOVERY + "LiberEntryDefinitions.java");
		Set<String> registered = new TreeSet<>();
		collect(SANGUINIUM, definitions, "libersanguinium/", registered);
		collect(IMMACULATUS, definitions, "liberimmaculatus/", registered);
		collect(UPGRADE_LESSON, definitions, "libersanguinium/", registered);
		List<String> hidden = new ArrayList<>();
		int pages = 0;
		for (String book : List.of("libersanguinium", "liberimmaculatus")) {
			Path root = RepoPaths.DATA.resolve("books/" + book);
			try (Stream<Path> files = Files.walk(root)) {
				for (Path file : files.filter(path -> path.toString().endsWith(".json")).toList()) {
					String path = book + "/" + root.relativize(file).toString().replace('\\', '/').replaceAll("\\.json$", "");
					if (!path.contains("/pages/")) continue;
					pages++;
					if (registered.contains(path)) continue;
					if (LOOPED_FAMILIES.keySet().stream().anyMatch(path::startsWith)) continue;
					hidden.add(path);
				}
			}
		}
		assertTrue(pages > 140, "the page scan found too few pages to be reading both books: " + pages);
		assertEquals(List.of(), hidden, "pages no LiberEntryDefinitions entry registers, which MemoBookFilter hides from everyone");
	}

	@Test
	void everyOlderPageHasAFloorOrAnOpener() {
		String definitions = SourceAssertions.readMainJava(DISCOVERY + "LiberEntryDefinitions.java");
		String block = SourceAssertions.section(definitions, "Older pages (CQ-1", "/** The older Liber Sanguinium pages");
		String lists = SourceAssertions.section(definitions, "OLDER_PAGES =", "/** The Manipulations chapter's page");
		String openers = SourceAssertions.readMainJava(DISCOVERY + "LiberKnowledgeHelper.java")
				+ SourceAssertions.readMainJava(DISCOVERY + "LiberDiscoveryEvents.java");
		List<String> problems = new ArrayList<>();
		Matcher constant = CONSTANT.matcher(block);
		int count = 0;
		while (constant.find()) {
			count++;
			String name = constant.group(1);
			if (!Pattern.compile("\\b" + name + "\\b").matcher(lists).find()) problems.add(name + " is in neither older-page list, so never registered");
			if (!openers.contains("LiberEntryDefinitions." + name)) problems.add(name + " has no Degree floor, stage, item or advancement that opens it");
		}
		assertEquals(40, count, "the older pages changed; update this guardrail and the P9 build log");
		assertEquals(List.of(), problems);
	}

	@Test
	void theSchoolPagesNameEveryMemoryAndOnlyRealOnes() {
		JsonObject lang = ResourceReader.asset("lang/en_us.json");
		Set<String> names = new TreeSet<>();
		for (Map.Entry<String, JsonElement> entry : lang.entrySet()) {
			if (entry.getKey().matches("hemomancy\\.manipulation\\.[a-z0-9_]+")) names.add(entry.getValue().getAsString());
		}
		StringBuilder all = new StringBuilder();
		List<String> problems = new ArrayList<>();
		for (String school : SCHOOL_PAGES) {
			String text = ResourceReader.data("books/libersanguinium/manipulations/pages/" + school + ".json").get("text").getAsString();
			all.append(text).append('\n');
			Matcher typed = TYPED_NAME.matcher(text);
			int listed = 0;
			while (typed.find()) {
				listed++;
				if (!names.contains(typed.group(1))) problems.add(school + " names \"" + typed.group(1) + "\", which is no memory");
			}
			if (listed < 4) problems.add(school + " lists only " + listed + " memories with their type");
		}
		Matcher registered = REGISTERED_MEMORY.matcher(SourceAssertions.readMainJava("common/init/ManipulationInit.java"));
		while (registered.find()) {
			JsonElement name = lang.get("hemomancy.manipulation." + registered.group(1));
			if (name == null) problems.add(registered.group(1) + " has no name");
			else if (!all.toString().contains(name.getAsString())) problems.add(name.getAsString() + " is on no school page");
		}
		assertEquals(List.of(), problems);
	}

	/** CQ-47: the school pages carry no mL price; the live price is on the HUD. */
	@Test
	void theSchoolPagesCarryNoMemoryPrice() {
		List<String> priced = new ArrayList<>();
		for (String school : SCHOOL_PAGES) {
			String text = ResourceReader.data("books/libersanguinium/manipulations/pages/" + school + ".json").get("text").getAsString();
			Matcher price = MEMORY_PRICE.matcher(text);
			while (price.find()) priced.add(school + ": \"" + price.group() + "\"");
		}
		assertEquals(List.of(), priced);
	}

	/** CQ-47 with CQ-23 A: the school pages teach the held forms that are built, not the jets, sprays and plants. */
	@Test
	void theSchoolPagesTeachNoCutHeldForm() {
		List<String> stale = new ArrayList<>();
		for (String school : SCHOOL_PAGES) {
			String text = ResourceReader.data("books/libersanguinium/manipulations/pages/" + school + ".json").get("text").getAsString();
			Matcher cut = CUT_HELD_FORM.matcher(text);
			while (cut.find()) stale.add(school + ": \"" + cut.group() + "\"");
		}
		assertEquals(List.of(), stale);
	}

	private static void collect(Pattern pattern, String source, String prefix, Set<String> into) {
		Matcher matcher = pattern.matcher(source);
		while (matcher.find()) into.add(prefix + matcher.group(1));
	}
}
