package com.vincenthuto.hemomancy.architecture;

import com.vincenthuto.hemomancy.support.RepoPaths;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * W-3: boss and arena managers live in {@code common/encounter}, not {@code common/worldgen}. The move kept every
 * saved key: they are strings in the classes, not class names.
 */
class EncounterPackageGuardrailTest {
	private static final List<String> ENCOUNTER_CLASSES = List.of("ChamberOfWillManager", "ChamberVisitService",
			"ChamberAccess", "FungalGardenTravelHelper", "MycophantEncounterManager", "VesperOrdealManager",
			"VesperOrdealRecoveryRules", "VagrantMindEncounterManager", "VagrantMindEncounterData");

	@Test
	void encounterManagersLiveInTheEncounterPackage() {
		for (String name : ENCOUNTER_CLASSES) {
			assertTrue(Files.isRegularFile(RepoPaths.mainJava("common/encounter/" + name + ".java")), name);
		}
	}

	@Test
	void worldgenHoldsNoEncounterManager() throws Exception {
		try (Stream<Path> files = Files.list(RepoPaths.mainJava("common/worldgen"))) {
			List<String> managers = files.map(path -> path.getFileName().toString())
					.filter(name -> name.endsWith("Manager.java") || name.endsWith("Service.java"))
					.sorted()
					.toList();
			// The Cortical Drift arcs are terrain state, not an encounter.
			assertEquals(List.of("CorticalArcManager.java"), managers);
		}
	}
}
