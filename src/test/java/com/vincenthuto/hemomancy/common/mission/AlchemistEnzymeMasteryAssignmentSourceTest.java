package com.vincenthuto.hemomancy.common.mission;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class AlchemistEnzymeMasteryAssignmentSourceTest {
	private static final Path SOURCE_ROOT = Path.of("src/main/java");
	private static final Path RESOURCE_ROOT = Path.of("src/main/resources");

	public static void main(String[] args) throws Exception {
		enzymeMasteryAssignmentExists();
	}

	private static void enzymeMasteryAssignmentExists() throws IOException {
		String advancementGranter = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/event/HarbingerAdvancementGranter.java"));
		String bloodVolumeEvents = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/capability/player/harbinger/bloodvolume/BloodVolumeEvents.java"));
		String ledgerItem = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/item/harbinger/HarbingerAssignmentLedgerItem.java"));
		String ledgerPacket = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/network/mission/OpenHarbingerAssignmentLedgerPacket.java"));
		String ledgerScreen = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/client/screen/item/HarbingerAssignmentLedgerScreen.java"));
		String alchemistEntity = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/entity/npc/harbinger/HarbingerAlchemistEntity.java"));
		String language = read(RESOURCE_ROOT.resolve("assets/hemomancy/lang/en_us.json"));
		String docs = read(Path.of("docs/HEMOMANCY_REFERENCE.md"));

		for (String enzyme : new String[] {
				"vivacious", "fervent", "neurotic", "incandescent",
				"ruinous", "frigid", "ferric", "umbral"
		}) {
			assertContains(enzyme + " enzyme mastery advancement constant exists", advancementGranter,
					"ADV_ENZYME_MASTERY_" + enzyme.toUpperCase());
			assertContains(enzyme + " enzyme mastery scanner checks item", bloodVolumeEvents,
					"ItemInit." + enzyme + "_enzyme.get()");
			assertContains(enzyme + " enzyme mastery advancement json exists", read(RESOURCE_ROOT.resolve(
					"data/hemomancy/advancement/hemomancy/enzyme_mastery_" + enzyme + ".json")),
					"advancements.hemomancy.enzyme_mastery_" + enzyme + ".title");
		}

		assertContains("enzyme mastery completion constant exists", advancementGranter,
				"ADV_ENZYME_MASTERY_COMPLETE");
		assertContains("enzyme mastery helper counts records", advancementGranter,
				"getEnzymeMasteryCount");
		assertContains("enzyme mastery completion helper exists", advancementGranter,
				"isEnzymeMasteryComplete");
		assertContains("D2+ inventory scanner awards enzyme mastery", bloodVolumeEvents,
				"recordEnzymeMastery(serverPlayer)");
		assertContains("Alchemist sample turn-ins are gated to D2+", alchemistEntity,
				"degree >= 2 ? findHeldRedTaxonomySample(held) : null");
		assertContains("ledger item sends enzyme mastery count", ledgerItem,
				"getEnzymeMasteryCount");
		assertContains("ledger packet carries enzyme mastery count", ledgerPacket,
				"enzymeMasteryCount");
		assertContains("ledger screen renders enzyme mastery assignment", ledgerScreen,
				"renderEnzymeMastery");
		assertContains("ledger screen displays eight enzyme steps", ledgerScreen,
				"enzymeMasteryCount, 8");
		assertContains("enzyme mastery language title exists", language,
				"screen.hemomancy.harbinger_assignment_ledger.enzyme_mastery.title");
		assertContains("enzyme mastery completion advancement exists", read(RESOURCE_ROOT.resolve(
				"data/hemomancy/advancement/hemomancy/enzyme_mastery_complete.json")),
				"advancements.hemomancy.enzyme_mastery_complete.title");
		assertContains("reference docs describe enzyme mastery", docs,
				"The Eightfold Centrifuge");
	}

	private static String read(Path path) throws IOException {
		return Files.readString(path);
	}

	private static void assertContains(String label, String text, String needle) {
		if (!text.contains(needle)) {
			throw new AssertionError(label + " missing: " + needle);
		}
	}
}
