package com.vincenthuto.hemomancy.client.screen.skilltree.shared;

import com.vincenthuto.hemomancy.common.item.shared.MnemonicBlueprintTarget;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class RecipeMapBlueprintTargetTest {
	@Test
	void unlockedFloorNodesCreateRiteBlueprintTargets() {
		RecipeMapEntry floor = entry(RecipeMapEntry.Kind.FLOOR, true);

		assertEquals(new MnemonicBlueprintTarget(MnemonicBlueprintTarget.Type.CARDINAL_RITE, floor.id()),
				RecipeMapBlueprintTarget.from(floor));
	}

	@Test
	void lockedAndNonBlueprintNodesRemainIneligible() {
		assertNull(RecipeMapBlueprintTarget.from(entry(RecipeMapEntry.Kind.FLOOR, false)));
		assertNull(RecipeMapBlueprintTarget.from(entry(RecipeMapEntry.Kind.SIGIL, true)));
	}

	private static RecipeMapEntry entry(RecipeMapEntry.Kind kind, boolean unlocked) {
		return new RecipeMapEntry(new RecipeMapKey(kind,
				ResourceLocation.fromNamespaceAndPath("hemomancy", "test_node")),
				"Test", "", 0, "Test", 0, true, unlocked);
	}
}
