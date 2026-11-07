package com.vincenthuto.hemomancy.architecture;

import com.vincenthuto.hemomancy.support.RepoPaths;
import com.vincenthuto.hemomancy.support.SourceAssertions;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * A block a player's cast or payload places or breaks goes through {@code common/util/PlayerBlockEdits} (which asks
 * {@code mayBuild}, {@code mayInteract}, spawn protection and the NeoForge break or place event, so claim mods can
 * refuse it). A file under {@code common/manipulation} or {@code common/network} that edits blocks either names that
 * helper or is listed here with the reason its edit is not a player's (cleanup audit RT-19 / A-6). Stale entries fail.
 */
class PlayerBlockEditGuardrailTest {
	private static final Pattern BLOCK_EDIT = Pattern.compile(
			"\\.(setBlock|setBlockAndUpdate|destroyBlock|removeBlock)\\s*\\(");
	private static final Pattern GUARDED = Pattern.compile("PlayerBlockEdits\\.|hasBreakPermission\\(|mayAlter\\(");

	private static final Map<String, String> ALLOWED = Map.of(
			"src/main/java/com/vincenthuto/hemomancy/common/manipulation/EntityCasts.java",
			"Wills and Vespers casting; no player is the actor",
			"src/main/java/com/vincenthuto/hemomancy/common/manipulation/congeatio/TemporaryIceManager.java",
			"Removes the ice it placed itself; placement is guarded by its callers",
			"src/main/java/com/vincenthuto/hemomancy/common/network/PlaceStructurePacket.java",
			"Creative, operator-only structure spawner");

	/** Non-player editors outside the walked trees, each still editing blocks without the helper. */
	private static final Map<String, String> NON_PLAYER_EDITORS = Map.of(
			"common/entity/boss/saint/SaintArena.java",
			"A Saint's fight changing its own chamber, every change recorded and restored; no player is the actor");

	@Test
	void playerBlockEditsAreGuarded() throws Exception {
		TreeSet<String> unguarded = new TreeSet<>();
		try (Stream<Path> files = Stream.concat(
				Files.walk(RepoPaths.MAIN_JAVA.resolve("common/manipulation")),
				Files.walk(RepoPaths.MAIN_JAVA.resolve("common/network")))) {
			for (Path file : files.filter(path -> path.toString().endsWith(".java")).toList()) {
				String text = SourceAssertions.read(file);
				if (BLOCK_EDIT.matcher(text).find() && !GUARDED.matcher(text).find()) {
					unguarded.add(RepoPaths.display(file));
				}
			}
		}
		assertEquals(new TreeSet<>(ALLOWED.keySet()), unguarded,
				"route player block edits through PlayerBlockEdits, or list the file here with the reason it is not a player's");
	}

	@Test
	void listedNonPlayerEditorsStillEditBlocksThemselves() {
		for (String file : NON_PLAYER_EDITORS.keySet()) {
			String text = SourceAssertions.readMainJava(file);
			assertEquals(true, BLOCK_EDIT.matcher(text).find() && !GUARDED.matcher(text).find(),
					file + " is listed as a non-player editor: it edits blocks and names no player guard");
		}
	}
}
