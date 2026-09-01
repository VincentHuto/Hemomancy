package com.vincenthuto.hemomancy.common.command;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

final class ManipulationKnowledgeCommandSourceTest {
	@Test
	void commandCanListAddRemoveAndClearIndividualOrAllManipulations() throws Exception {
		String command = Files.readString(Path.of(
				"src/main/java/com/vincenthuto/hemomancy/common/command/HemoCommand.java"));

		assertContains(command, "Commands.literal(\"manipulations\")");
		assertContains(command, "Commands.literal(\"list\")");
		assertContains(command, "Commands.literal(\"add\")");
		assertContains(command, "Commands.literal(\"remove\")");
		assertContains(command, "Commands.literal(\"clear\")");
		assertContains(command, "Commands.literal(\"level\")");
		assertContains(command, "Commands.literal(\"xp\")");
		assertContains(command, "setSelectedManipulationLevel");
		assertContains(command, "setSelectedManipulationXp");
		assertContains(command, "IntegerArgumentType.integer(0, ManipLevel.MAX_LEVEL)");
		assertContains(command, "DoubleArgumentType.doubleArg(0.0D)");
		assertContains(command, "selectedEquippedManipulationLevel");
		assertContains(command, "suggestManipulations");
		assertContains(command, "ManipulationInit.getAllEntries()");
		assertContains(command, "ManipulationInit.getByName(manipulationName)");
		assertContains(command, "new ManipLevel(0, 0)");
		assertContains(command, "KnownManipulationEvents.syncPlayerEvent(player)");
		assertContains(command, "known.setSelectedManip(BloodManipulation.BLANK)");
		assertContains(command, "\"all\".equals(manipulationName)");
	}

	private static void assertContains(String source, String expected) {
		assertTrue(source.contains(expected), () -> "Expected command source to contain: " + expected);
	}
}
