package com.vincenthuto.hemomancy.common.event;

import com.vincenthuto.hemomancy.common.block.harbinger.functional.EarthenVeinBlock;
import net.minecraft.world.level.block.Block;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class BloodProjectionInteractionEventsTest {
	@Test
	void earthenVeinsRouteTheInitialClickToTheHeldBloodTool() {
		assertTrue(BloodProjectionInteractionEvents.isDirectBloodToolTarget(EarthenVeinBlock.class));
		assertFalse(BloodProjectionInteractionEvents.isDirectBloodToolTarget(Block.class));
	}
}
