package com.vincenthuto.hemomancy.common.command;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

final class PuppeteerSummonCommandSourceTest {
	@Test
	void commandCanListAddRemoveAndClearIndividualOrAllShapes() throws Exception {
		String command = Files.readString(Path.of(
				"src/main/java/com/vincenthuto/hemomancy/common/command/HemoCommand.java"));

		assertContains(command, "Commands.literal(\"summons\")");
		assertContains(command, "Commands.literal(\"list\")");
		assertContains(command, "Commands.literal(\"add\")");
		assertContains(command, "Commands.literal(\"remove\")");
		assertContains(command, "Commands.literal(\"clear\")");
		assertContains(command, "suggestPuppeteerSummons");
		assertContains(command, "PuppeteerSummonDefinitions.byName");
		assertContains(command, "KnownSummonEvents.sync(player, known)");
		assertContains(command, "\"all\".equals(summonName)");
	}

	private static void assertContains(String source, String expected) {
		assertTrue(source.contains(expected), () -> "Expected command source to contain: " + expected);
	}
}
