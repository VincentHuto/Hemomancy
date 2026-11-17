package com.vincenthuto.hemomancy.common.manipulation.mortem;

import com.vincenthuto.hemomancy.common.manipulation.mortem.CarrionRules.Remains;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Round two P4: what Carrion Communion's Sculpting draws from the dead (Mortem Temper and health, never blood). */
class CarrionRulesTest {
	@Test
	void eachRemainIsWorthItsTableEntry() {
		assertEquals(2.0F, Remains.FLESH.temper);
		assertEquals(3.0F, Remains.BONE.temper, "a bone is worth what dissolving it as a Mortem catalyst is worth");
		assertEquals(10.0F, Remains.SKULL.temper);
		assertEquals(9.0F, Remains.BONE_BLOCK.temper, "CQ-16: a bone block is worth 9");
		assertEquals(1.0F, CarrionRules.HEAL_PER_REMAIN);
	}

	@Test
	void aBlockIsDrawnWholeAfterItsPulses() {
		assertFalse(CarrionRules.drawsWhole(Remains.BONE_BLOCK, 0));
		assertFalse(CarrionRules.drawsWhole(Remains.BONE_BLOCK, 2));
		assertTrue(CarrionRules.drawsWhole(Remains.BONE_BLOCK, 3), "four paid pulses take a bone block");
		assertFalse(CarrionRules.drawsWhole(Remains.SKULL, 0));
		assertTrue(CarrionRules.drawsWhole(Remains.SKULL, 1), "two take a skull");
		assertTrue(CarrionRules.drawsWhole(Remains.FLESH, 0));
	}

	@Test
	void theStaffDrawsMoreRemainsAndADryPulseDrawsLess() {
		assertEquals(1, CarrionRules.itemsPerPulse(1.0D));
		assertEquals(1, CarrionRules.itemsPerPulse(0.5D), "never none");
		assertEquals(1, CarrionRules.itemsPerPulse(0.0D));
		assertEquals(2, CarrionRules.itemsPerPulse(1.6D));
		assertEquals(3.0F, CarrionRules.temper(Remains.BONE, 1.0D), 1e-6F);
		assertEquals(3.0F, CarrionRules.temper(Remains.BONE, 2.0D), 1e-6F, "the Staff draws more remains, not more from each");
		assertEquals(1.5F, CarrionRules.temper(Remains.BONE, 0.5D), 1e-6F, "a dry pulse draws less");
		assertEquals(9.0F, CarrionRules.temper(Remains.BONE_BLOCK, 2.0D), 1e-6F, "CQ-16: the work scale never multiplies a block");
		assertEquals(10.0F, CarrionRules.temper(Remains.SKULL, 2.0D), 1e-6F, "nor a skull");
		assertEquals(4.5F, CarrionRules.temper(Remains.BONE_BLOCK, 0.5D), 1e-6F, "a dry pulse still draws less");
	}

	@Test
	void theNetIsTemperGainedSinceAPulseBurnsAQuarterOfAMortem() {
		float burnPerPulse = com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.TemperRules.pulseCost(
				CarrionDrawSculpting.CADENCE, CarrionDrawSculpting.BLOOD_PER_PULSE * 20.0D / CarrionDrawSculpting.CADENCE);
		assertEquals(0.25F, burnPerPulse, 1e-6F, "8 mL a second is the cheapest band, 0.5 a second");
		assertTrue(Remains.FLESH.temper > burnPerPulse, "one flesh a pulse still nets Temper");
		assertTrue(Remains.BONE_BLOCK.temper > Remains.BONE_BLOCK.pulses * burnPerPulse);
		assertTrue(Remains.SKULL.temper > Remains.SKULL.pulses * burnPerPulse);
	}
}
