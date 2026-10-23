package com.vincenthuto.hemomancy.architecture;

import com.vincenthuto.hemomancy.support.RepoPaths;
import com.vincenthuto.hemomancy.support.SourceAssertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Step 11 (roadmap Phase 10, O-4): dialogue option events reach gameplay through one ordered routing table,
 * {@code DialogueEventRouter}, which tries one {@code <Npc>DialogueEvents} class per NPC. A new NPC's events get a
 * class in the table, not another {@code DialogueEvent} subscriber or a growing shared handler.
 */
class DialogueRoutingGuardrailTest {
	private static final Path ROUTES = RepoPaths.mainJava("common/entity/npc/dialogue/route");
	private static final Pattern SUBSCRIBER = Pattern.compile("@SubscribeEvent[^;{]*?\\(\\s*(?:final\\s+)?DialogueEvent\\s+\\w+\\s*\\)");
	/** The largest route class may not grow back towards the old 1,531-line handler. */
	private static final int MAX_ROUTE_LINES = 400;

	@Test
	void onlyTheRouterSubscribesToDialogueEvents() throws IOException {
		Map<String, Integer> found = new TreeMap<>();
		try (Stream<Path> files = Files.walk(RepoPaths.MAIN_JAVA)) {
			for (Path file : files.filter(path -> path.toString().endsWith(".java")).toList()) {
				var matcher = SUBSCRIBER.matcher(SourceAssertions.read(file));
				int count = 0;
				while (matcher.find()) count++;
				if (count > 0) found.put(RepoPaths.display(file), count);
			}
		}
		assertEquals(Map.of(RepoPaths.display(ROUTES.resolve("DialogueEventRouter.java")), 1), found,
				"route a new dialogue event through DialogueEventRouter's table");
	}

	@Test
	void everyNpcRouteIsInTheTable() throws IOException {
		String router = SourceAssertions.read(ROUTES.resolve("DialogueEventRouter.java"));
		Set<String> unrouted = new TreeSet<>();
		try (Stream<Path> files = Files.list(ROUTES)) {
			for (Path file : files.filter(path -> path.toString().endsWith("DialogueEvents.java")).toList()) {
				String name = file.getFileName().toString().replace(".java", "");
				SourceAssertions.assertContains(name + " handles its own events", SourceAssertions.read(file),
						"static boolean handle(DialogueEvent event)");
				if (!router.contains(name + "::handle")) unrouted.add(name);
			}
		}
		assertEquals(Set.of(), unrouted, "add each NPC's route class to DialogueEventRouter.ROUTES");
	}

	@Test
	void noRouteClassGrowsBackIntoAGodClass() throws IOException {
		try (Stream<Path> files = Files.list(ROUTES)) {
			for (Path file : files.filter(path -> path.toString().endsWith(".java")).toList()) {
				long lines = SourceAssertions.read(file).lines().count();
				assertTrue(lines <= MAX_ROUTE_LINES, RepoPaths.display(file) + " has " + lines
						+ " lines; split the NPC's events or move gameplay into its mission class");
			}
		}
	}
}
