package com.vincenthuto.hemomancy.client.event;

import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ManipulationInputRulesTest {
	@Test
	void quickAndPassiveFireOnceOnPress() {
		var quick = ManipulationInputRules.tick(EnumManipulationType.QUICK, true, true, 0, 20);
		var passive = ManipulationInputRules.tick(EnumManipulationType.PASSIVE, true, true, 0, 20);

		assertTrue(quick.cast());
		assertTrue(passive.cast());
		assertFalse(ManipulationInputRules.tick(EnumManipulationType.QUICK, true, false, 0, 20).cast());
	}

	@Test
	void continuousSendsOneStartAndOneStop() {
		var first = ManipulationInputRules.tick(EnumManipulationType.CONTINUOUS, true, true, 0, 20);
		var second = ManipulationInputRules.tick(EnumManipulationType.CONTINUOUS, true, false,
				first.nextHeldTicks(), 20);
		var released = ManipulationInputRules.tick(EnumManipulationType.CONTINUOUS, false, false,
				second.nextHeldTicks(), 20);

		assertEquals(ManipulationInputRules.Action.START_CONTINUOUS, first.action());
		assertEquals(ManipulationInputRules.Action.NONE, second.action());
		assertEquals(ManipulationInputRules.Action.STOP_CONTINUOUS, released.action());
		assertEquals(0, released.nextHeldTicks());
	}

	@Test
	void chargedFiresPartialChargeOnRelease() {
		var held = ManipulationInputRules.tick(EnumManipulationType.CHARGED, true, true, 0, 40);
		for (int i = 1; i < 20; i++) {
			held = ManipulationInputRules.tick(EnumManipulationType.CHARGED, true, false,
					held.nextHeldTicks(), 40);
		}
		var released = ManipulationInputRules.tick(EnumManipulationType.CHARGED, false, false,
				held.nextHeldTicks(), 40);

		assertFalse(held.cast());
		assertTrue(released.cast());
		assertEquals(20, released.castTicks());
		assertEquals(0, released.nextHeldTicks());
	}

	@Test
	void chargedFullStrengthStaysLatchedUntilTheServerAcceptsIt() {
		var charging = ManipulationInputRules.tick(EnumManipulationType.CHARGED, true, true, 0, 40);
		for (int i = 1; i < 39; i++) {
			charging = ManipulationInputRules.tick(EnumManipulationType.CHARGED, true, false,
					charging.nextHeldTicks(), 40);
		}
		assertFalse(charging.cast());
		assertEquals(39, charging.nextHeldTicks());

		var completed = ManipulationInputRules.tick(EnumManipulationType.CHARGED, true, false,
				charging.nextHeldTicks(), 40);
		assertTrue(completed.cast());
		assertEquals(40, completed.castTicks());
		assertEquals(40, completed.nextHeldTicks());

		// A successful server response resets the client charge to zero.
		var nextHeart = ManipulationInputRules.tick(EnumManipulationType.CHARGED, true, false,
				0, 40);
		assertFalse(nextHeart.cast());
		assertEquals(1, nextHeart.nextHeldTicks());
	}
}
