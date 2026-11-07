package com.vincenthuto.hemomancy.common.entity.boss.saint.velorum;

import com.vincenthuto.hemomancy.common.block.harbinger.puzzle.ChapelRules;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintDeeds;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VelorumDeedRulesTest {
	@Test
	void eachSkippedChapelAloneAndTogether() {
		int all = SaintDeeds.of("FORCED", List.of("BEHOLDING", "LITANY", "REPOSE")).mask();
		assertTrue(VelorumDeedRules.beholding(all) && VelorumDeedRules.litany(all) && VelorumDeedRules.repose(all));
		int litany = SaintDeeds.of("FORCED", List.of("LITANY")).mask();
		assertTrue(VelorumDeedRules.litany(litany));
		assertFalse(VelorumDeedRules.beholding(litany) || VelorumDeedRules.repose(litany));
		int proper = SaintDeeds.of("PROPER", List.of("BEHOLDING", "LITANY", "REPOSE")).mask();
		assertFalse(VelorumDeedRules.beholding(proper) || VelorumDeedRules.litany(proper) || VelorumDeedRules.repose(proper));
		assertTrue(SaintDeeds.skipped(SaintDeeds.FORCED | 1 << ChapelRules.Kind.REPOSE.ordinal(), ChapelRules.Kind.REPOSE));
	}

	@Test
	void heNeverPausesUnderTheRepose() {
		assertEquals(18, VelorumDeedRules.recovery(18, false));
		assertEquals(13, VelorumDeedRules.recovery(18, true));
		assertEquals(10, VelorumDeedRules.recovery(12, true));
		assertEquals(0, VelorumDeedRules.recovery(0, true));
		assertEquals(16, VelorumDeedRules.decisionDelay(24, true));
		assertEquals(0, VelorumLocomotionRules.settle(true));
		assertEquals(8, VelorumLocomotionRules.settle(false));
	}

	@Test
	void theLitanyBringsOpeningsAFifthMoreOften() {
		assertEquals(240, VelorumDeedRules.longNightSpacing(true));
		assertEquals(300, VelorumDeedRules.longNightSpacing(false));
		assertEquals(384, VelorumDeedRules.openingDue(true));
		assertEquals(480, VelorumDeedRules.openingDue(false));
		assertTrue(VelorumDeedRules.longNightWeight(300, true) > VelorumDeedRules.longNightWeight(300, false));
		assertEquals(7, VelorumDeedRules.threads(true));
		assertEquals(5, VelorumDeedRules.threads(false));
	}

	@Test
	void eachDeedGainsALineForTheRevision() {
		// Beholding: a gaze reaches 10 in the dark, an Eclipse shape 5
		Vec3 face = new Vec3(0, 1.2, 0);
		assertFalse(VelorumDeedRules.inGaze(face, 0, 1, new Vec3(0, 0, 9), VelorumDeedRules.gazeLength(false)));
		assertTrue(VelorumDeedRules.inGaze(face, 0, 1, new Vec3(0, 0, 9), VelorumDeedRules.gazeLength(true)));
		assertEquals(5.0D, VelorumEclipseRules.reach(true));
		// Litany: the relight takes 30 fed ticks
		assertEquals(30, VelorumEclipseRules.relightTicks(true));
		assertEquals(20, VelorumEclipseRules.relightTicks(false));
		// Repose: no settle after the Shadow Step's rise either
		assertEquals(0, VelorumLocomotionRules.settle(true));
	}

	@Test
	void aFiguresGazeIsEightBlocksAndFiftyDegrees() {
		Vec3 face = new Vec3(0, 1.2, 0);
		assertTrue(VelorumDeedRules.inGaze(face, 0, 1, new Vec3(0, 0, 6)));
		assertTrue(VelorumDeedRules.inGaze(face, 0, 1, new Vec3(2, 0, 6)));
		assertFalse(VelorumDeedRules.inGaze(face, 0, 1, new Vec3(5, 0, 5)));
		assertFalse(VelorumDeedRules.inGaze(face, 0, 1, new Vec3(0, 0, 9)));
		assertFalse(VelorumDeedRules.inGaze(face, 0, 1, new Vec3(0, 0, -3)));
	}
}
