package com.vincenthuto.hemomancy.architecture;

import com.vincenthuto.hemomancy.support.RepoPaths;
import com.vincenthuto.hemomancy.support.SourceAssertions;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * P-10: entity ids are reused in the next world, so a static client collection keyed by entity id is cleared when
 * the player logs out ({@code ClientSessionEvents.onClientPlayerLogout}), or listed here with its reason.
 */
class ClientSessionCacheTest {
	private static final Pattern ENTITY_ID_KEYED = Pattern.compile(
			"static\\s+(?:final\\s+)?(?:Map<Integer|HashMap<Integer|LinkedHashMap<Integer|Set<Integer|HashSet<Integer"
					+ "|Int2\\w+Map|IntSet|IntOpenHashSet)");

	private static final Map<String, String> EXEMPT = Map.of(
			"ArborGrowthAnimations", "keyed by skill id, not entity id; the same skill grows the same way in any world");

	@Test
	void everyEntityIdCacheIsClearedOnLogout() throws Exception {
		String logout = SourceAssertions.methodBody(SourceAssertions.readMainJava("client/event/ClientSessionEvents.java"),
				"void onClientPlayerLogout(");
		TreeSet<String> uncleared = new TreeSet<>();
		TreeSet<String> stale = new TreeSet<>(EXEMPT.keySet());
		for (Path file : clientFiles()) {
			String name = file.getFileName().toString().replace(".java", "");
			if (!ENTITY_ID_KEYED.matcher(SourceAssertions.read(file)).find()) {
				continue;
			}
			stale.remove(name);
			if (EXEMPT.containsKey(name)) {
				continue;
			}
			if (!Pattern.compile("\\b" + name + "\\.(clear|clearSession)\\(\\)").matcher(logout).find()) {
				uncleared.add(name);
			}
		}
		assertEquals(List.of(), new ArrayList<>(uncleared), "entity-id caches not cleared on logout");
		assertEquals(List.of(), new ArrayList<>(stale), "stale exemptions");
	}

	private static List<Path> clientFiles() throws Exception {
		try (Stream<Path> files = Files.walk(RepoPaths.MAIN_JAVA.resolve("client"))) {
			return files.filter(path -> path.toString().endsWith(".java")).sorted().toList();
		}
	}
}
