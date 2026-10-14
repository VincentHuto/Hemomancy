package com.vincenthuto.hemomancy.common.entity.utility;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

final class DaemonFlightTest {
	private static double dot(double[] a, double x, double y, double z) {
		double length = Math.sqrt(x * x + y * y + z * z);
		return (a[0] * x + a[1] * y + a[2] * z) / length;
	}

	private static double angleBetween(double[] a, double[] b) {
		return Math.acos(Math.max(-1.0D, Math.min(1.0D, a[0] * b[0] + a[1] * b[1] + a[2] * b[2])));
	}

	@Test
	void hoveringItStandsUprightWithItsSmokeAtRest() {
		DaemonFlight flight = new DaemonFlight();
		for (int tick = 0; tick < 40; tick++) flight.tick(0.0D, 0.0D, 0.0D, 0.0D, 1.4D);
		assertEquals(1.0D, flight.crown(1.0F)[1], 1e-9, "its crown points straight up");
		assertEquals(0.0D, flight.stream(1.0F), 1e-9, "no tail streams out behind it");
	}

	@Test
	void flyingItLiesAlongItsPathHeadFirstAndStreams() {
		DaemonFlight flight = new DaemonFlight();
		for (int tick = 0; tick < 60; tick++) flight.tick(-0.2D, 0.0D, 0.0D, 0.0D, 1.4D);
		assertTrue(dot(flight.crown(1.0F), -1.0D, 0.0D, 0.0D) > 0.98, "the crown leads along the path");
		assertTrue(flight.stream(1.0F) > 0.95, "at speed the tail streams out fully");
	}

	@Test
	void divingItPointsDownTheDiveNotFlatLikeAFloatingLog() {
		DaemonFlight flight = new DaemonFlight();
		for (int tick = 0; tick < 60; tick++) flight.tick(-0.15D, -0.1D, 0.0D, 0.0D, 1.4D);
		double[] crown = flight.crown(1.0F);
		assertTrue(crown[1] < -0.4, "head first down the slope, not lying level: " + crown[1]);
		assertTrue(dot(crown, -0.15D, -0.1D, 0.0D) > 0.98);
	}

	@Test
	void straightDownItTipsOverTowardWhereItFaces() {
		DaemonFlight flight = new DaemonFlight();
		double yaw = Math.toRadians(90.0D); // facing -x
		flight.tick(0.0D, -0.004D, 0.0D, yaw, 1.4D);
		for (int tick = 0; tick < 6; tick++) flight.tick(0.0D, -0.05D, 0.0D, yaw, 1.4D);
		double[] crown = flight.crown(1.0F);
		assertTrue(crown[0] < -0.1, "it swings over through its front, not sideways: " + crown[0]);
		for (int tick = 0; tick < 80; tick++) flight.tick(0.0D, -0.2D, 0.0D, yaw, 1.4D);
		assertTrue(flight.crown(1.0F)[1] < -0.95, "and ends diving head first");
	}

	@Test
	void theBodySwingsOverEasedNeverSnapping() {
		DaemonFlight flight = new DaemonFlight();
		double[] previous = flight.crown(1.0F);
		int swinging = 0;
		for (int tick = 0; tick < 60; tick++) {
			flight.tick(0.25D, 0.0D, 0.0D, 0.0D, 1.4D);
			double[] crown = flight.crown(1.0F);
			double turned = angleBetween(previous, crown);
			assertTrue(turned < Math.toRadians(16.0D), "tick " + tick + " turned " + Math.toDegrees(turned));
			if (turned > Math.toRadians(1.0D)) swinging++;
			previous = crown;
		}
		assertTrue(swinging > 8, "the swing takes its time: " + swinging + " ticks");
	}

	@Test
	void reversingItTurnsOverRatherThanFreezing() {
		DaemonFlight flight = new DaemonFlight();
		for (int tick = 0; tick < 60; tick++) flight.tick(-0.2D, 0.0D, 0.0D, 0.0D, 1.4D);
		for (int tick = 0; tick < 80; tick++) flight.tick(0.2D, 0.0D, 0.0D, 0.0D, 1.4D);
		assertTrue(dot(flight.crown(1.0F), 1.0D, 0.0D, 0.0D) > 0.98, "it comes round to lead the other way");
	}

	@Test
	void betweenTicksTheCrownIsInterpolated() {
		DaemonFlight flight = new DaemonFlight();
		for (int tick = 0; tick < 4; tick++) flight.tick(-0.2D, 0.0D, 0.0D, 0.0D, 1.4D);
		double[] before = flight.crown(0.0F);
		double[] after = flight.crown(1.0F);
		double[] half = flight.crown(0.5F);
		assertEquals(1.0D, Math.sqrt(half[0] * half[0] + half[1] * half[1] + half[2] * half[2]), 1e-9);
		assertEquals(angleBetween(before, after), angleBetween(before, half) + angleBetween(half, after), 1e-6);
		assertTrue(angleBetween(before, half) > 0.0D && angleBetween(half, after) > 0.0D);
	}

	@Test
	void itsFlowCountsTheDistanceTravelledInHeights() {
		DaemonFlight flight = new DaemonFlight();
		for (int tick = 0; tick < 100; tick++) flight.tick(0.0D, 0.0D, 0.0D, 0.0D, 2.0D);
		assertEquals(0.0D, flight.flow(1.0F), 1e-9, "at rest the smoke does not stream");
		for (int tick = 0; tick < 100; tick++) flight.tick(0.2D, 0.0D, 0.0D, 0.0D, 2.0D);
		assertEquals(10.0D, flight.flow(1.0F), 0.5D, "twenty blocks at a two-block height: ten heights");
		double mid = flight.flow(0.5F);
		assertTrue(mid > flight.flow(0.0F) && mid < flight.flow(1.0F));
	}

	@Test
	void itsSmoothedVelocityFollowsTheMotion() {
		DaemonFlight flight = new DaemonFlight();
		flight.tick(0.3D, 0.0D, 0.0D, 0.0D, 1.4D);
		assertTrue(flight.velocity()[0] > 0.0D && flight.velocity()[0] < 0.3D, "a sudden move is eased in");
		for (int tick = 0; tick < 60; tick++) flight.tick(0.3D, 0.0D, 0.0D, 0.0D, 1.4D);
		assertEquals(0.3D, flight.velocity()[0], 1e-3);
	}
}
