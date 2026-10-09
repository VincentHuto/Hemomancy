package com.vincenthuto.hemomancy.common.entity.summon;

import com.vincenthuto.hemomancy.common.summon.PuppeteerSummonDefinition;
import com.vincenthuto.hemomancy.common.summon.PuppeteerSummonDefinitions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PuppeteerSummonStructureTest {
	private static final Path SOURCE_ROOT = Path.of("src/main/java/com/vincenthuto/hemomancy");
	private static final List<String> BODIES = List.of("CinderBellows", "GoreboundHulk", "MarrowSpitter",
			"MnemonistPuppet", "RingmasterPattern", "SanguineHound", "ScarletMummer", "VeinwingVulture");

	@Test
	void everyDefinitionHasExactlyOneBodyMapping() throws IOException {
		String bodies = read("common/summon/PuppeteerSummonBodies.java");
		for (PuppeteerSummonDefinition definition : PuppeteerSummonDefinitions.all()) {
			String constant = definition.name().toUpperCase(Locale.ROOT);
			assertTrue(bodies.contains("PuppeteerSummonDefinitions." + constant + ", EntityInit." + definition.name()),
					definition.name() + " must map to its own entity type");
		}
		for (String caller : List.of("common/summon/PuppeteerSummonFactory.java",
				"client/screen/skilltree/harbinger/SummonsTabView.java", "common/circus/CircusDemonstrations.java")) {
			assertTrue(read(caller).contains("PuppeteerSummonBodies.create("), caller + " must use the shared body table");
			assertFalse(read(caller).contains("EntityInit.mnemonist_puppet"), caller + " must not pick bodies itself");
		}
	}

	@Test
	void ringmasterPatternNeverReplaysMnemonistMemories() throws IOException {
		String pattern = read("common/entity/summon/RingmasterPatternEntity.java");
		assertTrue(pattern.contains("extends GroundPuppetEntity"), "The Conductor is not a Mnemonist subclass");
		assertFalse(pattern.contains("MemoryReplay") || pattern.contains("rememberDamage"),
				"The Conductor must not carry the Mnemonist's D5 replay");

		String mnemonist = read("common/entity/summon/MnemonistPuppetEntity.java");
		assertTrue(mnemonist.contains("if (damaged == null || !replaysMemories()"),
				"A legacy Ringmaster save must not record memories before it converts");
		assertTrue(mnemonist.contains("convertLegacyRingmaster((ServerLevel) level())"),
				"A legacy Ringmaster save must convert to its own entity type");

		String events = read("common/entity/summon/MnemonistPuppetEvents.java");
		assertTrue(events.contains("MnemonistPuppetEntity.loaded()"), "Damage capture must use the tracked puppets");
		assertFalse(events.contains("getEntitiesOfClass"), "Damage capture must not search the world on every hit");
	}

	@Test
	void bodiesShareSyncedBindingStateAndDefinitionStats() throws IOException {
		for (String body : BODIES) {
			String source = read("common/entity/summon/" + body + "Entity.java");
			assertTrue(source.contains("implements SyncedBoundSummon"), body + " must use the shared binding state");
			assertTrue(source.contains("BoundSummonSync.define(" + body + "Entity.class)"),
					body + " must define its binding accessors once");
			assertFalse(source.contains("DATA_OWNER_UUID"), body + " must not duplicate binding accessors");
			assertTrue(source.contains("BoundSummonBehavior.definitionAttributes("),
					body + " registered stats must come from its summon definition");
		}
	}

	private static String read(String path) throws IOException {
		return Files.readString(SOURCE_ROOT.resolve(path));
	}
}
