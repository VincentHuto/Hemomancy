package com.vincenthuto.hemomancy.client.vein;

import com.vincenthuto.hemomancy.common.vein.EarthenVeinTravelPhase;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

final class EarthenVeinTravelVisualsTest {
	@Test
	void oneArrivalFinishingDoesNotClearAnotherArrivalAtTheSameVein() {
		var visuals = new EarthenVeinTravelVisuals();
		ResourceLocation overworld = ResourceLocation.withDefaultNamespace("overworld");
		BlockPos vein = new BlockPos(4, 70, 8);
		visuals.update(overworld, vein, 10, EarthenVeinTravelPhase.EJECTING, 0.2F);
		visuals.update(overworld, vein, 11, EarthenVeinTravelPhase.EJECTING, 0.6F);

		visuals.update(overworld, vein, 10, EarthenVeinTravelPhase.IDLE, 0.0F);

		var remaining = visuals.visual(overworld, vein);
		assertNotNull(remaining);
		assertEquals(11, remaining.travelerEntityId());
	}
}
