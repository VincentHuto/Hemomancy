package com.vincenthuto.hemomancy.common.mission;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class HarbingerAssignmentStructureSourceTest {
	private static final Path SOURCE_ROOT = Path.of("src/main/java");
	private static final Path RESOURCE_ROOT = Path.of("src/main/resources");

	private HarbingerAssignmentStructureSourceTest() {
	}

	public static void main(String[] args) throws IOException {
		assignmentLedgerSeparatesMainAndSideAssignments();
		d2CentrifugeMainAssignmentExists();
	}

	private static void assignmentLedgerSeparatesMainAndSideAssignments() throws IOException {
		String ledgerScreen = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/client/screen/item/HarbingerAssignmentLedgerScreen.java"));
		String language = read(RESOURCE_ROOT.resolve("assets/hemomancy/lang/en_us.json"));
		String docs = read(Path.of("docs/HEMOMANCY_REFERENCE.md"));

		assertContains("ledger renders D1 main assignment", ledgerScreen, "renderFirstBloodcraft");
		assertContains("ledger renders D1 Hermit Road side assignment", ledgerScreen, "renderHermitRoad");
		assertContains("ledger renders D2 main assignment before D2 sides", ledgerScreen, "renderFirstSeparation");
		assertContains("ledger labels Taxonomy as side assignment", language,
				"screen.hemomancy.harbinger_assignment_ledger.red_taxonomy.side_title");
		assertContains("ledger labels Eightfold as side assignment", language,
				"screen.hemomancy.harbinger_assignment_ledger.enzyme_mastery.side_title");
		assertContains("reference docs name D1 main assignment", docs, "Main D1 assignment, **First Bloodcraft**");
		assertContains("reference docs name D2 main assignment", docs, "Main D2 assignment, **The First Separation**");
	}

	private static void d2CentrifugeMainAssignmentExists() throws IOException {
		String advancementGranter = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/event/HarbingerAdvancementGranter.java"));
		String startPacket = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/network/capa/harbinger/manips/StartCentrifugeButtonPacket.java"));
		String ledgerItem = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/item/harbinger/HarbingerAssignmentLedgerItem.java"));
		String ledgerPacket = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/network/mission/OpenHarbingerAssignmentLedgerPacket.java"));
		String ledgerScreen = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/client/screen/item/HarbingerAssignmentLedgerScreen.java"));
		String language = read(RESOURCE_ROOT.resolve("assets/hemomancy/lang/en_us.json"));

		assertContains("first separation advancement constant exists", advancementGranter,
				"ADV_FIRST_SEPARATION_STARTED");
		assertContains("first separation completion helper exists", advancementGranter,
				"isFirstSeparationStarted");
		assertContains("centrifuge startup grants first separation when successful", startPacket,
				"ADV_FIRST_SEPARATION_STARTED");
		assertContains("ledger item detects vial centrifuge", ledgerItem, "hasVialCentrifuge");
		assertContains("ledger item detects sampled vial", ledgerItem, "hasSampledBloodVial");
		assertContains("ledger item detects any enzyme", ledgerItem, "hasAnyEnzyme");
		assertContains("ledger packet carries vial centrifuge state", ledgerPacket, "hasVialCentrifuge");
		assertContains("ledger packet carries sampled vial state", ledgerPacket, "hasSampledBloodVial");
		assertContains("ledger packet carries centrifuge startup state", ledgerPacket, "firstSeparationStarted");
		assertContains("ledger packet carries enzyme acquisition state", ledgerPacket, "hasAnyEnzyme");
		assertContains("ledger screen renders first separation", ledgerScreen, "renderFirstSeparation");
		assertContains("first separation displays four steps", ledgerScreen, "firstSeparationProgress(), 4");
		assertContains("first separation language title exists", language,
				"screen.hemomancy.harbinger_assignment_ledger.first_separation.title");
		assertContains("first separation advancement exists", read(RESOURCE_ROOT.resolve(
				"data/hemomancy/advancement/hemomancy/first_separation_started.json")),
				"advancements.hemomancy.first_separation_started.title");
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
