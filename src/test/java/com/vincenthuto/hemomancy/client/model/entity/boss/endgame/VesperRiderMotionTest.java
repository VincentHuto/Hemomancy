package com.vincenthuto.hemomancy.client.model.entity.boss.endgame;

import com.vincenthuto.hemomancy.common.entity.boss.endgame.VesperPhaseOneAttack;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.VesperWingedFlightRules.FlightMode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

final class VesperRiderMotionTest {
	private static VesperRiderMotion.State grounded(VesperPhaseOneAttack attack, float tick) {
		return new VesperRiderMotion.State(attack, tick, 0.0F, false, FlightMode.GROUNDED, false);
	}

	@Test
	void musterRaisesTheCrossbar() {
		assertTrue(VesperRiderMotion.weight(VesperRiderMotion.Pose.CROSSBAR_RAISE,
				grounded(VesperPhaseOneAttack.PUPPET_MUSTER, 20.0F)) > 0.5F);
		assertEquals(0.0F, VesperRiderMotion.weight(VesperRiderMotion.Pose.CROSSBAR_RAISE,
				grounded(VesperPhaseOneAttack.ROYAL_SCUTTLE, 20.0F)), 1.0E-5F);
		assertTrue(VesperRiderMotion.weight(VesperRiderMotion.Pose.CROSSBAR_RAISE,
				grounded(VesperPhaseOneAttack.RECALL, 15.0F)) > 0.5F);
	}

	@Test
	void burrowTucksAndGripsTheNeck() {
		VesperRiderMotion.State burrow = new VesperRiderMotion.State(VesperPhaseOneAttack.IDLE, 0.0F, 0.0F, false,
				FlightMode.GROUNDED, true);
		assertEquals(1.0F, VesperRiderMotion.weight(VesperRiderMotion.Pose.NECK_GRIP_TUCK, burrow), 1.0E-5F);
	}

	@Test
	void woundRecoilAndFlightLeanOnlyInTheirStates() {
		VesperRiderMotion.State wounded = new VesperRiderMotion.State(VesperPhaseOneAttack.IDLE, 0.0F, 0.0F, true,
				FlightMode.GROUNDED, false);
		assertEquals(1.0F, VesperRiderMotion.weight(VesperRiderMotion.Pose.WOUND_RECOIL, wounded), 1.0E-5F);
		assertEquals(0.0F, VesperRiderMotion.weight(VesperRiderMotion.Pose.FLIGHT_LEAN, wounded), 1.0E-5F);
		VesperRiderMotion.State flying = new VesperRiderMotion.State(VesperPhaseOneAttack.IDLE, 0.0F, 0.0F, false,
				FlightMode.CIRCLING, false);
		assertEquals(1.0F, VesperRiderMotion.weight(VesperRiderMotion.Pose.FLIGHT_LEAN, flying), 1.0E-5F);
	}

	@Test
	void staggerFadesWithItsTimer() {
		VesperRiderMotion.State fresh = new VesperRiderMotion.State(VesperPhaseOneAttack.IDLE, 0.0F, 14.0F, false,
				FlightMode.GROUNDED, false);
		VesperRiderMotion.State late = new VesperRiderMotion.State(VesperPhaseOneAttack.IDLE, 0.0F, 2.0F, false,
				FlightMode.GROUNDED, false);
		assertTrue(VesperRiderMotion.weight(VesperRiderMotion.Pose.STAGGER, fresh)
				> VesperRiderMotion.weight(VesperRiderMotion.Pose.STAGGER, late));
	}
}
