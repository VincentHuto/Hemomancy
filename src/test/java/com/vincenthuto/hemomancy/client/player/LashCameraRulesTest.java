package com.vincenthuto.hemomancy.client.player;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** The view's share of the mouse while a lash is out: capped, never banked, and coasting to a stop. */
class LashCameraRulesTest {
	private static final double CAP = 4.0D, DRAG = 0.6D;

	private static double[] view(LashCameraRules.State state, double yaw, double pitch, double dt, double strength) {
		return LashCameraRules.view(state, yaw, pitch, dt, CAP, DRAG, strength);
	}

	@Test
	void theWhipAimMovesNoFurtherInATickThanTheServerAllows() {
		LashCameraRules.Lead lead = new LashCameraRules.Lead();
		lead.set(-70.0D, 0.0D);
		LashCameraRules.lead(lead, 200.0D, 0.0D, 1.0D, 70.0D, 0.88D);
		LashCameraRules.limitStep(lead, -70.0D, 0.0D, 4.0D, 0.0D, 60.0D);
		assertEquals(60.0D, 4.0D + lead.yaw() + 70.0D, 1.0E-9D, "the aim moved past the server's 60 degrees a tick");
		LashCameraRules.Lead small = new LashCameraRules.Lead();
		small.set(10.0D, 5.0D);
		LashCameraRules.limitStep(small, 0.0D, 0.0D, 2.0D, 0.0D, 60.0D);
		assertEquals(10.0D, small.yaw(), 1.0E-9D, "a move inside the limit was changed");
		assertEquals(5.0D, small.pitch(), 1.0E-9D);
	}

	@Test
	void zeroStrengthIsTheRawTurn() {
		LashCameraRules.State state = new LashCameraRules.State();
		double[] turned = view(state, 40.0D, -12.0D, 1.0D, 0.0D);
		assertEquals(40.0D, turned[0], 1.0E-9D);
		assertEquals(-12.0D, turned[1], 1.0E-9D);
		assertEquals(0.0D, view(state, 0.0D, 0.0D, 1.0D, 0.0D)[0], 1.0E-9D, "a view with the damping off coasted");
	}

	@Test
	void aTurnUnderTheCapPassesWhole() {
		assertEquals(3.0D, view(new LashCameraRules.State(), 3.0D, 0.0D, 1.0D, 1.0D)[0], 1.0E-9D);
	}

	@Test
	void aHardFlickIsCappedAndKeepsItsDirection() {
		double[] turned = view(new LashCameraRules.State(), -40.0D, 0.0D, 1.0D, 1.0D);
		assertEquals(-4.0D, turned[0], 1.0E-9D);
		double[] diagonal = view(new LashCameraRules.State(), 30.0D, 40.0D, 1.0D, 1.0D);
		assertEquals(2.4D, diagonal[0], 1.0E-9D);
		assertEquals(3.2D, diagonal[1], 1.0E-9D);
	}

	@Test
	void theExcessIsNotBankedAndTheViewCoastsToRest() {
		LashCameraRules.State state = new LashCameraRules.State();
		view(state, 40.0D, 0.0D, 1.0D, 1.0D);
		double first = view(state, 0.0D, 0.0D, 1.0D, 1.0D)[0];
		assertEquals(true, first > 0.0D && first < CAP, "the view stopped dead, or coasted faster than the cap: " + first);
		double total = first;
		for (int i = 0; i < 60; i++) total += view(state, 0.0D, 0.0D, 1.0D, 1.0D)[0];
		assertEquals(0.0D, view(state, 0.0D, 0.0D, 1.0D, 1.0D)[0], 1.0E-9D, "the view never came to rest");
		// A rate of 4 losing 40% a tick can cover at most 4 / -ln(0.6), about 7.8 degrees: nothing like the 36 the cap cut.
		assertEquals(true, total < CAP / -Math.log(DRAG), "the coast paid back the capped turn: " + total);
	}

	@Test
	void theViewWaitsAMomentBeforeItCoasts() {
		LashCameraRules.State state = new LashCameraRules.State();
		view(state, 40.0D, 0.0D, 1.0D, 1.0D);
		assertEquals(0.0D, view(state, 0.0D, 0.0D, LashCameraRules.COAST_GAP * 0.5D, 1.0D)[0], 1.0E-9D,
				"a frame between two mouse reports was taken for a hand at rest");
		assertEquals(true, view(state, 0.0D, 0.0D, 1.0D, 1.0D)[0] > 0.0D, "the view did not coast once the hand had stopped");
	}

	@Test
	void slowAimingAfterAFlickDoesNotCoast() {
		LashCameraRules.State state = new LashCameraRules.State();
		view(state, 40.0D, 0.0D, 1.0D, 1.0D);
		for (int i = 0; i < 40; i++) assertEquals(1.0D, view(state, 1.0D, 0.0D, 1.0D, 1.0D)[0], 1.0E-9D);
		double after = 0.0D;
		for (int i = 0; i < 10; i++) after += view(state, 0.0D, 0.0D, 1.0D, 1.0D)[0];
		assertEquals(0.0D, after, 1.0E-9D, "the flick's momentum outlived two seconds of ordinary aiming");
	}

	@Test
	void halfStrengthBlends() {
		assertEquals(22.0D, view(new LashCameraRules.State(), 40.0D, 0.0D, 1.0D, 0.5D)[0], 1.0E-9D);
	}

	@Test
	void frameRateDoesNotChangeTheCap() {
		LashCameraRules.State state = new LashCameraRules.State();
		double total = 0.0D;
		for (int frame = 0; frame < 4; frame++) total += view(state, 10.0D, 0.0D, 0.25D, 1.0D)[0];
		assertEquals(4.0D, total, 1.0E-9D);
	}

	@Test
	void frameRateDoesNotChangeTheCoast() {
		LashCameraRules.State whole = new LashCameraRules.State(), quarters = new LashCameraRules.State();
		view(whole, 40.0D, 0.0D, 1.0D, 1.0D);
		view(quarters, 40.0D, 0.0D, 1.0D, 1.0D);
		double byTicks = 0.0D, byQuarters = 0.0D;
		for (int tick = 0; tick < 3; tick++) byTicks += view(whole, 0.0D, 0.0D, 1.0D, 1.0D)[0];
		for (int frame = 0; frame < 12; frame++) byQuarters += view(quarters, 0.0D, 0.0D, 0.25D, 1.0D)[0];
		assertEquals(byTicks, byQuarters, 1.0E-9D);
	}

	/** Slow aiming at a high frame rate: mouse counts land on some frames and not others, and the view turns no further than the hand. */
	@Test
	void sparseSlowInputTurnsTheViewOnlyAsFarAsTheMouse() {
		LashCameraRules.State state = new LashCameraRules.State();
		double dt = 20.0D / 144.0D, turned = 0.0D, moved = 0.0D;
		for (int frame = 0; frame < 60; frame++) {
			double raw = frame % 3 == 0 ? 0.2D : 0.0D;
			moved += raw;
			turned += view(state, raw, 0.0D, dt, 1.0D)[0];
		}
		for (int frame = 0; frame < 60; frame++) turned += view(state, 0.0D, 0.0D, dt, 1.0D)[0];
		assertEquals(moved, turned, 1.0E-9D, "slow aiming drifted past the hand");
	}

	// ── The whip's lead over the view ──

	/** A hard swing straight up cannot carry the whip over the caster's head: its lead over the view is held in front. */
	@Test
	void theWhipCannotBeSwungBehindTheHead() {
		LashCameraRules.Lead lead = new LashCameraRules.Lead();
		for (int tick = 0; tick < 10; tick++) LashCameraRules.lead(lead, 0.0D, -60.0D, 1.0D, 70.0D, 0.88D);
		assertEquals(true, Math.abs(lead.pitch()) <= 70.0D * LashCameraRules.PITCH_SHARE + 1.0E-9D,
				"ten ticks of swinging up left the whip " + lead.pitch() + " degrees above the view");
		for (int tick = 0; tick < 10; tick++) LashCameraRules.lead(lead, 90.0D, 0.0D, 1.0D, 70.0D, 0.88D);
		assertEquals(true, Math.hypot(lead.yaw(), lead.pitch() / LashCameraRules.PITCH_SHARE) <= 70.0D + 1.0E-9D,
				"the whip got further from the view than its limit");
	}

	/** Once the hand stops, the whip comes back to the crosshair on its own. */
	@Test
	void theWhipComesBackToTheCrosshair() {
		LashCameraRules.Lead lead = new LashCameraRules.Lead();
		LashCameraRules.lead(lead, 50.0D, -30.0D, 1.0D, 70.0D, 0.88D);
		assertEquals(true, lead.yaw() > 40.0D, "a flick did not carry the whip out: " + lead.yaw());
		double before = Math.hypot(lead.yaw(), lead.pitch());
		for (int tick = 0; tick < 10; tick++) LashCameraRules.lead(lead, 0.0D, 0.0D, 1.0D, 70.0D, 0.88D);
		double after = Math.hypot(lead.yaw(), lead.pitch());
		assertEquals(true, after < before * 0.35D, "half a second on, the whip had only come back from " + before + " to " + after);
		for (int tick = 0; tick < 80; tick++) LashCameraRules.lead(lead, 0.0D, 0.0D, 1.0D, 70.0D, 0.88D);
		assertEquals(0.0D, lead.yaw(), 1.0E-9D, "the whip never reached the crosshair");
		assertEquals(0.0D, lead.pitch(), 1.0E-9D);
	}

	@Test
	void frameRateDoesNotChangeTheReturn() {
		LashCameraRules.Lead whole = new LashCameraRules.Lead(), quarters = new LashCameraRules.Lead();
		LashCameraRules.lead(whole, 40.0D, 0.0D, 1.0D, 70.0D, 0.88D);
		LashCameraRules.lead(quarters, 40.0D, 0.0D, 1.0D, 70.0D, 0.88D);
		for (int tick = 0; tick < 4; tick++) LashCameraRules.lead(whole, 0.0D, 0.0D, 1.0D, 70.0D, 0.88D);
		for (int frame = 0; frame < 16; frame++) LashCameraRules.lead(quarters, 0.0D, 0.0D, 0.25D, 70.0D, 0.88D);
		assertEquals(whole.yaw(), quarters.yaw(), 1.0E-9D);
	}
}
