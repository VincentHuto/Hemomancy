package com.vincenthuto.hemomancy.architecture;

import com.vincenthuto.hemomancy.support.RepoPaths;
import com.vincenthuto.hemomancy.support.SourceAssertions;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * S-9: a one-shot migration of old player data runs only from the {@code PlayerDataMigrations} table, gated by the
 * player's data version, never from its own login, respawn or tick handler.
 */
class PlayerDataMigrationGuardrailTest {
	private static final String TABLE =
			"src/main/java/com/vincenthuto/hemomancy/common/capability/player/shared/migration/PlayerDataMigrations.java";

	/** Each migration entry point and the file that defines it. */
	private static final Map<String, String> ENTRY_POINTS = Map.of(
			"PlayerCooldowns::migrateLegacyRootKeys",
			"src/main/java/com/vincenthuto/hemomancy/common/capability/player/shared/cooldown/PlayerCooldowns.java",
			"ChamberVisitService::migrateLegacyAccess",
			"src/main/java/com/vincenthuto/hemomancy/common/encounter/ChamberVisitService.java",
			"PendingWhispers::migrateLegacyInbox",
			"src/main/java/com/vincenthuto/hemomancy/common/entity/npc/dialogue/PendingWhispers.java",
			"CircusPlayerProgress::migrateLegacyRootKeys",
			"src/main/java/com/vincenthuto/hemomancy/common/capability/player/harbinger/circus/CircusPlayerProgress.java",
			"FungalScarMigrationRules::migrateCarried",
			"src/main/java/com/vincenthuto/hemomancy/common/capability/player/harbinger/scar/fungal/FungalScarMigrationRules.java",
			"StationUpgradeMigration::migrate",
			"src/main/java/com/vincenthuto/hemomancy/common/capability/player/harbinger/station/StationUpgradeMigration.java",
			"LiberKnowledgeEvents::migrateLegacyNotes",
			"src/main/java/com/vincenthuto/hemomancy/common/capability/player/shared/knowledge/LiberKnowledgeEvents.java",
			"InitiatoryDegreeEvents::migrateLegacyArchonChoice",
			"src/main/java/com/vincenthuto/hemomancy/common/capability/player/harbinger/degree/InitiatoryDegreeEvents.java");

	@Test
	void migrationsRunOnlyFromTheTable() throws Exception {
		String table = SourceAssertions.read(TABLE);
		List<String> offending = new ArrayList<>();
		for (String reference : ENTRY_POINTS.keySet()) {
			SourceAssertions.assertContains("migration table", table, reference);
		}
		try (Stream<Path> files = Files.walk(RepoPaths.ROOT.resolve("src/main/java"))) {
			for (Path file : files.filter(path -> path.toString().endsWith(".java")).toList()) {
				String shown = RepoPaths.display(file);
				if (shown.equals(TABLE)) continue;
				String text = SourceAssertions.read(file);
				for (var entry : ENTRY_POINTS.entrySet()) {
					String method = entry.getKey().substring(entry.getKey().indexOf("::") + 2) + "(";
					// The defining file declares the method once and never calls it; nothing else calls it at all.
					boolean runsIt = shown.equals(entry.getValue())
							? text.indexOf(method) != text.lastIndexOf(method)
							: text.contains(entry.getKey().replace("::", ".") + "(") || text.contains(entry.getKey());
					if (runsIt) offending.add(shown + " runs " + entry.getKey());
				}
			}
		}
		assertEquals(List.of(), offending, "Run one-shot migrations through PlayerDataMigrations");
	}

	/** F-21: the degree's Archon path is the only authority; the old root key is only read once and cleared. */
	@Test
	void theArchonChoiceRootKeyIsNeverWritten() throws Exception {
		List<String> users = new ArrayList<>();
		try (Stream<Path> files = Files.walk(RepoPaths.ROOT.resolve("src/main/java"))) {
			for (Path file : files.filter(path -> path.toString().endsWith(".java")).toList()) {
				if (SourceAssertions.read(file).contains("ARCHON_CHOICE_KEY")) users.add(RepoPaths.display(file));
			}
		}
		users.sort(null);
		assertEquals(List.of(
				"src/main/java/com/vincenthuto/hemomancy/common/capability/player/harbinger/degree/InitiatoryDegreeEvents.java",
				"src/main/java/com/vincenthuto/hemomancy/common/command/QliphothSubcommand.java",
				"src/main/java/com/vincenthuto/hemomancy/common/encounter/FungalGardenTravelHelper.java",
				"src/main/java/com/vincenthuto/hemomancy/common/recipe/ArmatureUpgradeRecipe.java"), users);
		// The literal key: only the constant in code, and in data only Armature gates, which resolve it by path.
		List<String> literal = new ArrayList<>();
		for (String root : List.of("src/main/java", "src/main/resources")) {
			try (Stream<Path> files = Files.walk(RepoPaths.ROOT.resolve(root))) {
				for (Path file : files.filter(Files::isRegularFile)
						.filter(path -> path.toString().endsWith(".java") || path.toString().endsWith(".json")).toList()) {
					String shown = RepoPaths.display(file);
					if (SourceAssertions.read(file).contains("archon_choice_made")
							&& !shown.endsWith("encounter/FungalGardenTravelHelper.java")
							&& !shown.startsWith("src/main/resources/data/hemomancy/recipe/armature_upgrade/")) literal.add(shown);
				}
			}
		}
		assertEquals(List.of(), literal, "Read the Archon choice from the degree's path");
		SourceAssertions.assertContains("the migration clears the old key",
				SourceAssertions.read(ENTRY_POINTS.get("InitiatoryDegreeEvents::migrateLegacyArchonChoice")),
				"persistent.remove(FungalGardenTravelHelper.ARCHON_CHOICE_KEY)");
	}

	@Test
	void theFungalScarSweepNoLongerTicks() throws Exception {
		List<String> ticking = new ArrayList<>();
		try (Stream<Path> files = Files.walk(RepoPaths.ROOT.resolve("src/main/java"))) {
			for (Path file : files.filter(path -> path.toString().endsWith(".java")).toList()) {
				String text = SourceAssertions.read(file);
				if (text.contains("PlayerTickEvent") && text.contains("FungalScarMigrationRules")) {
					ticking.add(RepoPaths.display(file));
				}
			}
		}
		assertEquals(List.of(), ticking);
	}
}
