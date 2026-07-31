package com.vincenthuto.hemomancy.common.rite;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

class CardinalRiteCancellationRulesTest {

	@Test
	void onlyOwnerTargetingCenterCanChannelPlantedInteractiveRite() {
		UUID owner = UUID.randomUUID();
		BlockPos center = new BlockPos(4, 70, -8);

		assertTrue(CardinalRiteCancellationRules.canChannel(
				owner, owner, center, center, true, false));
		assertFalse(CardinalRiteCancellationRules.canChannel(
				UUID.randomUUID(), owner, center, center, true, false));
		assertFalse(CardinalRiteCancellationRules.canChannel(
				owner, owner, center.above(), center, true, false));
		assertFalse(CardinalRiteCancellationRules.canChannel(
				owner, owner, center, center, false, false));
		assertFalse(CardinalRiteCancellationRules.canChannel(
				owner, owner, center, center, true, true));
	}

	@Test
	void renderedStaffCanBeTargetedAboveThePhysicalFocusBlock() {
		BlockPos center = new BlockPos(4, 70, -8);
		Vec3 eye = new Vec3(4.5D, 72.0D, -3.5D);

		assertTrue(CardinalRiteCancellationRules.aimsAtPlantedStaff(
				eye, new Vec3(0.0D, 0.0D, -1.0D), 8.0D, center));
		assertFalse(CardinalRiteCancellationRules.aimsAtPlantedStaff(
				eye, new Vec3(1.0D, 0.0D, 0.0D), 8.0D, center));
	}

	@Test
	void raisedAndEnlargedStaffCanBeTargetedAcrossItsVisibleUpperSection() {
		BlockPos center = new BlockPos(4, 70, -8);
		Vec3 eye = new Vec3(4.5D, 74.25D, -3.5D);

		assertTrue(CardinalRiteCancellationRules.aimsAtPlantedStaff(
				eye, new Vec3(0.0D, 0.0D, -1.0D), 8.0D, center));
		assertFalse(CardinalRiteCancellationRules.aimsAtPlantedStaff(
				eye, new Vec3(1.0D, 0.0D, 0.0D), 8.0D, center));
	}

	@Test
	void continuousChannelAdvancesAndInterruptionResets() {
		assertEquals(1, CardinalRiteCancellationRules.nextChannelTicks(0, true));
		assertEquals(CardinalRiteCancellationRules.TOTAL_TICKS,
				CardinalRiteCancellationRules.nextChannelTicks(
						CardinalRiteCancellationRules.TOTAL_TICKS, true));
		assertEquals(0, CardinalRiteCancellationRules.nextChannelTicks(37, false));
	}

	@Test
	void animationUsesDaemonThenStaffStages() {
		assertEquals(0.0D, CardinalRiteCancellationRules.daemonAbsorptionProgress(0), 0.0001D);
		assertEquals(1.0D, CardinalRiteCancellationRules.daemonAbsorptionProgress(
				CardinalRiteCancellationRules.DAEMON_ABSORPTION_TICKS), 0.0001D);
		assertEquals(0.0D, CardinalRiteCancellationRules.staffDissolutionProgress(
				CardinalRiteCancellationRules.DAEMON_ABSORPTION_TICKS), 0.0001D);
		assertEquals(1.0D, CardinalRiteCancellationRules.staffDissolutionProgress(
				CardinalRiteCancellationRules.TOTAL_TICKS), 0.0001D);
		assertFalse(CardinalRiteCancellationRules.isComplete(
				CardinalRiteCancellationRules.TOTAL_TICKS - 1));
		assertTrue(CardinalRiteCancellationRules.isComplete(
				CardinalRiteCancellationRules.TOTAL_TICKS));
	}

	@Test
	void cancellationNeverInventsADaemonBeforeTheRiteSpawnsOne() {
		assertFalse(CardinalRiteCancellationRules.canAnimateDaemon(false));
		assertTrue(CardinalRiteCancellationRules.canAnimateDaemon(true));
	}
}
