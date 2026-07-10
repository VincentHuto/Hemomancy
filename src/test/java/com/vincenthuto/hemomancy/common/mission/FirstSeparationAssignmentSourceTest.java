package com.vincenthuto.hemomancy.common.mission;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

class FirstSeparationAssignmentSourceTest {
	private static final Path HELPER = Path.of(
			"src/main/java/com/vincenthuto/hemomancy/common/mission/FirstSeparationAssignmentHelper.java");
	private static final Path BRIEFING = Path.of(
			"src/main/resources/data/hemomancy/advancement/hemomancy/first_separation_briefed.json");
	private static final Path CLAIM = Path.of(
			"src/main/resources/data/hemomancy/advancement/hemomancy/first_separation_reward_claimed.json");
	private static final Path ALCHEMIST = Path.of(
			"src/main/java/com/vincenthuto/hemomancy/common/entity/npc/dialogue/HarbingerAlchemistDialogueTrees.java");
	private static final Path HANDLER = Path.of(
			"src/main/java/com/vincenthuto/hemomancy/common/entity/npc/dialogue/DialogueEventHandler.java");

	@Test
	void firstSeparationOwnsBriefingClaimAndInitializedSamplingKit() throws IOException {
		String helper = read(HELPER);
		String briefing = read(BRIEFING);
		String claim = read(CLAIM);

		assertContains(helper, "Hemomancy.rloc(\"hemomancy/first_separation_briefed\")");
		assertContains(helper, "Hemomancy.rloc(\"hemomancy/first_separation_reward_claimed\")");
		assertContains(helper, "HemoCapabilityAccess.getPlayerDegreeNumber(player) >= 2");
		assertContains(helper, "HarbingerAdvancementGranter.isFirstSeparationStarted(player)");
		assertContains(helper, "HarbingerAdvancementGranter.isFirstSeparationComplete(player)");
		assertContains(helper, "VialRackItem.ensureInitialized(rack)");
		assertContains(helper, "new ItemStack(ItemInit.living_syringe.get())");
		assertContains(helper, "new ItemStack(ItemInit.vial_rack.get())");
		assertContains(helper, "return isBriefed(player);");
		assertContains(helper, "return isClaimed(player);");
		assertContains(briefing, "\"trigger\": \"minecraft:impossible\"");
		assertContains(claim, "\"trigger\": \"minecraft:impossible\"");
		String alchemist = read(ALCHEMIST);
		String handler = read(HANDLER);
		assertContains(alchemist, "EVENT_FIRST_SEPARATION_BRIEF");
		assertContains(alchemist, "EVENT_FIRST_SEPARATION_CLAIM");
		assertContains(alchemist, "canBriefFirstSeparation");
		assertContains(alchemist, "canClaimFirstSeparation");
		assertContains(handler, "FirstSeparationAssignmentHelper.markClaimed(player)");
		assertOrdered(handler, "FirstSeparationAssignmentHelper.markClaimed(player)",
				"FirstSeparationAssignmentHelper.rewardStacks()");
	}

	private static String read(Path path) throws IOException {
		if (!Files.exists(path)) throw new AssertionError("missing " + path);
		return Files.readString(path).replace("\r\n", "\n");
	}

	private static void assertContains(String text, String expected) {
		if (!text.contains(expected)) throw new AssertionError("missing " + expected);
	}

	private static void assertOrdered(String text, String first, String second) {
		if (text.indexOf(first) < 0 || text.indexOf(second) < 0 || text.indexOf(first) >= text.indexOf(second)) {
			throw new AssertionError("expected " + first + " before " + second);
		}
	}
}
