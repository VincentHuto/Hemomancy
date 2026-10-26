package com.vincenthuto.hemomancy.common.entity.boss.endgame;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class MycophantStrikeRulesTest {
	/** An offset {@code distance} blocks out at {@code angleDeg} from facing {@code yawDeg} (Minecraft yaw: 0 faces +z). */
	private static double[] at(float yawDeg, double angleDeg, double distance) {
		double rad = Math.toRadians(yawDeg + angleDeg);
		return new double[] { -Math.sin(rad) * distance, Math.cos(rad) * distance };
	}

	@Test
	void coneEdges() {
		float yaw = 37.0F;
		double[] edge = at(yaw, 40.0, 6.0);
		assertTrue(MycophantStrikeRules.inCone(edge[0], edge[1], yaw, 6.0, 40.0));
		double[] wide = at(yaw, 40.5, 5.0);
		assertFalse(MycophantStrikeRules.inCone(wide[0], wide[1], yaw, 6.0, 40.0));
		double[] far = at(yaw, 0.0, 6.05);
		assertFalse(MycophantStrikeRules.inCone(far[0], far[1], yaw, 6.0, 40.0));
		double[] otherSide = at(yaw, -39.0, 3.0);
		assertTrue(MycophantStrikeRules.inCone(otherSide[0], otherSide[1], yaw, 6.0, 40.0));
	}

	@Test
	void rearArcEdges() {
		float yaw = -120.0F;
		double[] behind = at(yaw, 180.0, 6.0);
		assertTrue(MycophantStrikeRules.inRearArc(behind[0], behind[1], yaw, 6.0, 150.0));
		double[] edge = at(yaw, 105.0, 6.0);
		assertTrue(MycophantStrikeRules.inRearArc(edge[0], edge[1], yaw, 6.0, 150.0));
		double[] pastEdge = at(yaw, 104.5, 4.0);
		assertFalse(MycophantStrikeRules.inRearArc(pastEdge[0], pastEdge[1], yaw, 6.0, 150.0));
		double[] tooFar = at(yaw, 180.0, 6.05);
		assertFalse(MycophantStrikeRules.inRearArc(tooFar[0], tooFar[1], yaw, 6.0, 150.0));
		double[] ahead = at(yaw, 0.0, 3.0);
		assertFalse(MycophantStrikeRules.inRearArc(ahead[0], ahead[1], yaw, 6.0, 150.0));
	}

	@Test
	void circleEdges() {
		assertTrue(MycophantStrikeRules.inCircle(3.0, 0.0, 3.0));
		assertTrue(MycophantStrikeRules.inCircle(0.0, -3.0, 3.0));
		assertFalse(MycophantStrikeRules.inCircle(3.01, 0.0, 3.0));
		assertFalse(MycophantStrikeRules.inCircle(2.2, 2.2, 3.0));
	}

	@Test
	void sweepCoversFourToNineBlocksAcrossTheFront() {
		float yaw = 90.0F;
		double[] near = at(yaw, 0.0, 3.9);
		assertFalse(MycophantStrikeRules.inSweep(near[0], near[1], yaw));
		double[] inner = at(yaw, 0.0, 4.0);
		assertTrue(MycophantStrikeRules.inSweep(inner[0], inner[1], yaw));
		double[] outer = at(yaw, 0.0, 9.0);
		assertTrue(MycophantStrikeRules.inSweep(outer[0], outer[1], yaw));
		double[] far = at(yaw, 0.0, 9.1);
		assertFalse(MycophantStrikeRules.inSweep(far[0], far[1], yaw));
		double[] edge = at(yaw, 60.0, 6.0);
		assertTrue(MycophantStrikeRules.inSweep(edge[0], edge[1], yaw));
		double[] flank = at(yaw, 61.0, 6.0);
		assertFalse(MycophantStrikeRules.inSweep(flank[0], flank[1], yaw));
	}

	@Test
	void aJumpClearsTheSweep() {
		assertTrue(MycophantStrikeRules.clearsSweep(0.8));
		assertTrue(MycophantStrikeRules.clearsSweep(1.2));
		assertFalse(MycophantStrikeRules.clearsSweep(0.79));
		assertFalse(MycophantStrikeRules.clearsSweep(0.0));
	}

	@Test
	void stringTimelines() {
		assertArrayEquals(new int[] { 0, 8 }, MycophantStrikeRules.stringHits(0, 1));
		assertArrayEquals(new int[] { 0, 8 }, MycophantStrikeRules.stringHits(0, 2));
		assertArrayEquals(new int[] { 0, 8, 20 }, MycophantStrikeRules.stringHits(1, 1));
		assertArrayEquals(new int[] { 0, 7, 18 }, MycophantStrikeRules.stringHits(1, 2));
		assertArrayEquals(new int[] { 0, 7, 14, 26 }, MycophantStrikeRules.stringHits(2, 2));
		// the returned timeline is a copy
		MycophantStrikeRules.stringHits(1, 1)[2] = 99;
		assertEquals(20, MycophantStrikeRules.stringHits(1, 1)[2]);
	}

	@Test
	void onlyTheLastHitOfALongStringIsTheSlam() {
		assertFalse(MycophantStrikeRules.isSlam(0, 1, 2));
		assertFalse(MycophantStrikeRules.isSlam(1, 1, 3));
		assertTrue(MycophantStrikeRules.isSlam(1, 2, 3));
		assertFalse(MycophantStrikeRules.isSlam(2, 2, 4));
		assertTrue(MycophantStrikeRules.isSlam(2, 3, 4));
	}

	@Test
	void tailAndGlobTimelines() {
		assertArrayEquals(new int[] { 0 }, MycophantStrikeRules.tailHits(0));
		assertArrayEquals(new int[] { 0, 10 }, MycophantStrikeRules.tailHits(1));
		assertArrayEquals(new int[] { 0, 4, 8 }, MycophantStrikeRules.globTicks(1));
		assertArrayEquals(new int[] { 0, 3, 6, 9, 12 }, MycophantStrikeRules.globTicks(2));
	}

	@Test
	void spreadKeepsTheTargetAndRingsTheRest() {
		for (long seed = 0; seed < 50; seed++) {
			double[][] points = MycophantStrikeRules.spread(12.5, -4.0, 5, seed, 2.0, 4.5);
			assertEquals(5, points.length);
			assertEquals(12.5, points[0][0], 1.0E-9);
			assertEquals(-4.0, points[0][1], 1.0E-9);
			for (int i = 1; i < points.length; i++) {
				double r = Math.hypot(points[i][0] - 12.5, points[i][1] + 4.0);
				assertTrue(r >= 2.0 - 1.0E-9 && r <= 4.5 + 1.0E-9, "seed " + seed + " point " + i + " at " + r);
			}
		}
		assertArrayEquals(MycophantStrikeRules.spread(0, 0, 3, 7L, 1, 2)[2], MycophantStrikeRules.spread(0, 0, 3, 7L, 1, 2)[2]);
		assertEquals(1, MycophantStrikeRules.spread(3, 3, 1, 1L, 1, 2).length);
	}

	@Test
	void mortarFlight() {
		assertEquals(14, MycophantStrikeRules.mortarFlightTicks(1.0));
		assertEquals(14, MycophantStrikeRules.mortarFlightTicks(0.0));
		assertEquals(30, MycophantStrikeRules.mortarFlightTicks(20.0));
	}

	@Test
	void aLobLandsOnItsOffset() {
		double gravity = 0.05;
		double[][] offsets = { { 10.0, -1.5, 4.0 }, { -3.0, 2.0, 18.0 }, { 0.0, 0.0, 0.0 } };
		for (double[] offset : offsets) {
			int ticks = MycophantStrikeRules.mortarFlightTicks(Math.hypot(offset[0], offset[2]));
			double[] v = MycophantStrikeRules.lobVelocity(offset[0], offset[1], offset[2], ticks, gravity);
			double x = 0, y = 0, z = 0, vy = v[1];
			for (int t = 0; t < ticks; t++) {
				x += v[0];
				y += vy;
				z += v[2];
				vy -= gravity;
			}
			assertEquals(offset[0], x, 0.01);
			assertEquals(offset[1], y, 0.01);
			assertEquals(offset[2], z, 0.01);
		}
	}

	@Test
	void lanceLineRunsTowardTheTarget() {
		double[][] line = MycophantStrikeRules.lanceLine(0, 0, 0, 10, 12, 1);
		assertEquals(12, line.length);
		for (int i = 0; i < line.length; i++) {
			assertEquals(0.0, line[i][0], 1.0E-9);
			assertEquals(i + 1.0, line[i][1], 1.0E-9);
		}
		double[][] diagonal = MycophantStrikeRules.lanceLine(2, 2, 5, 6, 10, 2.5);
		assertEquals(4, diagonal.length);
		assertEquals(2 + 0.6 * 10, diagonal[3][0], 1.0E-9);
		assertEquals(2 + 0.8 * 10, diagonal[3][1], 1.0E-9);
		assertEquals(0, MycophantStrikeRules.lanceDelay(0));
		assertEquals(9, MycophantStrikeRules.lanceDelay(3));
		assertEquals(10, MycophantStrikeRules.LANCE_WARN_TICKS);
	}

	@Test
	void gazeSweepsAHundredDegrees() {
		assertEquals(-20.0F, MycophantStrikeRules.gazeYaw(30.0F, 1, 0.0F), 1.0E-4F);
		assertEquals(30.0F, MycophantStrikeRules.gazeYaw(30.0F, 1, 20.0F), 1.0E-4F);
		assertEquals(80.0F, MycophantStrikeRules.gazeYaw(30.0F, 1, 40.0F), 1.0E-4F);
		assertEquals(80.0F, MycophantStrikeRules.gazeYaw(30.0F, 1, 55.0F), 1.0E-4F);
		assertEquals(-20.0F, MycophantStrikeRules.gazeYaw(30.0F, 1, -5.0F), 1.0E-4F);
		assertEquals(80.0F, MycophantStrikeRules.gazeYaw(30.0F, -1, 0.0F), 1.0E-4F);
		assertEquals(-20.0F, MycophantStrikeRules.gazeYaw(30.0F, -1, 40.0F), 1.0E-4F);
	}

	@Test
	void gazeHitsWithinSixDegreesOnly() {
		float beam = 178.0F; // the edge cases cross the +-180 seam
		double[] on = at(beam, 0.0, 12.0);
		assertTrue(MycophantStrikeRules.gazeHits(beam, on[0], on[1], 24.0));
		double[] edge = at(beam, 6.0, 12.0);
		assertTrue(MycophantStrikeRules.gazeHits(beam, edge[0], edge[1], 24.0));
		double[] off = at(beam, 6.5, 12.0);
		assertFalse(MycophantStrikeRules.gazeHits(beam, off[0], off[1], 24.0));
		double[] wrapped = at(beam, -5.0, 12.0);
		assertTrue(MycophantStrikeRules.gazeHits(beam, wrapped[0], wrapped[1], 24.0));
		double[] far = at(beam, 0.0, 24.5);
		assertFalse(MycophantStrikeRules.gazeHits(beam, far[0], far[1], 24.0));
		assertEquals(0.9, MycophantStrikeRules.gazeTargetHeight(1.8), 1.0E-9);
	}

	@Test
	void onlySurvivalPlayersAreTaken() {
		assertTrue(MycophantStrikeRules.onlySurvival(false, false));
		assertFalse(MycophantStrikeRules.onlySurvival(true, false));
		assertFalse(MycophantStrikeRules.onlySurvival(false, true));
		assertFalse(MycophantStrikeRules.onlySurvival(true, true));
	}

	@Test
	void bearingMatchesMinecraftYaw() {
		assertEquals(0.0F, MycophantStrikeRules.bearing(0.0, 5.0), 1.0E-4F);
		assertEquals(90.0F, Math.abs(MycophantStrikeRules.bearing(-5.0, 0.0)), 1.0E-4F);
		double[] p = at(-73.0F, 0.0, 8.0);
		assertEquals(0.0, MycophantStrikeRules.angleFromFacing(p[0], p[1], MycophantStrikeRules.bearing(p[0], p[1])), 1.0E-4);
	}
}
