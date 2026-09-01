package com.vincenthuto.hemomancy.common.item.harbinger.tool.living;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

final class LivingTorchBreathRulesTest {
	@Test
	void coneIncludesTargetsWithinSevenBlocksAndTwentyEightDegrees() {
		assertTrue(LivingTorchBreathRules.isInsideCone(0, 0, 0, 0, 0, 1,
				0, 0, 7));
		assertTrue(LivingTorchBreathRules.isInsideCone(0, 0, 0, 0, 0, 1,
				Math.sin(Math.toRadians(27)) * 6, 0, Math.cos(Math.toRadians(27)) * 6));
	}

	@Test
	void coneExcludesTargetsPastRangeBehindOrOutsideHalfAngle() {
		assertFalse(LivingTorchBreathRules.isInsideCone(0, 0, 0, 0, 0, 1,
				0, 0, 7.001));
		assertFalse(LivingTorchBreathRules.isInsideCone(0, 0, 0, 0, 0, 1,
				Math.sin(Math.toRadians(29)) * 6, 0, Math.cos(Math.toRadians(29)) * 6));
		assertFalse(LivingTorchBreathRules.isInsideCone(0, 0, 0, 0, 0, 1,
				0, 0, -2));
	}

	@Test
	void lineOfSightHostilityAndPulseDeduplicationAllGateDamage() {
		assertTrue(LivingTorchBreathRules.canHitCandidate(true, true, false));
		assertFalse(LivingTorchBreathRules.canHitCandidate(false, true, false));
		assertFalse(LivingTorchBreathRules.canHitCandidate(true, false, false));
		assertFalse(LivingTorchBreathRules.canHitCandidate(true, true, true));
	}

	@Test
	void windupDefersBloodAndDamageThenUsesFourTickCadence() {
		for (int tick = 0; tick < 6; tick++) {
			assertFalse(LivingTorchBreathRules.shouldDrainBlood(tick));
			assertFalse(LivingTorchBreathRules.isDamagePulse(tick));
		}
		assertTrue(LivingTorchBreathRules.shouldDrainBlood(6));
		assertTrue(LivingTorchBreathRules.isDamagePulse(6));
		assertFalse(LivingTorchBreathRules.isDamagePulse(7));
		assertFalse(LivingTorchBreathRules.isDamagePulse(9));
		assertTrue(LivingTorchBreathRules.isDamagePulse(10));
		assertEquals(3.0D, LivingTorchBreathRules.BLOOD_COST_PER_TICK);
		assertEquals(1.5F, LivingTorchBreathRules.DAMAGE_PER_PULSE);
	}

	@Test
	void paymentIsExactAndNeverOverdraws() {
		assertFalse(LivingTorchBreathRules.canPay(2.999D));
		assertTrue(LivingTorchBreathRules.canPay(3.0D));
		assertEquals(0.0D, LivingTorchBreathRules.bloodAfterPayment(3.0D));
		assertEquals(7.0D, LivingTorchBreathRules.bloodAfterPayment(10.0D));
	}

	@Test
	void everyRequiredInvalidStateStopsTheChannel() {
		LivingTorchBreathRules.ChannelState valid = new LivingTorchBreathRules.ChannelState(
				true, true, true, true, true, false, false, true);
		assertEquals(LivingTorchBreathRules.StopReason.NONE,
				LivingTorchBreathRules.stopReason(valid));
		assertEquals(LivingTorchBreathRules.StopReason.RELEASED,
				LivingTorchBreathRules.stopReason(valid.withUsing(false)));
		assertEquals(LivingTorchBreathRules.StopReason.HAND_OR_ITEM_CHANGED,
				LivingTorchBreathRules.stopReason(valid.withSameHeldStack(false)));
		assertEquals(LivingTorchBreathRules.StopReason.DEAD,
				LivingTorchBreathRules.stopReason(valid.withAlive(false)));
		assertEquals(LivingTorchBreathRules.StopReason.LOGGED_OUT,
				LivingTorchBreathRules.stopReason(valid.withConnected(false)));
		assertEquals(LivingTorchBreathRules.StopReason.DIMENSION_CHANGED,
				LivingTorchBreathRules.stopReason(valid.withSameDimension(false)));
		assertEquals(LivingTorchBreathRules.StopReason.STAFF_FORM_RESTORED,
				LivingTorchBreathRules.stopReason(valid.withStaffFormRestored(true)));
		assertEquals(LivingTorchBreathRules.StopReason.CARDINAL_RITE_BLOCKING,
				LivingTorchBreathRules.stopReason(valid.withBlockingCardinalRite(true)));
		assertEquals(LivingTorchBreathRules.StopReason.NO_ACTIVE_BLOOD,
				LivingTorchBreathRules.stopReason(valid.withActiveBlood(false)));
	}

	@Test
	void castingMovementIsThirtyFivePercentAndJumpingDoesNotStopIt() {
		assertEquals(0.35F, LivingTorchBreathRules.MOVEMENT_MULTIPLIER);
		LivingTorchBreathRules.ChannelState jumping = new LivingTorchBreathRules.ChannelState(
				true, true, true, true, true, false, false, true);
		assertEquals(LivingTorchBreathRules.StopReason.NONE,
				LivingTorchBreathRules.stopReason(jumping));
	}
}
