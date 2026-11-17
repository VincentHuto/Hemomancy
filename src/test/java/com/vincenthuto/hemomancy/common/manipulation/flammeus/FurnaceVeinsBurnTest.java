package com.vincenthuto.hemomancy.common.manipulation.flammeus;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Round two P4: Furnace Veins keeps a furnace burning only while the stream is held, and never banks fuel. */
class FurnaceVeinsBurnTest {
	@Test
	void aPulseBurnsJustPastTheNextPulse() {
		assertEquals(FurnaceVeinsSculpting.CADENCE + FurnaceVeinsSculpting.BURN_MARGIN, FurnaceVeinsSculpting.burnTicks(1.0D));
		assertTrue(FurnaceVeinsSculpting.burnTicks(1.0D) > FurnaceVeinsSculpting.CADENCE, "a held stream never flickers");
		assertTrue(FurnaceVeinsSculpting.burnTicks(1.0D) <= 20, "let go and the fire dies within a second");
	}

	@Test
	void theStaffNeverOutburnsAndADryPulseGutters() {
		assertEquals(FurnaceVeinsSculpting.burnTicks(1.0D), FurnaceVeinsSculpting.burnTicks(3.0D), "no faster cooking than coal");
		assertTrue(FurnaceVeinsSculpting.burnTicks(0.5D) < FurnaceVeinsSculpting.CADENCE, "a dry stream gutters between pulses");
		assertEquals(1, FurnaceVeinsSculpting.burnTicks(0.0D));
	}
}
