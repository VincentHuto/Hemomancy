package com.vincenthuto.hemomancy.client.render.world.rite.escalation;

import com.vincenthuto.hemomancy.common.rite.CardinalRiteInstability;
import com.vincenthuto.hemomancy.common.rite.CardinalRitePhase;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

final class RiteAirLayerTest {
	private static RiteWorldState.World world(BlockPos center, float level, String fog) {
		return new RiteWorldState.World(center, null, level, level, 0.0F, 2, 4, CardinalRiteInstability.STABLE, 8.0F,
				fog, true, CardinalRitePhase.ORDEAL, RiteFloor.NONE, RiteWorldMath.Ending.NONE, 0.0F);
	}

	@Test
	void strongestRiteWinsInsteadOfSumming() {
		BlockPos storm = new BlockPos(0, 64, 0);
		BlockPos faint = new BlockPos(20, 64, 0);
		Vec3 between = new Vec3(10.5D, 65.0D, 0.5D);

		RiteAirLayer.Choice choice = RiteAirLayer.strongest(
				List.of(world(faint, 1.0F, "faint"), world(storm, 1.0F, "storm")), between, 1.0F);

		assertEquals(storm, choice.world().center());
		assertEquals(0.6F, choice.strength(), 1e-6F, "the stronger rite's own strength, not the sum");
	}

	@Test
	void noAirBeforeTheFirstStepOrFarAway() {
		BlockPos center = new BlockPos(0, 64, 0);

		assertNull(RiteAirLayer.strongest(List.of(world(center, 0.0F, "storm")), new Vec3(0.5D, 65.0D, 0.5D), 1.0F));
		assertNull(RiteAirLayer.strongest(List.of(world(center, 1.0F, "storm")), new Vec3(200.0D, 65.0D, 0.0D), 1.0F));
	}
}
