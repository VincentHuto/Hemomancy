package com.vincenthuto.hemomancy.common.tile.crafting;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

class VialCentrifugeAuxiliaryOutputSourceTest {
	@Test
	void sampledVialsProduceHematicIronPowderAsTheAuxiliaryOutput() throws IOException {
		String source = Files.readString(Path.of(
				"src/main/java/com/vincenthuto/hemomancy/common/tile/crafting/VialCentrifugeBlockEntity.java"));
		String outputResults = source.substring(source.indexOf("private void outputResults()"),
				source.indexOf("public List<ItemStack> getVialSlots()"));

		assertTrue(outputResults.contains("new ItemStack(ItemInit.hematic_iron_powder.get())"));
		assertTrue(outputResults.contains("inventory.get(18).getItem() == ItemInit.hematic_iron_powder.get()"));
		assertFalse(outputResults.contains("befouling_ash"));
	}
}
