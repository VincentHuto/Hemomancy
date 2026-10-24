package com.vincenthuto.hemomancy.architecture;

import com.vincenthuto.hemomancy.support.RepoPaths;
import com.vincenthuto.hemomancy.support.SourceAssertions;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * S-2 / S-3: owner attachments reach the client through the {@code PlayerStateSync} table, which only
 * {@code HemoCapabilityRegistrar} drives on login, respawn and dimension change. No other lifecycle handler resends a
 * table entry, and nothing polls a whole attachment per tick.
 */
class PlayerStateSyncTest {
	private static final String TABLE = "common/capability/PlayerStateSync.java";
	private static final String REGISTRAR = "common/capability/HemoCapabilityRegistrar.java";
	private static final Pattern LIFECYCLE_HANDLER = Pattern.compile(
			"static\\s+void\\s+(\\w+)\\s*\\([^)]*\\b(?:PlayerLoggedInEvent|PlayerRespawnEvent|PlayerChangedDimensionEvent"
					+ "|EntityJoinLevelEvent)\\b[^)]*\\)\\s*\\{");
	private static final Pattern TABLE_SENDER = Pattern.compile(
			"\\b(?:syncTendency|syncVascular|syncVolume|syncSkills|syncDegree|syncPlayerEvent|syncOwnAvatar"
					+ "|syncLastDeathMemory|syncSlots|syncLayerVisibilityToClient)\\s*\\(");
	private static final Pattern OWN_SYNC = Pattern.compile("(?<![\\w.])(?:sync|syncProgress|syncToClient)\\s*\\(");
	private static final Pattern TABLE_PAYLOAD = Pattern.compile("\\b(\\w+)\\.class\\b");
	private static final Pattern TABLE_IMPORT = Pattern.compile(
			"^import com\\.vincenthuto\\.hemomancy\\.(common\\.(?!network)[\\w.]+)\\.(\\w+);", Pattern.MULTILINE);

	@Test
	void onlyTheRegistrarResendsTableEntriesOnLifecycleEvents() throws Exception {
		String table = SourceAssertions.read(RepoPaths.MAIN_JAVA.resolve(TABLE));
		Set<String> payloads = new LinkedHashSet<>();
		Matcher payload = TABLE_PAYLOAD.matcher(table);
		while (payload.find()) payloads.add(payload.group(1));
		Set<String> owners = new LinkedHashSet<>();
		Matcher owner = TABLE_IMPORT.matcher(table);
		while (owner.find()) owners.add(owner.group(2) + ".java");
		assertTrue(payloads.size() >= 20, "the table names its payloads");

		List<String> offending = new ArrayList<>();
		for (Path file : javaFiles(RepoPaths.MAIN_JAVA.resolve("common"))) {
			String shown = RepoPaths.MAIN_JAVA.relativize(file).toString().replace('\\', '/');
			if (shown.equals(REGISTRAR) || shown.equals(TABLE)) continue;
			String text = SourceAssertions.read(file);
			Matcher handler = LIFECYCLE_HANDLER.matcher(text);
			while (handler.find()) {
				String body = SourceAssertions.methodBody(text, handler.group());
				boolean ownsAnEntry = owners.contains(file.getFileName().toString());
				boolean sends = TABLE_SENDER.matcher(body).find() || (ownsAnEntry && OWN_SYNC.matcher(body).find())
						|| payloads.stream().anyMatch(name -> body.contains("new " + name + "("));
				if (sends) offending.add(shown + "#" + handler.group(1));
			}
		}
		assertEquals(List.of(), offending, "Let HemoCapabilityRegistrar send the PlayerStateSync table instead");
	}

	@Test
	void noWholeAttachmentIsPolledPerTick() throws Exception {
		String known = SourceAssertions.readMainJava("common/capability/player/harbinger/manip/KnownManipulationEvents.java");
		assertFalse(known.contains("LAST_OWNER_SNAPSHOT"), "the per-tick serialise-and-diff is gone");
		String stations = SourceAssertions.readMainJava("common/event/StationUpgradeEvents.java");
		assertFalse(SourceAssertions.methodBody(stations, "public static void onTick(").contains(".sync("),
				"the station tick only backfills lessons; changes are sent through the dirty flag");
	}

	private static List<Path> javaFiles(Path root) throws Exception {
		try (Stream<Path> files = Files.walk(root)) {
			return files.filter(path -> path.toString().endsWith(".java")).sorted().toList();
		}
	}
}
