package com.vincenthuto.hemomancy.client.data;

import com.vincenthuto.hemomancy.common.rite.harbinger.ActiveRiteData.SigilSegment;
import com.vincenthuto.hemomancy.common.rite.sigil.IchorianSigilGougeStages;
import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class SigilGougeClientStateTest {
	private static final BlockPos CENTER = new BlockPos(10, 64, -4);
	private static final String SIGIL = "hemomancy:reservoir@0";
	private static final float EPSILON = 1.0e-4F;

	@AfterEach
	void clearState() {
		SigilGougeClientState.clear();
	}

	@Test
	void aRiteFirstSeenMidwayShowsItsLinksAndAwakeningSettled() {
		SigilGougeClientState.observe(CENTER, List.of(link(0, true), link(1, true)), 50L, true);
		assertEquals(settled(60.0F), SigilGougeClientState.linkAge(CENTER, SIGIL, 0, 60.0F), EPSILON);
		assertEquals(settled(60.0F), SigilGougeClientState.linkAge(CENTER, SIGIL, 1, 60.0F), EPSILON);
		assertEquals(settled(60.0F), SigilGougeClientState.awakeAge(CENTER, SIGIL, 60.0F), EPSILON);
	}

	@Test
	void aSettledAwakeningStillAdvancesSoItsFrontReRuns() {
		SigilGougeClientState.observe(CENTER, List.of(link(0, true), link(1, true)), 50L, true);
		float early = SigilGougeClientState.awakeAge(CENTER, SIGIL, 60.0F);
		float later = SigilGougeClientState.awakeAge(CENTER, SIGIL, 100.0F);
		assertTrue(early >= IchorianSigilGougeStages.SETTLED, "still reads as long done");
		assertEquals(40.0F, later - early, 0.2F, "the settled age moves with the game time");
	}

	@Test
	void aRiteThatLeavesTheSyncKeepsItsStampsForItsEnding() {
		SigilGougeClientState.observe(CENTER, List.of(link(0, false), link(1, false)), 50L, true);
		SigilGougeClientState.observe(CENTER, List.of(link(0, true), link(1, true), link(2, true)), 100L, false);
		SigilGougeClientState.retire(CENTER, 110L);
		assertEquals(20.0F, SigilGougeClientState.awakeAge(CENTER, SIGIL, 120.0F), EPSILON,
				"the ghost reads the awakening it had, not a fresh or missing one");
		assertEquals(15.0F, SigilGougeClientState.linkAge(CENTER, SIGIL, 2, 115.0F), EPSILON);

		SigilGougeClientState.observe(new BlockPos(0, 0, 0), List.of(), 110L + SigilGougeClientState.RETIRED_KEEP_TICKS + 1L, false);
		assertTrue(SigilGougeClientState.awakeAge(CENTER, SIGIL, 400.0F) < 0.0F, "retired stamps go once every ending is over");
	}

	@Test
	void aLaterLinkIsStampedWhenItArrives() {
		SigilGougeClientState.observe(CENTER, List.of(link(0, false), link(1, false)), 50L, true);
		SigilGougeClientState.observe(CENTER, List.of(link(0, false), link(1, false), link(2, false)), 100L, false);
		assertEquals(10.0F, SigilGougeClientState.linkAge(CENTER, SIGIL, 2, 110.0F), EPSILON);
		assertEquals(settled(110.0F), SigilGougeClientState.linkAge(CENTER, SIGIL, 0, 110.0F), EPSILON);
		assertEquals(settled(110.0F), SigilGougeClientState.linkAge(CENTER, SIGIL, 1, 110.0F), EPSILON);
	}

	@Test
	void theAwakeningIsStampedWhenTheFlagFlips() {
		SigilGougeClientState.observe(CENTER, List.of(link(0, false), link(1, false)), 50L, true);
		assertTrue(SigilGougeClientState.awakeAge(CENTER, SIGIL, 199.0F) < 0.0F, "not awake before the flip");
		SigilGougeClientState.observe(CENTER, List.of(link(0, true), link(1, true)), 200L, false);
		assertEquals(15.0F, SigilGougeClientState.awakeAge(CENTER, SIGIL, 215.0F), EPSILON);
	}

	@Test
	void aNewRiteOnTheSameCentreCutsInFresh() {
		SigilGougeClientState.observe(CENTER, List.of(link(0, true), link(1, true)), 50L, true);
		SigilGougeClientState.observe(CENTER, List.of(), 60L, false);
		SigilGougeClientState.observe(CENTER, List.of(link(0, false)), 70L, false);
		assertEquals(5.0F, SigilGougeClientState.linkAge(CENTER, SIGIL, 0, 75.0F), EPSILON,
				"an empty sync drops the old stamps");

		SigilGougeClientState.forget(CENTER);
		SigilGougeClientState.observe(CENTER, List.of(link(0, false)), 300L, false);
		assertEquals(5.0F, SigilGougeClientState.linkAge(CENTER, SIGIL, 0, 305.0F), EPSILON);
		assertTrue(SigilGougeClientState.awakeAge(CENTER, SIGIL, 305.0F) < 0.0F, "the new pattern is not awake");
	}

	private static float settled(float gameTime) {
		return IchorianSigilGougeStages.settledAge(gameTime);
	}

	private static SigilSegment link(int index, boolean awakened) {
		return new SigilSegment(index, 64.1, 0, index + 1, 64.1, 0, 0xAA1122, SIGIL, index, 5, awakened);
	}
}
