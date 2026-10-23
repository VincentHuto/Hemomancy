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

	// That Wills may cast exactly the 32 entity casts they could before, and that every CASTABLE entry has an entity
	// cast, is checked over the live registry by entity_cast_validation's
	// entityCastsAreAttachedWhereWillsAndVesperNeedThem (entity casts are attached at registration since step 11).
}
