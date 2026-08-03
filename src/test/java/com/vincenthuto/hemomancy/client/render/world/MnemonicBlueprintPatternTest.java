package com.vincenthuto.hemomancy.client.render.world;

import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class MnemonicBlueprintPatternTest {
	@Test
	void coordinatesPreserveBottomUpRowsAndExcludeSpaces() {
		List<BlockPos> positions = MnemonicBlueprintPattern.coordinates(
				new String[][]{{"S ", " S"}, {" S", "S "}}, symbol -> !symbol.equals(" "));

		assertEquals(List.of(new BlockPos(0, 1, 0), new BlockPos(1, 0, 0),
				new BlockPos(1, 1, 1), new BlockPos(0, 0, 1)), positions);
	}

	@Test
	void exactAndTagKeysUseTheirDistinctMatchRules() {
		assertTrue(MnemonicBlueprintPattern.matchDecision(false, false, true));
		assertFalse(MnemonicBlueprintPattern.matchDecision(false, true, false));
		assertTrue(MnemonicBlueprintPattern.matchDecision(true, true, false));
		assertTrue(MnemonicBlueprintPattern.matchDecision(true, false, true));
		assertFalse(MnemonicBlueprintPattern.matchDecision(true, false, false));
	}
}
