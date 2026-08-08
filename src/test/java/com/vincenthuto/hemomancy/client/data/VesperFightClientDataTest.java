package com.vincenthuto.hemomancy.client.data;

import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class VesperFightClientDataTest {
	@AfterEach
	void clearState() {
		VesperFightClientData.clear();
	}

	@Test
	void activationRetainsTheExactWorldAnchor() {
		BlockPos center = new BlockPos(4096, 64, 384);
		VesperFightClientData.activate(center);

		assertTrue(VesperFightClientData.isActive());
		assertEquals(center, VesperFightClientData.center());
	}

	@Test
	void clearRemovesBothActivityAndAnchor() {
		VesperFightClientData.activate(BlockPos.ZERO);
		VesperFightClientData.clear();

		assertFalse(VesperFightClientData.isActive());
		assertEquals(BlockPos.ZERO, VesperFightClientData.center());
	}
}
