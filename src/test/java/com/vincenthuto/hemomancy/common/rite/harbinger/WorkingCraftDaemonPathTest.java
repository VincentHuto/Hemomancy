package com.vincenthuto.hemomancy.common.rite.harbinger;

import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

final class WorkingCraftDaemonPathTest {
	private static final Vec3 HOVER = new Vec3(0.5D, 66.0D, 0.5D);
	private static final List<Vec3> STOPS = List.of(new Vec3(3.5D, 65.6D, 0.5D), new Vec3(-2.5D, 65.6D, 0.5D),
			new Vec3(0.5D, 65.6D, -3.5D));

	@Test
	void theDaemonReachesEachBrazierAsItsOfferingIsAbsorbed() {
		for (int i = 0; i < STOPS.size(); i++) {
			int draw = WorkingCraftDaemonPath.drawTick(i);
			assertTrue(WorkingCraftDaemonPath.processionPosition(draw, HOVER, STOPS).distanceTo(STOPS.get(i)) < 0.05D,
					"at brazier " + i + " when it absorbs");
			assertEquals(i, WorkingCraftDaemonPath.drawsDue(draw - 1, STOPS.size()));
			assertEquals(i + 1, WorkingCraftDaemonPath.drawsDue(draw, STOPS.size()));
		}
	}

	@Test
	void itSetsOutFromTheStaffAndComesBack() {
		assertTrue(WorkingCraftDaemonPath.processionPosition(0, HOVER, STOPS).distanceTo(HOVER) < 0.05D);
		int end = WorkingCraftDaemonPath.processionTicks(STOPS.size());
		assertFalse(WorkingCraftDaemonPath.processionFinished(end - 1, STOPS.size()));
		assertTrue(WorkingCraftDaemonPath.processionFinished(end, STOPS.size()));
		assertTrue(WorkingCraftDaemonPath.processionPosition(end, HOVER, STOPS).distanceTo(HOVER) < 0.05D);
	}

	@Test
	void itFliesInArcsRatherThanSkimmingTheFloor() {
		Vec3 mid = WorkingCraftDaemonPath.processionPosition(WorkingCraftDaemonPath.FLY_TICKS / 2, HOVER, STOPS);
		assertTrue(mid.y > (HOVER.y + STOPS.getFirst().y) / 2.0D + 0.3D, "arcs up between stops: " + mid.y);
	}

	@Test
	void thePathNeverJumps() {
		int end = WorkingCraftDaemonPath.processionTicks(STOPS.size());
		Vec3 previous = WorkingCraftDaemonPath.processionPosition(0, HOVER, STOPS);
		for (int tick = 1; tick <= end + 5; tick++) {
			Vec3 next = WorkingCraftDaemonPath.processionPosition(tick, HOVER, STOPS);
			assertTrue(next.distanceTo(previous) < 1.2D, "jump at tick " + tick);
			previous = next;
		}
	}

	@Test
	void itDivesIntoTheStructureAndStaysThere() {
		Vec3 structure = new Vec3(0.5D, 66.5D, 3.0D);
		int dive = WorkingCraftDaemonPath.DIVE_TICKS;
		assertTrue(WorkingCraftDaemonPath.divePosition(0, HOVER, structure).distanceTo(HOVER) < 0.05D);
		assertTrue(WorkingCraftDaemonPath.divePosition(dive, HOVER, structure).distanceTo(structure) < 0.05D);
		assertTrue(WorkingCraftDaemonPath.divePosition(dive + 40, HOVER, structure).distanceTo(structure) < 0.05D);
		assertEquals(0.5F, WorkingCraftDaemonPath.diveScale(0, 0.5F, 0.1F), 1.0E-6F);
		assertEquals(0.1F, WorkingCraftDaemonPath.diveScale(dive, 0.5F, 0.1F), 1.0E-6F, "shrinks into the structure");
		assertTrue(dive < CardinalRiteFinaleTiming.TOTAL_TICKS - 20, "the structure has time to darken before it is taken");
	}

	@Test
	void aCraftWithoutOfferingsHasNoProcession() {
		assertTrue(WorkingCraftDaemonPath.processionFinished(0, 0));
		assertEquals(0, WorkingCraftDaemonPath.drawsDue(100, 0));
	}
}
