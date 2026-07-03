package com.vincenthuto.hemomancy.common.capability.player.harbinger.scar.fungal;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class FungalScarMigrationRulesSourceTest {
	private static final Path ROOT = Path.of("").toAbsolutePath();
	private static final Path MIGRATION_RULES = ROOT.resolve(
			"src/main/java/com/vincenthuto/hemomancy/common/capability/player/harbinger/scar/fungal/FungalScarMigrationRules.java");
	private static final String[][] MIGRATIONS = {
			{ "respergillus", "noctifly_agaric" },
			{ "lumina_devorans", "oculiflora_reticularis" },
			{ "anastocordyceps_nexus", "oculiflora_reticularis" },
			{ "thanomyces_resurgens", "cryostroma_perdurans" },
			{ "sanguiflora_cadens", "putrivora_resolvens" }
	};

	private FungalScarMigrationRulesSourceTest() {
	}

	public static void main(String[] args) throws IOException {
		migrationHelperOwnsTheOnlyLegacyIdMap();
		migrationEventsSweepInventoryAndScarSlot();
	}

	private static void migrationHelperOwnsTheOnlyLegacyIdMap() throws IOException {
		String source = read(MIGRATION_RULES);
		assertContains("migration helper exposes stack migration", source, "ItemStack migrate(ItemStack stack)");
		assertContains("migration helper exposes path lookup", source, "migrationTargetPath(String path)");
		for (String[] migration : MIGRATIONS) {
			assertContains("legacy source id " + migration[0], source, "\"" + migration[0] + "\"");
			assertContains("survivor target id " + migration[1], source, "\"" + migration[1] + "\"");
		}
	}

	private static void migrationEventsSweepInventoryAndScarSlot() throws IOException {
		Path events = ROOT.resolve("src/main/java/com/vincenthuto/hemomancy/common/event/FungalScarMigrationEvents.java");
		String source = read(events);
		assertContains("login migration hook", source, "PlayerLoggedInEvent");
		assertContains("tick fallback hook", source, "PlayerTickEvent.Post");
		assertContains("scar slot migration", source, "setFungalScar(FungalScarMigrationRules.migrate");
		assertContains("inventory migration", source, "player.getInventory()");
		assertContains("no client-side migration", source, "player.level().isClientSide");
	}

	private static String read(Path path) throws IOException {
		if (!Files.exists(path)) {
			throw new AssertionError("missing " + path);
		}
		return Files.readString(path).replace("\r\n", "\n");
	}

	private static void assertContains(String label, String text, String expected) {
		if (!text.contains(expected)) {
			throw new AssertionError(label + ": missing " + expected);
		}
	}
}
