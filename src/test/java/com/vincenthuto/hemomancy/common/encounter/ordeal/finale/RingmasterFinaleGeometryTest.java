package com.vincenthuto.hemomancy.common.encounter.ordeal.finale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import com.vincenthuto.hemomancy.common.encounter.ordeal.OrdealHazardKinds;
import com.vincenthuto.hemomancy.common.encounter.ordeal.OrdealMoves;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

/** The Ringmaster's rafter moves hit what they mark and miss what reads them (2026-11-18). */
class RingmasterFinaleGeometryTest {
	private static final Vec3 FLOOR = new Vec3(0.5, 64.0, 0.5);

	@Test
	void kindsStayInTheRingmastersBlock() {
		for (int kind : new int[]{RingmasterFinaleMoves.KIND_VOLLEY, RingmasterFinaleMoves.KIND_LIGHTS,
				RingmasterFinaleMoves.KIND_SPOTLIGHT, RingmasterFinaleMoves.KIND_RUSH, CarouselAlive.KIND_HORSE,
				CarouselAlive.KIND_SPIRAL, CarouselAlive.KIND_SLAM})
			assertTrue(kind >= OrdealHazardKinds.RINGMASTER && kind < OrdealHazardKinds.RINGMASTER + 10, "kind " + kind);
	}

	@Test
	void horseLanesAre120DegreesApart() {
		for (float yaw : new float[]{0.0F, 37.0F, -150.0F}) {
			List<Vec3> ends = RingmasterFinaleGeometry.horseLanes(FLOOR, yaw);
			assertEquals(3, ends.size());
			for (int i = 0; i < 3; i++) {
				Vec3 a = ends.get(i).subtract(FLOOR), b = ends.get((i + 1) % 3).subtract(FLOOR);
				assertEquals(RingmasterFinaleGeometry.HORSE_LANE, Math.hypot(a.x, a.z), 1.0E-6, "each lane runs its length");
				double cos = (a.x * b.x + a.z * b.z) / (Math.hypot(a.x, a.z) * Math.hypot(b.x, b.z));
				assertEquals(120.0D, Math.toDegrees(Math.acos(cos)), 1.0E-4, "lanes " + i + " and " + (i + 1));
			}
		}
	}

	@Test
	void spiralGrowsFrom2_4To9In40Ticks() {
		assertEquals(2.4D, RingmasterFinaleGeometry.spiralRadius(0), 1.0E-9);
		assertEquals(9.0D, RingmasterFinaleGeometry.spiralRadius(40), 1.0E-9);
		for (int t = 1; t <= 40; t++)
			assertTrue(RingmasterFinaleGeometry.spiralRadius(t) > RingmasterFinaleGeometry.spiralRadius(t - 1), "tick " + t);
		assertEquals(9.0D, RingmasterFinaleGeometry.spiralRadius(60), 1.0E-9, "it stops at the curb");
	}

	@Test
	void ringHitsTheBandAndIsJumpable() {
		double radius = 5.0D, band = 1.0D;
		Vec3 onBand = FLOOR.add(radius + 0.3D, 0.0D, 0.0D);
		assertTrue(RingmasterFinaleGeometry.ringHits(FLOOR, radius, band, onBand, OrdealMoves.CLEARANCE));
		assertFalse(RingmasterFinaleGeometry.ringHits(FLOOR, radius, band, FLOOR.add(radius + 0.8D, 0.0D, 0.0D),
				OrdealMoves.CLEARANCE), "outside the band");
		assertFalse(RingmasterFinaleGeometry.ringHits(FLOOR, radius, band, FLOOR.add(radius - 1.5D, 0.0D, 0.0D),
				OrdealMoves.CLEARANCE), "inside the band");
		assertFalse(RingmasterFinaleGeometry.ringHits(FLOOR, radius, band, onBand.add(0.0D, 1.2D, 0.0D),
				OrdealMoves.CLEARANCE), "a jump clears it");
	}

	@Test
	void laneAndCurveHits() {
		Vec3 from = FLOOR, to = FLOOR.add(14.0D, 0.0D, 0.0D);
		double half = RingmasterFinaleGeometry.VOLLEY_HALF;
		assertTrue(RingmasterFinaleGeometry.laneHits(from, to, half, FLOOR.add(7.0D, 0.0D, half - 0.05D)));
		assertFalse(RingmasterFinaleGeometry.laneHits(from, to, half, FLOOR.add(7.0D, 0.0D, half + 0.05D)));
		assertFalse(RingmasterFinaleGeometry.laneHits(from, to, half, FLOOR.add(14.5D, 0.0D, 0.0D)), "past its end");
		assertFalse(RingmasterFinaleGeometry.laneHits(from, to, half, FLOOR.add(-0.5D, 0.0D, 0.0D)), "behind its start");

		List<Vec3> curve = RingmasterFinaleGeometry.curve(FLOOR, FLOOR.add(10.0D, 0.0D, 0.0D), 3.0D, 10);
		assertEquals(11, curve.size());
		Vec3 bend = curve.get(5);
		assertTrue(Math.abs(bend.z - FLOOR.z) > 1.0D, "the string bends");
		double lights = RingmasterFinaleMoves.LIGHTS_HALF;
		assertTrue(RingmasterFinaleGeometry.curveHits(curve, lights, bend.add(0.0D, 0.0D, lights - 0.05D)));
		assertFalse(RingmasterFinaleGeometry.curveHits(curve, lights, bend.add(0.0D, 0.0D, lights + 0.6D)));
		assertFalse(RingmasterFinaleGeometry.curveHits(curve, lights, FLOOR.add(5.0D, 0.0D, 0.0D)),
				"the straight line under a bent string is off it");
	}

	@Test
	void spiralHorseRunsOutAlongItsRadius() {
		Vec3 start = RingmasterFinaleGeometry.spiralPoint(FLOOR, 0.0F, 0, 0);
		Vec3 end = RingmasterFinaleGeometry.spiralPoint(FLOOR, 0.0F, 0, 40);
		assertEquals(2.4D, Math.hypot(start.x - FLOOR.x, start.z - FLOOR.z), 1.0E-6);
		assertEquals(9.0D, Math.hypot(end.x - FLOOR.x, end.z - FLOOR.z), 1.0E-6);
	}

	@Test
	void beatQuickensAsHisHealthFalls() {
		assertEquals(70, RingmasterFinaleMoves.beat(1.0F));
		assertEquals(45, RingmasterFinaleMoves.beat(0.5F));
		assertTrue(RingmasterFinaleMoves.beat(0.75F) < 70 && RingmasterFinaleMoves.beat(0.75F) > 45);
	}

	@Test
	void arcHitsTheBandWithinTheSweptAngle() {
		double radius = 3.5D, band = 1.2D;
		// 30 degrees round from +x, on the band.
		Vec3 inSweep = FLOOR.add(Math.cos(Math.toRadians(30.0D)) * radius, 0.0D, Math.sin(Math.toRadians(30.0D)) * radius);
		assertTrue(RingmasterFinaleGeometry.arcHits(FLOOR, radius, band, 0.0F, 90.0F, inSweep));
		assertFalse(RingmasterFinaleGeometry.arcHits(FLOOR, radius, band, 45.0F, 90.0F, inSweep), "not yet swept");
		assertFalse(RingmasterFinaleGeometry.arcHits(FLOOR, radius + 1.0D, band, 0.0F, 90.0F, inSweep), "outside the band");
		assertFalse(RingmasterFinaleGeometry.arcHits(FLOOR, radius, band, 0.0F, 90.0F, inSweep.add(0.0D, 2.6D, 0.0D)), "above it");
		// A sweep across the wrap at 360: from 300 round to 400 (40) covers 350 and 20.
		Vec3 at350 = FLOOR.add(Math.cos(Math.toRadians(350.0D)) * radius, 0.0D, Math.sin(Math.toRadians(350.0D)) * radius);
		assertTrue(RingmasterFinaleGeometry.arcHits(FLOOR, radius, band, 300.0F, 400.0F, at350));
		assertFalse(RingmasterFinaleGeometry.arcHits(FLOOR, radius, band, 300.0F, 330.0F, at350));
	}

	@Test
	void staffWedgeHitsInFrontOfHimOnly() {
		Vec3 facing = new Vec3(0.0D, 0.0D, 1.0D);
		assertTrue(RingmasterFinaleGeometry.wedgeHits(FLOOR, facing, 3.2D, 100.0D, FLOOR.add(0.5D, 0.0D, 2.5D)));
		assertFalse(RingmasterFinaleGeometry.wedgeHits(FLOOR, facing, 3.2D, 100.0D, FLOOR.add(0.0D, 0.0D, 3.6D)), "past its reach");
		assertFalse(RingmasterFinaleGeometry.wedgeHits(FLOOR, facing, 3.2D, 100.0D, FLOOR.add(2.5D, 0.0D, 0.5D)), "beside him");
		assertFalse(RingmasterFinaleGeometry.wedgeHits(FLOOR, facing, 3.2D, 100.0D, FLOOR.add(0.0D, 0.0D, -2.0D)), "behind him");
	}
}
