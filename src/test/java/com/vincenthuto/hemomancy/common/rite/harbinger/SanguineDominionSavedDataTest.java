package com.vincenthuto.hemomancy.common.rite.harbinger;

import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

final class SanguineDominionSavedDataTest {
	@Test
	void recastingRelocatesTheOwnersSingleDomain() {
		SanguineDominionSavedData data = new SanguineDominionSavedData();
		UUID owner = UUID.randomUUID();
		var first = new SanguineDominionSavedData.DominionEntry(
				owner, BlockPos.ZERO, "minecraft:overworld", 2, 10);
		var second = new SanguineDominionSavedData.DominionEntry(
				owner, new BlockPos(100, 64, 100), "minecraft:the_nether", 2, 20);

		assertNull(data.replaceDominionForOwner(first));
		assertEquals(first, data.replaceDominionForOwner(second));
		assertEquals(1, data.getDominions().size());
		assertEquals(second, data.getDominions().get(0));
	}
}
