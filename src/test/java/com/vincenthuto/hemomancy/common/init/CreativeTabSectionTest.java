package com.vincenthuto.hemomancy.common.init;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import com.vincenthuto.hemomancy.support.RepoPaths;
import org.junit.jupiter.api.Test;

class CreativeTabSectionTest {
	private static final Set<String> HEMATIC_IDS = Arrays.stream(CreativeTabGroup.values())
			.flatMap(group -> group.paths().stream()).collect(Collectors.toSet());

	/** The main tab's ids, in order, from the frozen snapshot the GameTest checks against the live tab. */
	private static List<String> mainTab() throws Exception {
		List<String> lines = Files.readAllLines(RepoPaths.resolve("src/gameTest/resources/frozen/creative-tab-order.txt"));
		List<String> ids = new ArrayList<>();
		for (String line : lines.subList(lines.indexOf("# hemomancytab") + 1, lines.indexOf("# hemomancywip"))) {
			if (!line.isBlank()) {
				ids.add(line.split(" ")[0].replace("hemomancy:", ""));
			}
		}
		return ids;
	}

	@Test
	void everyFiledIdIsInTheMainTab() throws Exception {
		Set<String> tab = Set.copyOf(mainTab());
		TreeSet<String> missing = new TreeSet<>();
		for (CreativeTabSection section : CreativeTabSection.values()) {
			section.paths().stream().filter(path -> !tab.contains(path)).forEach(missing::add);
		}
		assertEquals(Set.of(), missing, "filed under a section but not in the main tab (renamed or moved to WIP?)");
	}

	@Test
	void theMainTabRunsSectionBySectionWithEggsLast() throws Exception {
		List<String> ids = mainTab();
		int lastSection = 0;
		boolean eggs = false;
		for (String id : ids) {
			int section = sectionOf(id).ordinal();
			assertTrue(section >= lastSection, id + " is out of section order");
			if (section > lastSection) {
				eggs = false;
			}
			boolean egg = id.startsWith("spawn_egg_");
			assertTrue(egg || !eggs, id + " follows its section's spawn eggs");
			eggs |= egg;
			lastSection = section;
		}
		// Miscellany may be empty.
		assertEquals(CreativeTabSection.values().length - 1,
				ids.stream().map(CreativeTabSectionTest::sectionOf).collect(Collectors.toSet()).size(),
				"every section has at least one entry");
	}

	private static CreativeTabSection sectionOf(String id) {
		return Arrays.stream(CreativeTabSection.values()).filter(section -> section.paths().contains(id)).findFirst()
				.orElse(HEMATIC_IDS.contains(id) || id.startsWith("memory_") ? CreativeTabSection.HEMATIC
						: CreativeTabSection.MISC);
	}
}
