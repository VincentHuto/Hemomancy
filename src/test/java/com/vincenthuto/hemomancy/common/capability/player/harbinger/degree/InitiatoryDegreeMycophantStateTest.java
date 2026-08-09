package com.vincenthuto.hemomancy.common.capability.player.harbinger.degree;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class InitiatoryDegreeMycophantStateTest {
	@Test
	void mycophantProgressRoundTripsAndMissingTagsDefaultSafely() {
		InitiatoryDegree degree = new InitiatoryDegree();
		degree.setMycophantExposureTicks(12_345);
		degree.setMycophantRetryCooldownTicks(6_000);
		degree.setMycophantDefeated(true);

		InitiatoryDegree restored = new InitiatoryDegree();
		restored.deserializeNBT(null, degree.serializeNBT(null));
		assertEquals(12_345, restored.getMycophantExposureTicks());
		assertEquals(6_000, restored.getMycophantRetryCooldownTicks());
		assertTrue(restored.isMycophantDefeated());

		InitiatoryDegree legacy = new InitiatoryDegree();
		legacy.deserializeNBT(null, new net.minecraft.nbt.CompoundTag());
		assertEquals(0, legacy.getMycophantExposureTicks());
		assertEquals(0, legacy.getMycophantRetryCooldownTicks());
	}
}
