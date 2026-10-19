package com.vincenthuto.hemomancy.common.entity.boss.endgame;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

final class VesperManipulationCoverageTest {
	private static final Path ROOT = Path.of("src/main/java/com/vincenthuto/hemomancy/common");

	private static List<String> registeredIds() throws Exception {
		String source = Files.readString(ROOT.resolve("init/ManipulationInit.java"));
		Matcher matcher = Pattern.compile("MANIPS\\.register\\(\"(\\w+)\"").matcher(source);
		List<String> ids = new ArrayList<>();
		while (matcher.find()) ids.add(matcher.group(1));
		return ids;
	}

	@Test
	void tableCoversEveryRegisteredManipulation() throws Exception {
		List<String> ids = registeredIds();
		assertTrue(ids.size() > 100);
		for (String id : ids) assertNotNull(VesperManipulationCoverage.entry(id), id + " is missing from the table");
		assertEquals(ids.size(), VesperManipulationCoverage.TABLE.size(), "no stale entries");
	}

	@Test
	void everyExcludedEntryHasAReason() {
		VesperManipulationCoverage.TABLE.forEach((id, entry) -> {
			if (entry.coverage() == VesperManipulationCoverage.Coverage.EXCLUDED) {
				assertFalse(entry.reason().isBlank(), id);
			} else {
				assertNotNull(entry.school(), id);
			}
			if (entry.coverage() == VesperManipulationCoverage.Coverage.BOSS_VARIANT) assertNotNull(entry.spec(), id);
		});
	}

	@Test
	void everySchoolHasSeveralFightManipulations() {
		for (EnumBloodTendency school : EnumBloodTendency.values()) {
			assertTrue(VesperManipulationCoverage.usable(school).size() >= 3, school + " has too few");
		}
	}

	@Test
	void chainsOnlyReferenceUsableEntriesFromDifferentSchools() {
		assertFalse(VesperSynergyChains.ALL.isEmpty());
		for (VesperSynergyChains.Chain chain : VesperSynergyChains.ALL) {
			VesperManipulationCoverage.Entry first = VesperManipulationCoverage.entry(chain.first());
			VesperManipulationCoverage.Entry second = VesperManipulationCoverage.entry(chain.second());
			assertNotNull(first, chain.first());
			assertNotNull(second, chain.second());
			assertNotEquals(VesperManipulationCoverage.Coverage.EXCLUDED, first.coverage(), chain.first());
			assertNotEquals(VesperManipulationCoverage.Coverage.EXCLUDED, second.coverage(), chain.second());
			assertNotEquals(first.school(), second.school(), chain + " should cross schools");
			assertTrue(chain.gapTicks() >= 4 && chain.gapTicks() <= 30);
		}
		assertTrue(VesperSynergyChains.ALL.stream().anyMatch(c -> c.first().equals("sanguine_magnetism")
				&& c.second().equals("synaptic_jolt")));
	}

	@Test
	void willSupportedListIsUnchanged() throws Exception {
		String source = Files.readString(ROOT.resolve("manipulation/EntityManipulationEffects.java"));
		int start = source.indexOf("SUPPORTED_CASTS = Set.of(");
		int end = source.indexOf(");", start);
		long count = Pattern.compile("\"\\w+\"").matcher(source.substring(start, end)).results().count();
		assertEquals(32, count, "Vesper's casts must not widen what Wills can cast");
	}

	@Test
	void castableMeansTheSharedEntityPathSupportsIt() throws Exception {
		String source = Files.readString(ROOT.resolve("manipulation/EntityManipulationEffects.java"));
		int start = source.indexOf("SUPPORTED_CASTS = Set.of(");
		String supported = source.substring(start, source.indexOf(");", start));
		VesperManipulationCoverage.TABLE.forEach((id, entry) -> {
			if (entry.coverage() == VesperManipulationCoverage.Coverage.CASTABLE) {
				assertTrue(supported.contains("\"" + id + "\""), id + " is CASTABLE but has no entity cast");
			}
		});
	}
}
