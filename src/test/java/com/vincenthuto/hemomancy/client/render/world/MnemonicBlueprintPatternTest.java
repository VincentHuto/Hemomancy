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

	@Test
	void riteCompositionAlignsTheFloorFocusAndUpperStructureAboveIt() {
		MnemonicBlueprintPattern floor = new MnemonicBlueprintPattern(List.of(
				new MnemonicBlueprintPattern.Cell(new BlockPos(0, 0, 0), null),
				new MnemonicBlueprintPattern.Cell(new BlockPos(1, 0, 1), null),
				new MnemonicBlueprintPattern.Cell(new BlockPos(2, 0, 2), null)),
				new MnemonicBlueprintPlacement.Bounds(0, 2, 0, 2));
		MnemonicBlueprintPattern upper = new MnemonicBlueprintPattern(List.of(
				new MnemonicBlueprintPattern.Cell(new BlockPos(0, 1, 0), null),
				new MnemonicBlueprintPattern.Cell(new BlockPos(2, 1, 2), null)),
				new MnemonicBlueprintPlacement.Bounds(0, 2, 0, 2));

		MnemonicBlueprintPattern combined = MnemonicBlueprintPattern.composeRiteLayers(
				floor, new BlockPos(1, 0, 1), upper, new BlockPos(1, 1, 1));

		assertEquals(List.of(
				new BlockPos(-1, 0, -1), new BlockPos(0, 0, 0), new BlockPos(1, 0, 1),
				new BlockPos(-1, 1, -1), new BlockPos(1, 1, 1)),
				combined.cells().stream().map(MnemonicBlueprintPattern.Cell::localPos).toList());
		assertEquals(new MnemonicBlueprintPlacement.Bounds(-1, 1, -1, 1), combined.bounds());
	}
}
