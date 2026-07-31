package com.vincenthuto.hemomancy.common.rite.harbinger;

import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class RiteRelocationConfirmationRulesTest {
	@Test
	void relocationConfirmationIsShortLivedAndLocationSpecific() {
		BlockPos requested = new BlockPos(20, 70, -4);
		assertTrue(RiteRelocationConfirmationRules.confirmed(
				requested, "minecraft:overworld", 500,
				requested, "minecraft:overworld", 500));
		assertFalse(RiteRelocationConfirmationRules.confirmed(
				requested, "minecraft:overworld", 501,
				requested, "minecraft:overworld", 500));
		assertFalse(RiteRelocationConfirmationRules.confirmed(
				requested, "minecraft:the_nether", 400,
				requested, "minecraft:overworld", 500));
		assertFalse(RiteRelocationConfirmationRules.confirmed(
				requested, "minecraft:overworld", 400,
				BlockPos.ZERO, "minecraft:overworld", 500));
	}
}
