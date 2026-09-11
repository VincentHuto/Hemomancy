package com.vincenthuto.hemomancy.common.vein;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class EarthenVeinTravelRulesTest {
	private static final ResourceLocation OVERWORLD = ResourceLocation.withDefaultNamespace("overworld");
	private static final ResourceLocation NETHER = ResourceLocation.withDefaultNamespace("the_nether");

	@Test
	void sameDimensionCostUsesTheFourAgreedDistanceBands() {
		BlockPos origin = BlockPos.ZERO;
		assertEquals(500, EarthenVeinTravelRules.cost(origin, OVERWORLD, new BlockPos(256, 80, 0), OVERWORLD));
		assertEquals(1_000, EarthenVeinTravelRules.cost(origin, OVERWORLD, new BlockPos(257, 0, 0), OVERWORLD));
		assertEquals(1_000, EarthenVeinTravelRules.cost(origin, OVERWORLD, new BlockPos(1_024, -80, 0), OVERWORLD));
		assertEquals(2_000, EarthenVeinTravelRules.cost(origin, OVERWORLD, new BlockPos(1_025, 0, 0), OVERWORLD));
		assertEquals(2_000, EarthenVeinTravelRules.cost(origin, OVERWORLD, new BlockPos(4_096, 0, 0), OVERWORLD));
		assertEquals(3_000, EarthenVeinTravelRules.cost(origin, OVERWORLD, new BlockPos(4_097, 0, 0), OVERWORLD));
	}

	@Test
	void verticalSeparationDoesNotInflateDirectionalTravelCost() {
		assertEquals(500, EarthenVeinTravelRules.cost(BlockPos.ZERO, OVERWORLD,
				new BlockPos(0, 2_000, 0), OVERWORLD));
	}

	@Test
	void crossDimensionTravelAlwaysCostsFourThousand() {
		assertEquals(4_000, EarthenVeinTravelRules.cost(BlockPos.ZERO, OVERWORLD,
				new BlockPos(1, 1, 1), NETHER));
	}

	@Test
	void feedingClampsToTheRemainingCostAndAvailableBlood() {
		assertEquals(100.0D, EarthenVeinTravelRules.acceptedBlood(700.0D, 1_000.0D, 100.0D, 400.0D));
		assertEquals(50.0D, EarthenVeinTravelRules.acceptedBlood(950.0D, 1_000.0D, 100.0D, 400.0D));
		assertEquals(25.0D, EarthenVeinTravelRules.acceptedBlood(700.0D, 1_000.0D, 100.0D, 25.0D));
		assertEquals(0.0D, EarthenVeinTravelRules.acceptedBlood(1_000.0D, 1_000.0D, 100.0D, 400.0D));
	}

	@Test
	void inactivityAndReadyWindowsUseTheirSeparateTimeouts() {
		assertFalse(EarthenVeinTravelRules.feedExpired(400L, 400L + 299L));
		assertTrue(EarthenVeinTravelRules.feedExpired(400L, 400L + 301L));
		assertFalse(EarthenVeinTravelRules.readyExpired(400L, 400L + 599L));
		assertTrue(EarthenVeinTravelRules.readyExpired(400L, 400L + 601L));
	}

	@Test
	void temporaryOriginsOutliveEveryActivePhase() {
		assertTrue(EarthenVeinTravelRules.temporaryExpiry(EarthenVeinTravelPhase.SELECTING, 100L)
				> 100L + EarthenVeinTravelRules.DISPLAY_TIMEOUT_TICKS);
		assertTrue(EarthenVeinTravelRules.temporaryExpiry(EarthenVeinTravelPhase.FEEDING, 100L)
				> 100L + EarthenVeinTravelRules.FEED_TIMEOUT_TICKS);
		assertTrue(EarthenVeinTravelRules.temporaryExpiry(EarthenVeinTravelPhase.READY, 100L)
				> 100L + EarthenVeinTravelRules.READY_TIMEOUT_TICKS);
	}

	@Test
	void fallProtectionExpiresAtItsDeadline() {
		assertTrue(EarthenVeinTravelRules.hasFallProtection(180L, 180L));
		assertFalse(EarthenVeinTravelRules.hasFallProtection(180L, 181L));
	}
}
