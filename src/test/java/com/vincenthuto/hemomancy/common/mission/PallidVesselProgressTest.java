package com.vincenthuto.hemomancy.common.mission;

import com.vincenthuto.hemomancy.common.mission.alchemist.PallidVesselProgress;
import com.vincenthuto.hemomancy.common.mission.alchemist.PallidVesselProgress.Stage;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PallidVesselProgressTest {
	@Test
	void stepsRunBriefingRiteFillReport() {
		PallidVesselProgress progress = PallidVesselProgress.NONE;
		assertEquals(Stage.NOT_BRIEFED, progress.stage());
		progress = progress.withBriefed();
		assertEquals(Stage.NEEDS_RITE, progress.stage());
		progress = progress.withRite();
		assertEquals(Stage.NEEDS_FILL, progress.stage());
		progress = progress.withFill();
		assertEquals(Stage.READY, progress.stage());
		assertTrue(progress.canReport());
		progress = progress.withReport();
		assertEquals(Stage.COMPLETE, progress.stage());
		assertEquals(PallidVesselProgress.STEPS, progress.completedSteps());
	}

	@Test
	void fillNeedsThePlayersOwnRiteFirst() {
		PallidVesselProgress briefed = PallidVesselProgress.NONE.withBriefed();
		assertFalse(briefed.acceptsFill());
		assertSame(briefed, briefed.withFill(), "a gourd filled before this player's rite proves nothing");
	}

	@Test
	void riteAndFillMayComeBeforeTheBriefingButTheReportCannot() {
		PallidVesselProgress early = PallidVesselProgress.NONE.withRite().withFill();
		assertTrue(early.riteDone() && early.filled());
		assertEquals(Stage.NOT_BRIEFED, early.stage());
		assertFalse(early.canReport());
		assertSame(early, early.withReport());
		assertEquals(Stage.READY, early.withBriefed().stage());
	}

	@Test
	void reportNeedsEveryStepAndHappensOnce() {
		assertFalse(PallidVesselProgress.NONE.withBriefed().withRite().canReport());
		PallidVesselProgress done = PallidVesselProgress.NONE.withBriefed().withRite().withFill().withReport();
		assertFalse(done.canReport());
		assertFalse(done.acceptsFill());
		assertSame(done, done.withReport());
	}
}
