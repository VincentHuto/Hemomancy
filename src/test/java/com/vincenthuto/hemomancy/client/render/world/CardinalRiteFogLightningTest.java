package com.vincenthuto.hemomancy.client.render.world;

import com.vincenthuto.hemomancy.client.data.ActiveRiteClientData;
import com.vincenthuto.hutoslib.common.lightning.LightningTestConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class CardinalRiteFogLightningTest {
	@Test
	void activeHarbingerRiteSchedulesOneStrikeWithoutRepeatingDuringTheSameTick() {
		CardinalRiteFogLightningSchedule schedule = new CardinalRiteFogLightningSchedule();
		ActiveRiteClientData.RiteEntry rite = rite(false);

		for (long tick = 0; tick < 6; tick++) {
			assertTrue(schedule.update(List.of(rite), tick).isEmpty());
		}
		assertEquals(1, schedule.update(List.of(rite), 6L).size());
		assertTrue(schedule.update(List.of(rite), 6L).isEmpty());
	}

	@Test
	void unstainedRitesNeverSchedulePurpleLightning() {
		CardinalRiteFogLightningSchedule schedule = new CardinalRiteFogLightningSchedule();

		for (long tick = 0; tick < 80; tick++) {
			assertTrue(schedule.update(List.of(rite(true)), tick).isEmpty());
		}
	}

	@Test
	void strikeArcsCloudToCloudInsideTheFogPerimeter() {
		float radius = 8.0F;
		CardinalRiteFogLightning.StrikeGeometry strike =
				CardinalRiteFogLightning.geometry(BlockPos.ZERO, radius, 91L);

		assertWithinFogBand(strike.start(), radius);
		assertWithinFogBand(strike.end(), radius);
		assertTrue(strike.start().y >= 0.32D && strike.start().y <= 1.12D);
		assertTrue(strike.end().y >= 0.32D && strike.end().y <= 1.12D);
		assertTrue(Math.abs(strike.start().y - strike.end().y) <= 0.28D);
		double horizontalSpan = Math.sqrt(
				Math.pow(strike.start().x - strike.end().x, 2.0D)
						+ Math.pow(strike.start().z - strike.end().z, 2.0D));
		assertTrue(horizontalSpan >= 0.65D,
				"cloud-to-cloud arc is too vertical or cramped: " + horizontalSpan);
	}

	@Test
	void strikeUsesTheIncorrectSigilBlackAndPurpleBoltPalette() {
		LightningTestConfig config = CardinalRiteFogLightning.config(19L, 1.1D);

		assertEquals(LightningTestConfig.Backend.BOLT, config.backend());
		assertEquals(0xE806020A, config.outerColor());
		assertEquals(0xFF5A167D, config.innerColor());
		assertTrue(config.fixedSeed());
		assertEquals(19L, config.seed());
	}

	private static void assertWithinFogBand(Vec3 point, float radius) {
		double dx = point.x - 0.5D;
		double dz = point.z - 0.5D;
		double horizontalRadius = Math.sqrt(dx * dx + dz * dz);
		assertTrue(Math.abs(horizontalRadius - radius) <= 0.75D,
				"strike point escaped fog band: " + horizontalRadius);
	}

	private static ActiveRiteClientData.RiteEntry rite(boolean unstained) {
		return new ActiveRiteClientData.RiteEntry(
				BlockPos.ZERO, 3, 0.0D,
				ResourceLocation.fromNamespaceAndPath("hemomancy", "fog_lightning_test"),
				unstained);
	}
}
