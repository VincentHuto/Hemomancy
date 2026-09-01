package com.vincenthuto.hemomancy.client.screen.skilltree.shared;

import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

final class RecipeMapNodeShapeTest {
	@Test
	void floorNodeUsesDiamondHitAreaWhileOrdinaryNodesRemainSquare() {
		RecipeMapLayout.NodeBounds floor = bounds(RecipeMapEntry.Kind.FLOOR);
		RecipeMapLayout.NodeBounds rite = bounds(RecipeMapEntry.Kind.RITE);

		assertTrue(floor.contains(14, 14));
		assertFalse(floor.contains(1, 1), "transparent diamond corners must not select the floor");
		assertTrue(rite.contains(1, 1), "ordinary map nodes keep their rectangular hit area");
	}

	@Test
	void floorIconKeepsNativeItemSizeInsideDiamond() {
		assertEquals(10, RecipeMapCanvas.iconHalfSize(RecipeMapEntry.Kind.FLOOR, 28));
		assertEquals(14, RecipeMapCanvas.iconHalfSize(RecipeMapEntry.Kind.RITE, 28));
	}

	private static RecipeMapLayout.NodeBounds bounds(RecipeMapEntry.Kind kind) {
		RecipeMapEntry entry = new RecipeMapEntry(
				new RecipeMapKey(kind, ResourceLocation.fromNamespaceAndPath("hemomancy", "test")),
				"Test", 0, "Test", 0, true, true);
		return new RecipeMapLayout.NodeBounds(entry, 0, 0, 28, 28);
	}
}
