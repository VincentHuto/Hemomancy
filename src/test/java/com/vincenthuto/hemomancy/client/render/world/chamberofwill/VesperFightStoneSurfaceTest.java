package com.vincenthuto.hemomancy.client.render.world.chamberofwill;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class VesperFightStoneSurfaceTest {
	private static final VesperFightArenaLayout.Tile TILE = new VesperFightArenaLayout.Tile(
			3, -7, VesperFightArenaLayout.Material.BONE_BRICK,
			VesperFightArenaLayout.SurfaceVariant.CHIPPED,
			false, false, 0.0F, 0.0F, 0.0F, 0.01F);
	private static final VesperFightArenaLayout.HorizonTile HORIZON_TILE =
			new VesperFightArenaLayout.HorizonTile(41, -29, VesperFightArenaLayout.Material.BASALT,
					-0.8F, 0.02F, 140, VesperFightArenaLayout.HorizonDamage.FRACTURED, 2);

	@Test
	void reliefReturnsToZeroAlongEveryStoneEdge() {
		for (int point = 0; point <= 4; point++) {
			assertEquals(0.0F, VesperFightStoneSurface.relief(TILE, 0, point), 0.000001F);
			assertEquals(0.0F, VesperFightStoneSurface.relief(TILE, 4, point), 0.000001F);
			assertEquals(0.0F, VesperFightStoneSurface.relief(TILE, point, 0), 0.000001F);
			assertEquals(0.0F, VesperFightStoneSurface.relief(TILE, point, 4), 0.000001F);
		}
	}

	@Test
	void subdividedInteriorHasDeterministicBoundedHeightAndToneVariation() {
		Set<Integer> tones = new HashSet<>();
		boolean visibleRelief = false;
		for (int gridX = 1; gridX < 4; gridX++) {
			for (int gridZ = 1; gridZ < 4; gridZ++) {
				float relief = VesperFightStoneSurface.relief(TILE, gridX, gridZ);
				assertEquals(relief, VesperFightStoneSurface.relief(TILE, gridX, gridZ), 0.0F);
				assertTrue(Math.abs(relief) <= 0.015F);
				visibleRelief |= Math.abs(relief) >= 0.002F;
				int tone = VesperFightStoneSurface.tone(TILE, gridX, gridZ);
				assertTrue(tone >= -7 && tone <= 7);
				tones.add(tone);
			}
		}
		assertTrue(visibleRelief, "the interior should not remain a perfectly flat plane");
		assertTrue(tones.size() >= 4, "the surface should have restrained mottled shading");
	}

	@Test
	void convertedHorizonTilesReceiveTheSameDeterministicSurfaceVariationAsInteriorTiles() {
		VesperFightArenaLayout.Tile extendedTile = HORIZON_TILE.asInteriorTile();
		Set<Integer> tones = new HashSet<>();
		boolean visibleRelief = false;
		for (int gridX = 1; gridX < 4; gridX++) {
			for (int gridZ = 1; gridZ < 4; gridZ++) {
				float relief = VesperFightStoneSurface.relief(extendedTile, gridX, gridZ);
				assertEquals(relief, VesperFightStoneSurface.relief(extendedTile, gridX, gridZ), 0.0F);
				assertTrue(Math.abs(relief) <= 0.015F);
				visibleRelief |= Math.abs(relief) >= 0.002F;
				int tone = VesperFightStoneSurface.tone(extendedTile, gridX, gridZ);
				assertTrue(tone >= -7 && tone <= 7);
				tones.add(tone);
			}
		}
		assertTrue(visibleRelief, "high-resolution horizon stones should not remain perfectly flat");
		assertTrue(tones.size() >= 4, "high-resolution horizon stones should retain mottled shading");
	}

	@Test
	void tileSlabBottomAlwaysStaysBelowItsLowestTopCorner() {
		float bottom = VesperFightStoneSurface.slabBottom(-0.82F, -0.75F, -0.91F, -0.79F);

		assertTrue(bottom < -0.91F);
		assertEquals(-0.965F, bottom, 0.000001F);
	}
}
