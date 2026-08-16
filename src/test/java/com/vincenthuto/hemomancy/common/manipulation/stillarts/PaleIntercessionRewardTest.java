package com.vincenthuto.hemomancy.common.manipulation.stillarts;

import com.vincenthuto.hemomancy.common.capability.player.unstained.EnumClarityStage;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PaleIntercessionRewardTest {
	@Test
	void resoluteAndLaterPlayersReceivePaleIntercession() {
		assertTrue(StillArtRewardTable.eligibleArtNames(EnumClarityStage.RESOLUTE).contains("pale_intercession"));
		assertTrue(StillArtRewardTable.eligibleArtNames(EnumClarityStage.ENLIGHTENED).contains("pale_intercession"));
	}

	@Test
	void vigilantPlayersDoNotReceivePaleIntercession() {
		assertFalse(StillArtRewardTable.eligibleArtNames(EnumClarityStage.VIGILANT).contains("pale_intercession"));
	}

	@Test
	void resoluteAdvancementUsesTheResoluteRewardTable() {
		assertTrue(StillArtRewardTable.stageForAdvancementPath("hemomancy/resolute_stage")
				.filter(stage -> stage == EnumClarityStage.RESOLUTE).isPresent());
	}

	@Test
	void loginBackfillAndAdminClarityBothGrantEligibleArts() throws Exception {
		String events = Files.readString(Path.of("src/main/java/com/vincenthuto/hemomancy/common/capability/player/unstained/stillart/KnownStillArtEvents.java"));
		String command = Files.readString(Path.of("src/main/java/com/vincenthuto/hemomancy/common/command/HemoCommand.java"));
		assertTrue(events.contains("playerLoggedIn") && events.contains("grantEligibleArts(player, progress)"));
		assertTrue(command.contains("KnownStillArtEvents.grantEligibleArts(player, cap)"));
	}
}
