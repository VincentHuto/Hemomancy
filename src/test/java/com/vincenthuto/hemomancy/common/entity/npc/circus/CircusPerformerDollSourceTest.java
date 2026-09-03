package com.vincenthuto.hemomancy.common.entity.npc.circus;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class CircusPerformerDollSourceTest {
	@Test
	void performersSummonOwnedDollsAndDollsOnlyFightTheirActiveThreat() throws IOException {
		String performer = source("common/entity/npc/circus/CircusPerformerEntity.java");
		String doll = source("common/entity/mob/monster/EnthralledDollEntity.java");
		assertTrue(performer.contains("new EnthralledDollEntity(level(), this)"));
		assertTrue(performer.contains("CircusPerformerRules.dollCount"));
		assertTrue(doll.contains("owner instanceof CircusPerformerEntity"));
		assertTrue(doll.contains("performer.getActState() != ActState.ALERT"));
		assertTrue(doll.contains("performer.getTarget() != target"));
	}

	private static String source(String relativePath) throws IOException {
		return Files.readString(Path.of("src/main/java/com/vincenthuto/hemomancy/" + relativePath));
	}
}
