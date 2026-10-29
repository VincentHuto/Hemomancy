package com.vincenthuto.hemomancy.common.world;

import net.minecraft.core.Direction;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GardensTerrainRulesTest {
	@Test
	void onlyAnApotheosBridgesAndOnlyInTheDimensionWithAnEmptyHand() {
		assertTrue(GardensTerrainRules.mayBridge(true, true, true));
		assertFalse(GardensTerrainRules.mayBridge(false, true, true));
		assertFalse(GardensTerrainRules.mayBridge(true, false, true), "the Overworld biome's caps stay caps");
		assertFalse(GardensTerrainRules.mayBridge(true, true, false));
	}

	@Test
	void aBridgeGrowsFromTheClickedSideOrTheWayThePlayerFaces() {
		assertEquals(Direction.EAST, GardensTerrainRules.bridgeDirection(Direction.EAST, Direction.NORTH));
		assertEquals(Direction.NORTH, GardensTerrainRules.bridgeDirection(Direction.UP, Direction.NORTH));
		assertEquals(Direction.SOUTH, GardensTerrainRules.bridgeDirection(Direction.DOWN, Direction.SOUTH));
		assertEquals(16, GardensTerrainRules.BRIDGE_REACH);
		assertEquals(600, GardensTerrainRules.BRIDGE_LIFETIME_TICKS);
	}

	@Test
	void gillsBiteStrangersOnly() {
		assertTrue(GardensTerrainRules.gillSnaps(false, false, true));
		assertFalse(GardensTerrainRules.gillSnaps(true, false, true), "an Apotheos passes");
		assertFalse(GardensTerrainRules.gillSnaps(false, true, true), "creative and spectators pass");
		assertFalse(GardensTerrainRules.gillSnaps(false, false, false));
		assertEquals(24, GardensTerrainRules.GILL_SPREAD);
		assertEquals(100, GardensTerrainRules.GILL_REOPEN_TICKS);
		assertEquals(3f, GardensTerrainRules.GILL_BITE);
	}
}
