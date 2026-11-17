package com.vincenthuto.hemomancy.architecture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.vincenthuto.hemomancy.support.RepoPaths;
import com.vincenthuto.hemomancy.support.ResourceReader;
import com.vincenthuto.hemomancy.support.SourceAssertions;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

/**
 * Core-loop Track 11: what the overhaul changed is taught where the player meets it. A memory that sculpts a verb carries
 * the tooltip line its memory item reads and is named on a Liber Sanguinium page a player can open; each core-loop first
 * is an advancement that exists, has its words, and is granted from the code where the act happens; and every page the
 * core loop wrote is registered (an unregistered page is hidden by {@code MemoBookFilter}) with a way to open it.
 */
class CoreLoopTeachingGuardrailTest {
	/** Family forms taught on their family's page, by the family's name. */
	private static final String DEFINITIONS = "common/capability/player/shared/knowledge/discovery/LiberEntryDefinitions.java";
	private static final Map<String, String> FORM_OF = Map.of(
			"lingering_blood_binding", "blood_binding",
			"chain_blood_binding", "blood_binding",
			"blood_lattice", "blood_binding",
			"canopy_mortis", "lignum_mortis",
			"worked_lignum", "lignum_mortis");
	private static final Pattern REGISTRATION = Pattern.compile("MANIPS\\.register\\(\"([a-z0-9_]+)\"");
	private static final Pattern ATTACHED = Pattern.compile("(set|add)(Projection|Absorption)Sculpting\\(Sculptings::");
	private static final Pattern ENTRY = Pattern.compile("entry\\(\"([a-z_/]+)\"\\)");
	private static final Pattern CORE_PAGE = Pattern.compile("public static final ResourceLocation ([A-Z_]+) = entry\\(\"([a-z_/]+)\"\\);");
	private static final Pattern FIRST = Pattern.compile("public static final ResourceLocation (ADV_[A-Z_]+) = Hemomancy\\.rloc\\(\"hemomancy/([a-z_]+)\"\\);");

	@Test
	void everySculptingIsOnItsMemoryAndInTheLiber() throws Exception {
		String init = SourceAssertions.readMainJava("common/init/ManipulationInit.java");
		JsonObject lang = lang();
		String liber = liberText(registeredPages());
		List<String> missing = new ArrayList<>();
		Matcher registration = REGISTRATION.matcher(init);
		List<int[]> spans = new ArrayList<>();
		List<String> ids = new ArrayList<>();
		while (registration.find()) {
			spans.add(new int[] {registration.start()});
			ids.add(registration.group(1));
		}
		int sculptings = 0;
		for (int i = 0; i < ids.size(); i++) {
			int end = i + 1 < spans.size() ? spans.get(i + 1)[0] : init.length();
			String block = init.substring(spans.get(i)[0], end);
			Matcher attached = ATTACHED.matcher(block);
			while (attached.find()) {
				sculptings++;
				String id = ids.get(i);
				String verb = attached.group(2).equals("Projection") ? "project" : "absorb";
				if (!lang.has("hemomancy.manipulation." + id + ".sculpting." + verb)) {
					missing.add(id + ": no hemomancy.manipulation." + id + ".sculpting." + verb + " line for its memory tooltip");
				}
				String taughtAs = FORM_OF.getOrDefault(id, id);
				JsonElement name = lang.get("hemomancy.manipulation." + taughtAs);
				if (name == null || !liber.contains(name.getAsString())) {
					missing.add(id + ": not named on any Liber Sanguinium page (as " + taughtAs + ")");
				}
			}
		}
		assertTrue(sculptings >= 25, "the Sculpting scan found too few attachments to be reading ManipulationInit: " + sculptings);
		assertEquals(List.of(), missing);
	}

	@Test
	void everyCoreLoopFirstIsAnAdvancementGrantedWhereItHappens() throws Exception {
		String granter = SourceAssertions.readMainJava("common/event/HarbingerAdvancementGranter.java");
		String firsts = SourceAssertions.section(granter, "Core loop firsts", "/** Ordered array of degree advancements");
		JsonObject lang = lang();
		List<String> problems = new ArrayList<>();
		Matcher first = FIRST.matcher(firsts);
		int count = 0;
		while (first.find()) {
			count++;
			String constant = first.group(1);
			String id = first.group(2);
			Path file = RepoPaths.DATA.resolve("advancement/hemomancy/" + id + ".json");
			if (!Files.isRegularFile(file)) {
				problems.add(id + ": no advancement file");
			} else if (!Files.readString(file).contains("minecraft:impossible")) {
				problems.add(id + ": not a granted (impossible-trigger) advancement");
			}
			for (String part : List.of("title", "description")) {
				if (!lang.has("advancements.hemomancy." + id + "." + part)) problems.add(id + ": no " + part);
			}
			if (grantSites(constant) == 0) problems.add(constant + " is never granted outside the granter");
		}
		assertEquals(27, count, "the core-loop firsts changed; update this guardrail and the Track 11 ledger");
		assertEquals(List.of(), problems);
	}

	/** CN-6: the firsts that carry the goal frame. Every other first is a task. */
	@Test
	void onlyTheNamedFirstsCarryTheGoalFrame() {
		String granter = SourceAssertions.readMainJava("common/event/HarbingerAdvancementGranter.java");
		String firsts = SourceAssertions.section(granter, "Core loop firsts", "/** Ordered array of degree advancements");
		List<String> goals = new ArrayList<>();
		Matcher first = FIRST.matcher(firsts);
		while (first.find()) {
			String id = first.group(2);
			JsonObject display = ResourceReader.data("advancement/hemomancy/" + id + ".json").getAsJsonObject("display");
			if ("goal".equals(display.get("frame").getAsString())) goals.add(id);
		}
		assertEquals(List.of("moon_brought_on", "carried_crossing"), goals,
				"the goal-framed firsts changed; update CN-6 in the Track 11 ledger");
	}

	private static long grantSites(String constant) throws Exception {
		// Whole-word: ADV_CARRIED must not be counted where only ADV_CARRIED_CROSSING is granted.
		Pattern named = Pattern.compile("\\b" + constant + "\\b");
		try (Stream<Path> files = Files.walk(RepoPaths.MAIN_JAVA)) {
			return files.filter(path -> path.toString().endsWith(".java"))
					.filter(path -> !path.getFileName().toString().equals("HarbingerAdvancementGranter.java"))
					.filter(path -> SourceAssertions.read(path).contains("grantIfNotDone(")
							&& named.matcher(SourceAssertions.read(path)).find())
					.count();
		}
	}

	private static JsonObject lang() throws Exception {
		return JsonParser.parseString(Files.readString(RepoPaths.ASSETS.resolve("lang/en_us.json"))).getAsJsonObject();
	}

	/** Liber Sanguinium page paths a player can ever see: those registered by literal in {@code LiberEntryDefinitions}. */
	private static List<String> registeredPages() {
		String definitions = SourceAssertions.readMainJava(DEFINITIONS);
		List<String> pages = new ArrayList<>();
		Matcher entry = ENTRY.matcher(definitions);
		while (entry.find()) pages.add(entry.group(1));
		return pages;
	}

	@Test
	void everyCoreLoopPageIsRegisteredWithAWayToOpenIt() throws Exception {
		String definitions = SourceAssertions.readMainJava(DEFINITIONS);
		String block = SourceAssertions.section(definitions, "Core loop pages", "CORE_LOOP_PAGES =");
		String list = SourceAssertions.section(definitions, "CORE_LOOP_PAGES =", ";");
		String unlocks = SourceAssertions.readMainJava("common/capability/player/shared/knowledge/discovery/LiberKnowledgeHelper.java")
				+ SourceAssertions.readMainJava("common/capability/player/shared/knowledge/discovery/LiberDiscoveryEvents.java");
		List<String> problems = new ArrayList<>();
		Matcher page = CORE_PAGE.matcher(block);
		int count = 0;
		while (page.find()) {
			count++;
			String constant = page.group(1);
			String path = page.group(2);
			if (!Files.isRegularFile(RepoPaths.DATA.resolve("books/libersanguinium/" + path + ".json"))) {
				problems.add(path + ": no page file");
			}
			if (!list.contains(constant)) problems.add(constant + " is not in CORE_LOOP_PAGES, so it is never registered");
			if (!unlocks.contains("LiberEntryDefinitions." + constant)) problems.add(constant + " has no Degree, item or first that opens it");
		}
		assertEquals(29, count, "the core loop's Liber pages changed; update this guardrail and the Track 11 ledger");
		assertEquals(List.of(), problems);
	}

	private static String liberText(List<String> visible) throws Exception {
		StringBuilder text = new StringBuilder();
		Path root = RepoPaths.DATA.resolve("books/libersanguinium");
		try (Stream<Path> files = Files.walk(root)) {
			for (Path file : files.filter(path -> path.toString().endsWith(".json")).toList()) {
				String path = root.relativize(file).toString().replace('\\', '/').replaceAll("\\.json$", "");
				if (!visible.contains(path)) continue;
				JsonElement page = JsonParser.parseString(Files.readString(file));
				if (page.isJsonObject() && page.getAsJsonObject().has("text")) {
					text.append(page.getAsJsonObject().get("text").getAsString()).append('\n');
				}
			}
		}
		return text.toString();
	}
}
