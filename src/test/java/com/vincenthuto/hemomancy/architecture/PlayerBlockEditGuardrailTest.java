package com.vincenthuto.hemomancy.architecture;

import com.vincenthuto.hemomancy.support.RepoPaths;
import com.vincenthuto.hemomancy.support.SourceAssertions;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * A block a player causes to be placed, broken or converted goes through {@code common/util/PlayerBlockEdits} (which
 * asks {@code mayBuild}, {@code mayInteract}, spawn protection and the NeoForge break or place event, so claim mods
 * can refuse it). Every raw edit site under the scan roots is counted per file, and each file with one is listed in
 * {@code architecture/player-block-edit-allow-list.txt} with its exact count and the reason those edits are not a
 * player's (cleanup audit RT-19 / A-6; widened to events, items, blocks and block entities by core-loop Track 0, and to
 * rites, entities and the world, counted per site, by core-loop round two P3). A guarded call elsewhere in a file no
 * longer excuses its raw edits. Stale rows and wrong counts fail.
 */
class PlayerBlockEditGuardrailTest {
	private static final String ALLOW_LIST = "src/test/resources/architecture/player-block-edit-allow-list.txt";
	private static final List<String> SCAN_ROOTS = List.of("common/manipulation", "common/network", "common/event",
			"common/item", "common/block", "common/tile", "common/rite", "common/entity", "common/world");
	private static final Pattern BLOCK_EDIT = Pattern.compile(
			"\\.(setBlock|setBlockAndUpdate|destroyBlock|removeBlock)\\s*\\(");

	@Test
	void everyRawBlockEditIsCountedWithItsReason() throws Exception {
		Map<String, Integer> allowed = new TreeMap<>();
		for (String line : SourceAssertions.read(ALLOW_LIST).split("\n")) {
			line = line.strip();
			if (line.isEmpty() || line.startsWith("#")) continue;
			String[] fields = line.split("\t");
			assertEquals(3, fields.length, "count, file and reason, tab-separated: " + line);
			assertFalse(fields[2].isBlank(), "every listed file needs a reason: " + line);
			allowed.put(fields[1], Integer.parseInt(fields[0]));
		}
		Map<String, Integer> found = new TreeMap<>();
		for (String root : SCAN_ROOTS) {
			try (Stream<Path> files = Files.walk(RepoPaths.MAIN_JAVA.resolve(root))) {
				for (Path file : files.filter(path -> path.toString().endsWith(".java")).toList()) {
					Matcher matcher = BLOCK_EDIT.matcher(SourceAssertions.read(file));
					int count = 0;
					while (matcher.find()) count++;
					if (count > 0) found.put(RepoPaths.display(file), count);
				}
			}
		}
		List<String> drift = new ArrayList<>();
		for (var entry : found.entrySet()) {
			Integer listed = allowed.get(entry.getKey());
			if (listed == null) drift.add(entry.getKey() + ": " + entry.getValue() + " raw block edits in a file not on the list");
			else if (!listed.equals(entry.getValue())) drift.add(entry.getKey() + ": listed " + listed + ", found " + entry.getValue());
		}
		for (String listed : allowed.keySet()) {
			if (!found.containsKey(listed)) drift.add(listed + ": listed but has no raw block edit now; remove the row");
		}
		assertEquals(List.of(), drift, "route player block edits through PlayerBlockEdits, or list the file in " + ALLOW_LIST
				+ " with its exact count and the reason its raw edits are not a player's");
	}
}
