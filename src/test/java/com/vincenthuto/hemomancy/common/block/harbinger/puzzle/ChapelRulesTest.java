package com.vincenthuto.hemomancy.common.block.harbinger.puzzle;

import com.vincenthuto.hemomancy.common.block.harbinger.puzzle.ChapelRules.Mark;
import com.vincenthuto.hemomancy.common.block.harbinger.puzzle.ChapelRules.MarkKind;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChapelRulesTest {
	@Test
	void onlyTheEighthDegreeAttemptsAChapel() {
		assertTrue(ChapelRules.mayAttempt(8));
		assertFalse(ChapelRules.mayAttempt(7));
	}

	@Test
	void aBeholdingReplaysFourWitnessesFromItsSeed() {
		List<Integer> sequence = ChapelRules.beholdingSequence(42L);
		assertEquals(ChapelRules.BEHOLDING_STEPS, sequence.size());
		assertTrue(sequence.stream().allMatch(witness -> witness >= 0 && witness < 4));
		assertEquals(sequence, ChapelRules.beholdingSequence(42L), "the same seed replays the same memory");
		for (int step = 1; step < sequence.size(); step++) {
			assertFalse(sequence.get(step).equals(sequence.get(step - 1)), "no witness twice in a row");
		}
		assertEquals(sequence, ChapelRules.beholdingSequence(42L, 4), "four witnesses is the default");
		assertTrue(ChapelRules.beholdingSequence(9L, 6).stream().allMatch(witness -> witness < 6));
	}

	@Test
	void aLitanyNamesThreeDifferentOfferings() {
		List<Integer> order = ChapelRules.litanyOrder(7L);
		assertEquals(3, order.size());
		assertEquals(3, new HashSet<>(order).size());
		assertTrue(order.stream().allMatch(index -> index >= 0 && index < ChapelRules.LITANY_CHOICES));
	}

	@Test
	void reposeRewardsStillnessAndTheSilentiumMost() {
		assertEquals(2, ChapelRules.reposeGain(true, true));
		assertEquals(1, ChapelRules.reposeGain(false, true));
		assertEquals(0, ChapelRules.reposeGain(true, false), "moving earns nothing");
		assertTrue(ChapelRules.still(0.0001));
		assertFalse(ChapelRules.still(0.05));
		assertEquals(600, ChapelRules.REPOSE_TICKS);
	}

	@Test
	void aDownfallIsThreeWavesOfThree() {
		assertEquals(3, ChapelRules.DOWNFALL_WAVES);
		assertEquals(3, ChapelRules.DOWNFALL_WAVE_SIZE);
	}

	@Test
	void aWitnessIsTouchedByStandingAgainstItsStele() {
		assertTrue(ChapelRules.atWitness(0.8, 0.0), "pressed against the stele's face");
		assertTrue(ChapelRules.atWitness(1.0, 0.5), "at its corner");
		assertFalse(ChapelRules.atWitness(2.0, 0.5), "a step away is not touching it");
	}

	/**
	 * Track 3: the generator writes a mark's offset with the focus facing north; a placed piece turns the focus's facing
	 * by its rotation and the mark must land where the template put it, for every rotation.
	 */
	@Test
	void aMarkTurnsWithItsFocusAsTheTemplateDoes() {
		BlockPos focus = new BlockPos(5, 1, 6);
		BlockPos witness = new BlockPos(-19, 0, -3);
		for (Rotation rotation : Rotation.values()) {
			BlockPos placedFocus = StructureTemplate.transform(focus, Mirror.NONE, rotation, BlockPos.ZERO);
			BlockPos placedWitness = StructureTemplate.transform(focus.offset(witness), Mirror.NONE, rotation,
					BlockPos.ZERO);
			Direction facing = rotation.rotate(Direction.NORTH);
			assertEquals(placedWitness, ChapelRules.place(placedFocus, witness, facing), rotation.name());
		}
	}

	@Test
	void aChallengerMayRoamTheWholeWing() {
		List<Mark> wing = List.of(new Mark(new BlockPos(-26, 0, 0), MarkKind.START, 0),
				new Mark(new BlockPos(-9, 0, 2), MarkKind.SPAWN, 2));
		assertEquals(26 + ChapelRules.LEAVE_MARGIN, ChapelRules.reach(wing), 1e-9);
		assertEquals(ChapelRules.CHAPEL_RADIUS * 2, ChapelRules.reach(List.of()), 1e-9, "never under a room's reach");
	}

	@Test
	void eachDownfallWaveRisesFromItsOwnSeals() {
		List<Mark> wing = List.of(new Mark(new BlockPos(-21, 0, -2), MarkKind.SPAWN, 0),
				new Mark(new BlockPos(-21, 0, 2), MarkKind.SPAWN, 0), new Mark(new BlockPos(-15, 0, 2), MarkKind.SPAWN, 1),
				new Mark(new BlockPos(-26, 0, 0), MarkKind.START, 0));
		assertEquals(2, ChapelRules.seals(wing, 0).size());
		assertEquals(List.of(wing.get(2)), ChapelRules.seals(wing, 1));
		assertEquals(3, ChapelRules.seals(wing, 2).size(), "a wave with no seals of its own rises from all of them");
		assertEquals(List.of(wing.get(3)), ChapelRules.marksOf(wing, MarkKind.START));
	}

	@Test
	void reposePulsesNeverTakeTheLastOfAPlayersLife() {
		assertEquals(2f, ChapelRules.pulseDamage(20f));
		assertEquals(0f, ChapelRules.pulseDamage(3f));
	}

	@Test
	void pylonsWindUpBeforeEachPulseAndSpendItAsItFires() {
		int pulse = ChapelRules.REPOSE_PULSE_TICKS;
		assertEquals(0, ChapelRules.pylonCharge(0));
		assertEquals(0, ChapelRules.pylonCharge(pulse - ChapelRules.PYLON_WIND_TICKS - 1));
		assertEquals(1, ChapelRules.pylonCharge(pulse - ChapelRules.PYLON_WIND_TICKS));
		assertEquals(2, ChapelRules.pylonCharge(pulse - ChapelRules.PYLON_WIND_TICKS / 2));
		assertEquals(3, ChapelRules.pylonCharge(pulse - 1));
		assertEquals(0, ChapelRules.pylonCharge(pulse), "the pulse fires and the charge is spent");
	}

	@Test
	void aLitanyKeepsItsWordsAcrossAttempts() {
		assertEquals(ChapelRules.litanySeed(123L, 1), ChapelRules.litanySeed(123L, 1));
		assertFalse(ChapelRules.litanySeed(123L, 1) == ChapelRules.litanySeed(456L, 1));
		assertEquals(1200, ChapelRules.LITANY_ANSWER_TICKS);
	}
}
