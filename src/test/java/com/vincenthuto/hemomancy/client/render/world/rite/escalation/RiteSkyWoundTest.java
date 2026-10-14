package com.vincenthuto.hemomancy.client.render.world.rite.escalation;

import com.vincenthuto.hemomancy.common.rite.CardinalRiteInstability;
import com.vincenthuto.hemomancy.common.rite.CardinalRitePhase;
import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

final class RiteSkyWoundTest {
	private static RiteWorldState.World world(float level, CardinalRitePhase phase, RiteWorldMath.Ending ending,
			float endingAge) {
		return new RiteWorldState.World(BlockPos.ZERO, null, level, level, 0.0F, 2, 4, CardinalRiteInstability.STABLE,
				10.0F, "storm", true, phase, RiteFloor.NONE, ending, endingAge);
	}

	@Test
	void noWoundBeforeHalfway() {
		assertNull(RiteSkyWound.shape(world(0.25F, CardinalRitePhase.CONSECRATION, RiteWorldMath.Ending.NONE, 0), 1));
	}

	@Test
	void itFormsAtHalfwayAndGrowsWithTheRite() {
		RiteSkyWound.Shape formed = RiteSkyWound.shape(world(0.5F, CardinalRitePhase.ORDEAL, RiteWorldMath.Ending.NONE, 0), 1);
		assertEquals(1.0F, formed.presence(), 1e-6F);
		assertEquals(8.0F, formed.radius(), 1e-6F);
		assertEquals(0.0F, formed.grow(), 1e-6F);

		RiteSkyWound.Shape grown = RiteSkyWound.shape(world(1.0F, CardinalRitePhase.ORDEAL, RiteWorldMath.Ending.NONE, 0), 1);
		assertEquals(16.0F, grown.radius(), 1e-6F);
		assertEquals(1.0F, grown.grow(), 1e-6F);
	}

	@Test
	void itOpensOnlyAtCulmination() {
		assertEquals(0.0F, RiteSkyWound.shape(world(1.0F, CardinalRitePhase.OFFERING_PROCESSION,
				RiteWorldMath.Ending.NONE, 0), 1).open());
		assertEquals(1.0F, RiteSkyWound.shape(world(1.0F, CardinalRitePhase.CULMINATION,
				RiteWorldMath.Ending.NONE, 0), 1).open(), 1e-6F);
	}

	@Test
	void completionClosesItBehindAFlash() {
		RiteSkyWound.Shape closing = RiteSkyWound.shape(world(1.0F, CardinalRitePhase.CULMINATION,
				RiteWorldMath.Ending.COMPLETED, 0), 1);
		assertEquals(1.0F, closing.flash(), 1e-6F);
		assertEquals(16.0F, closing.radius(), 1e-6F);
		assertNull(RiteSkyWound.shape(world(1.0F, CardinalRitePhase.CULMINATION, RiteWorldMath.Ending.COMPLETED, 14), 1));
	}

	@Test
	void aCollapseShuddersItShut() {
		assertNotNull(RiteSkyWound.shape(world(1.0F, CardinalRitePhase.ORDEAL, RiteWorldMath.Ending.COLLAPSED, 2), 1));
		assertNull(RiteSkyWound.shape(world(1.0F, CardinalRitePhase.ORDEAL, RiteWorldMath.Ending.COLLAPSED, 30), 1));
	}
}
