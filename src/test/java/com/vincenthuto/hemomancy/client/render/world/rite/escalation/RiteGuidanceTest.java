package com.vincenthuto.hemomancy.client.render.world.rite.escalation;

import com.vincenthuto.hemomancy.client.data.ActiveRiteClientData;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteProblems;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteProblems.RiteProblem;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

final class RiteGuidanceTest {
	private static final RiteProblem MEDIUM = new RiteProblem(CardinalRiteProblems.MEDIUM, List.of());

	private static RiteGuidance.Decision decide(RiteGuidance.Seen seen, String phase, List<RiteProblem> problems,
			long now) {
		return RiteGuidance.decide(seen, phase, "", false, problems, now);
	}

	@Test
	void aNewPhaseSpeaksFirstAndProblemsWaitTheirTurn() {
		RiteGuidance.Decision first = decide(RiteGuidance.Seen.NONE, "INSCRIPTION", List.of(MEDIUM), 0);
		assertEquals("hemomancy.rite.guide.inscription", first.line().key());

		RiteGuidance.Decision waiting = decide(first.seen(), "INSCRIPTION", List.of(MEDIUM), 10);
		assertNull(waiting.line(), "the phase line keeps the action bar for a while");

		RiteGuidance.Decision problem = decide(waiting.seen(), "INSCRIPTION", List.of(MEDIUM),
				RiteGuidance.QUIET_TICKS + 1);
		assertEquals(CardinalRiteProblems.MEDIUM, problem.line().key());
	}

	@Test
	void aProblemIsAnnouncedOnceUntilItIsResolved() {
		RiteGuidance.Seen seen = decide(RiteGuidance.Seen.NONE, "INSCRIPTION", List.of(MEDIUM), 0).seen();
		seen = decide(seen, "INSCRIPTION", List.of(MEDIUM), 100).seen();
		assertNull(decide(seen, "INSCRIPTION", List.of(MEDIUM), 300).line(), "no repeats");

		seen = decide(seen, "INSCRIPTION", List.of(), 400).seen();
		assertEquals(CardinalRiteProblems.MEDIUM, decide(seen, "INSCRIPTION", List.of(MEDIUM), 500).line().key(),
				"a problem that comes back is announced again");
	}

	@Test
	void sacrificeProgressDoesNotRepeatTheLine() {
		RiteProblem owed = new RiteProblem(CardinalRiteProblems.SACRIFICE, List.of("200"));
		RiteGuidance.Seen seen = decide(RiteGuidance.Seen.NONE, "OTHER", List.of(owed), 0).seen();
		RiteProblem less = new RiteProblem(CardinalRiteProblems.SACRIFICE, List.of("150"));

		assertNull(decide(seen, "OTHER", List.of(less), 500).line());
	}

	@Test
	void eachSleepingSigilIsItsOwnProblem() {
		RiteProblem bastion = new RiteProblem(CardinalRiteProblems.SIGIL_REQUIRED, List.of("Bastion"));
		RiteProblem lattice = new RiteProblem(CardinalRiteProblems.SIGIL_REQUIRED, List.of("Hematic Lattice"));
		RiteGuidance.Seen seen = decide(RiteGuidance.Seen.NONE, "OTHER", List.of(bastion, lattice), 0).seen();

		assertEquals(List.of("Hematic Lattice"), decide(seen, "OTHER", List.of(bastion, lattice), 500).line().args());
	}

	@Test
	void successionCuesSpeakWhenTheyChange() {
		RiteGuidance.Decision first = RiteGuidance.decide(RiteGuidance.Seen.NONE, "SUCCESSION",
				"hemomancy.succession.phase.1", true, List.of(), 0);
		assertEquals("hemomancy.succession.phase.1", first.line().key());
		assertNull(RiteGuidance.decide(first.seen(), "SUCCESSION", "hemomancy.succession.phase.1", true,
				List.of(), 500).line());
		assertEquals("hemomancy.succession.phase.2", RiteGuidance.decide(first.seen(), "SUCCESSION",
				"hemomancy.succession.phase.2", true, List.of(), 500).line().key());
	}

	@Test
	void onlyPhasesWithAnInstructionHaveALine() {
		assertEquals("hemomancy.rite.guide.consecration", RiteGuidance.phaseKey("CONSECRATION"));
		assertEquals("hemomancy.rite.guide.offering_procession", RiteGuidance.phaseKey("OFFERING_PROCESSION"));
		assertNull(RiteGuidance.phaseKey("ORDEAL"), "waves keep their own cues");
		assertNull(RiteGuidance.phaseKey("COMPLETE"));
		assertNull(RiteGuidance.phaseKey("SUCCESSION"));
	}

	@Test
	void onlyTheRitesOwnerHearsIt() {
		UUID owner = UUID.randomUUID();
		ActiveRiteClientData.RiteEntry entry = new ActiveRiteClientData.RiteEntry(BlockPos.ZERO, 5, 0.0D,
				ResourceLocation.parse("hemomancy:cardinal_rite/guidance_test"), false, "INSCRIPTION", 0, 0, 0, 1, 1,
				0, 0, 0, 0, 0, "", 6.0F, List.of(), List.of(), List.of(), List.of(), true, owner, 0, -1,
				"storm", true, true, 0);

		assertTrue(RiteGuidance.listensTo(entry, owner));
		assertFalse(RiteGuidance.listensTo(entry, UUID.randomUUID()));
		assertFalse(RiteGuidance.listensTo(entry, null));
	}
	@Test
	void aWakingRingIsAnnouncedAfterThePhaseLine() {
		RiteGuidance.Decision first = RiteGuidance.decide(RiteGuidance.Seen.NONE, "CONSECRATION", "", false, List.of(), 0, 1);
		RiteGuidance.Decision ring = RiteGuidance.decide(first.seen(), "ORDEAL", "", false, List.of(),
				RiteGuidance.QUIET_TICKS + 1, 2);
		assertEquals("hemomancy.rite.guide.ring_woke.2", ring.line().key(),
				"the ordeal has no guide line of its own, so the ring speaks at once");
		assertNull(RiteGuidance.decide(ring.seen(), "ORDEAL", "", false, List.of(), RiteGuidance.QUIET_TICKS * 3, 2)
				.line(), "once only");
	}

	@Test
	void ringsAlreadyAwakeWhenFirstSeenAreNotAnnounced() {
		RiteGuidance.Decision first = RiteGuidance.decide(RiteGuidance.Seen.NONE, "ORDEAL", "", false, List.of(), 0, 3);
		assertNull(RiteGuidance.decide(first.seen(), "ORDEAL", "", false, List.of(), RiteGuidance.QUIET_TICKS + 1, 3)
				.line());
		assertEquals("hemomancy.rite.guide.ring_woke.8", RiteGuidance.ringKey(11), "ordinals stop at the eighth");
	}

	@Test
	void theStillIntervalLineComesFirstThenTheRing() {
		RiteGuidance.Decision first = RiteGuidance.decide(RiteGuidance.Seen.NONE, "ORDEAL", "", false, List.of(), 0, 2);
		RiteGuidance.Decision phase = RiteGuidance.decide(first.seen(), "STILL_INTERVAL", "", false, List.of(),
				RiteGuidance.QUIET_TICKS + 1, 3);
		assertEquals("hemomancy.rite.guide.still_interval", phase.line().key());
		RiteGuidance.Decision ring = RiteGuidance.decide(phase.seen(), "STILL_INTERVAL", "", false, List.of(),
				RiteGuidance.QUIET_TICKS * 2 + 2, 3);
		assertEquals("hemomancy.rite.guide.ring_woke.3", ring.line().key());
	}
}
