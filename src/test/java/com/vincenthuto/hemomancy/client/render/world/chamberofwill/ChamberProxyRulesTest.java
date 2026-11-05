package com.vincenthuto.hemomancy.client.render.world.chamberofwill;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class ChamberProxyRulesTest {
	private static final double[] HUSK = { 10.5, 64.0, 10.5 };
	private static final double[] GATE = { 0.5, 3.0, 24.5 };

	@Test
	void inFrontOfASouthFacingHuskIsInFrontOfTheGateLookingIn() {
		// Two blocks south of the husk, looking north at it.
		var p = ChamberProxyRules.place(0.0F, HUSK, GATE, new double[] { 10.5, 65.6, 12.5 }, 180.0F, 5.0F);
		assertEquals(0.5, p.x(), 1e-9);
		assertEquals(4.6, p.y(), 1e-9);
		assertEquals(24.5 + 2.0, p.z(), 1e-9);
		assertEquals(180.0F, Math.abs(p.yaw()), 1e-6, "looking north, into the corridor");
		assertEquals(5.0F, p.pitch(), 1e-6);
	}

	@Test
	void inFrontOfANorthFacingHuskIsTheSameView() {
		// Two blocks north of the husk, looking south at it.
		var p = ChamberProxyRules.place(180.0F, HUSK, GATE, new double[] { 10.5, 65.6, 8.5 }, 0.0F, 0.0F);
		assertEquals(0.5, p.x(), 1e-9);
		assertEquals(24.5 + 2.0, p.z(), 1e-9);
		assertEquals(180.0F, Math.abs(p.yaw()), 1e-6);
	}

	@Test
	void theViewersLeftStaysTheirLeft() {
		// East-facing husk; the viewer two blocks east of it (in front) and one block south, looking west at it. South
		// is to their left. In the corridor they look north, and their left is west.
		var p = ChamberProxyRules.place(270.0F, HUSK, GATE, new double[] { 12.5, 65.6, 11.5 }, 90.0F, 0.0F);
		assertEquals(24.5 + 2.0, p.z(), 1e-9, "two blocks in front of the eye is two blocks in front of the gate");
		assertEquals(0.5 - 1.0, p.x(), 1e-9, "one block to the left stays one block to the left");
		assertEquals(180.0F, Math.abs(p.yaw()), 1e-6, "looking at the husk is looking into the corridor");
	}

	@Test
	void behindTheHuskIsInsideTheCorridorLookingOut() {
		var p = ChamberProxyRules.place(0.0F, HUSK, GATE, new double[] { 10.5, 65.6, 7.5 }, 0.0F, 0.0F);
		assertEquals(24.5 - 3.0, p.z(), 1e-9, "three blocks behind the eye is three blocks inside the gate");
		assertEquals(0.0F, p.yaw(), 1e-6, "looking south, out of the corridor");
	}

	@Test
	void aWalkInThroughTheGateCarriesTheViewThatFarInside() {
		// Two blocks in front of a south-facing husk, two blocks into the walk: two blocks in front of the gate, less two.
		var p = ChamberProxyRules.place(0.0F, HUSK, GATE, new double[] { 10.5, 65.6, 12.5 }, 180.0F, 0.0F, 2.0D);
		assertEquals(24.5 + 2.0 - 2.0, p.z(), 1e-9, "the depth walked is taken off the gate's south");
		assertEquals(0.5, p.x(), 1e-9);
	}

	@Test
	void theWalkOutLooksOutOfTheHuskTheWayTheWalkInLookedIntoTheGate() {
		// At the gate's eye looking south (out), two blocks into the walk: two blocks in front of a west-facing husk,
		// looking west.
		var p = ChamberProxyRules.placeOutside(90.0F, GATE, HUSK, new double[] { 0.5, 3.0, 24.5 }, 0.0F, 4.0F, 2.0D);
		assertEquals(10.5 - ChamberProxyRules.OUTSIDE_FRONT - 2.0, p.x(), 1e-9, "from the husk's front, two blocks out along its facing");
		assertEquals(64.0, p.y(), 1e-9);
		assertEquals(10.5, p.z(), 1e-9);
		assertEquals(90.0F, p.yaw(), 1e-6, "looking out of a west-facing husk is looking west");
		assertEquals(4.0F, p.pitch(), 1e-6);
		// A step to the viewer's left at the gate (east, looking south) is a step to their left outside (south, looking west).
		var left = ChamberProxyRules.placeOutside(90.0F, GATE, HUSK, new double[] { 1.5, 3.0, 24.5 }, 0.0F, 0.0F, 0.0D);
		assertEquals(10.5 - ChamberProxyRules.OUTSIDE_FRONT, left.x(), 1e-9);
		assertEquals(10.5 + 1.0, left.z(), 1e-9, "one block to the left stays one block to the left");
		// Ten blocks back from the gate, inside the corridor, is not ten blocks behind the husk, into the ground and the
		// trees: the view stays at the husk's front however far back the viewer stands.
		var back = ChamberProxyRules.placeOutside(90.0F, GATE, HUSK, new double[] { 0.5, 3.0, 14.5 }, 0.0F, 0.0F, 0.0D);
		assertEquals(10.5 - ChamberProxyRules.OUTSIDE_FRONT, back.x(), 1e-9, "never behind the front");
		assertEquals(10.5, back.z(), 1e-9);
	}
}
