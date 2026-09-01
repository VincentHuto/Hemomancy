package com.vincenthuto.hemomancy.common.entity.npc.dialogue;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

class DialogueHubMigrationSourceTest {
	private static final Path ROOT = Path.of("").toAbsolutePath();

	@Test
	void everyRecurringNpcUsesTheSharedHubDecorator() throws IOException {
		Map<String, String> entities = Map.ofEntries(
				Map.entry("harbinger/HarbingerAlchemistEntity.java", "alchemist"),
				Map.entry("harbinger/HarbingerArtificerEntity.java", "artificer"),
				Map.entry("harbinger/HarbingerCicatrixAnchoriteEntity.java", "cicatrix_anchorite"),
				Map.entry("harbinger/HarbingerHermitEntity.java", "hermit"),
				Map.entry("harbinger/HarbingerMnemonistEntity.java", "mnemonist"),
				Map.entry("harbinger/HarbingerVicarEntity.java", "vicar"),
				Map.entry("harbinger/HarbingerVotaryWayfarerEntity.java", "votary_wayfarer"),
				Map.entry("harbinger/HarbingerVoyagerEntity.java", "voyager"),
				Map.entry("unstained/UnstainedAcolyteEntity.java", "acolyte"),
				Map.entry("unstained/UnstainedGuardianEntity.java", "guardian"),
				Map.entry("unstained/UnstainedZealotEntity.java", "zealot"));
		Path root = ROOT.resolve("src/main/java/com/vincenthuto/hemomancy/common/entity/npc");
		for (var entry : entities.entrySet()) {
			String source = Files.readString(root.resolve(entry.getKey()));
			assertTrue(source.contains("DialogueHubFactory.decorate(tree, \"" + entry.getValue() + "\""),
					entry.getKey() + " must use the topic hub");
		}
	}
}
