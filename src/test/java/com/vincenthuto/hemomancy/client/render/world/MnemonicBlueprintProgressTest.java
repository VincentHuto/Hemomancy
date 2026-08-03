package com.vincenthuto.hemomancy.client.render.world;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MnemonicBlueprintProgressTest {
	@Test
	void groupsMissingPlacementsByMaterialAndOrdersLargestGroupsFirst() {
		MnemonicBlueprintProgress.Summary summary = MnemonicBlueprintProgress.summarize(List.of(
				"minecraft:dirt", "minecraft:stone", "minecraft:glass",
				"minecraft:stone", "minecraft:stone", "minecraft:dirt", "minecraft:oak_planks"));

		assertEquals(7, summary.remaining());
		assertEquals(List.of(
				new MnemonicBlueprintProgress.Entry("minecraft:stone", 3),
				new MnemonicBlueprintProgress.Entry("minecraft:dirt", 2),
				new MnemonicBlueprintProgress.Entry("minecraft:glass", 1)), summary.visibleEntries(3));
		assertEquals(1, summary.hiddenTypes(3));
	}

	@Test
	void reportsACompletedProjectionWithoutMaterialGroups() {
		MnemonicBlueprintProgress.Summary summary = MnemonicBlueprintProgress.summarize(List.of());

		assertEquals(0, summary.remaining());
		assertEquals(List.of(), summary.visibleEntries(3));
		assertEquals(0, summary.hiddenTypes(3));
	}
}
