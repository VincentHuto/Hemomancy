package com.vincenthuto.hemomancy.client.model.entity.boss.velorum;

import com.vincenthuto.hemomancy.common.entity.boss.BossMoveTimeline;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintMoveInputs;
import com.vincenthuto.hemomancy.common.entity.boss.saint.velorum.VelorumDeedRules;
import com.vincenthuto.hemomancy.common.entity.boss.saint.velorum.VelorumMove;
import com.vincenthuto.hemomancy.common.entity.boss.saint.velorum.VelorumOpeningRules;
import org.junit.jupiter.api.Test;

import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

class VelorumAnimStateTest {
	@Test
	void theEndingShowsOverAnOpeningOverAMoveOverTheStance() {
		assertEquals(VelorumAnimState.TRACK_ENDING, VelorumAnimState.select(3, VelorumOpeningRules.BOW_ID, 5.0F));
		assertEquals(VelorumAnimState.TRACK_OPENING + VelorumOpeningRules.BOW_ID,
				VelorumAnimState.select(3, VelorumOpeningRules.BOW_ID, -1.0F));
		assertEquals(VelorumAnimState.TRACK_MOVE + 3, VelorumAnimState.select(3, 0, -1.0F));
		assertEquals(VelorumAnimState.TRACK_IDLE, VelorumAnimState.select(0, 0, -1.0F));
		var frame = VelorumAnimState.Frame.opening(VelorumOpeningRules.GUTTER_ID, 12.0F, 60);
		assertEquals(VelorumAnimState.TRACK_OPENING + VelorumOpeningRules.GUTTER_ID, frame.track());
		assertEquals(12.0F, frame.trackTime());
	}

	@Test
	void aShortenedRecoveryStillPlaysTheWholeSettle() {
		int repose = IntStream.range(0, 1 << 8).filter(VelorumDeedRules::repose).findFirst().orElseThrow();
		SaintMoveInputs inputs = new SaintMoveInputs(1, 0, repose, 1);
		VelorumMove move = VelorumMove.CRUOR_RELEASE;
		BossMoveTimeline authored = move.authored();
		BossMoveTimeline live = move.timeline(inputs);
		assertTrue(live.recovery() < authored.recovery(), "the Repose deed shortens the recovery");
		assertEquals(authored.windup() + 3.0F, VelorumAnimState.trackTime(move, authored.windup() + 3.0F, inputs), 1.0E-4F,
				"the active span runs at its own pace");
		assertEquals(authored.total(), VelorumAnimState.trackTime(move, live.total(), inputs), 1.0E-3F,
				"the recovery's keys play out in full, faster");
		assertEquals(10.0F, VelorumAnimState.trackTime(move, 10.0F, SaintMoveInputs.NONE), 1.0E-4F);
	}

	@Test
	void aCutWindupLandsTheCommitOnItsAuthoredBeat() {
		SaintMoveInputs phaseThree = new SaintMoveInputs(3, 0, 0, 1);
		for (VelorumMove move : VelorumMove.values()) {
			if (!move.inPhase(3)) continue;
			int cut = move.timeline(phaseThree).windup();
			assertEquals(move.authored().windup(), VelorumAnimState.trackTime(move, cut, phaseThree), 1.0E-3F, move.name());
		}
	}
}
