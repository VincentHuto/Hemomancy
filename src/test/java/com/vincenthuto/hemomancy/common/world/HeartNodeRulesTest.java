package com.vincenthuto.hemomancy.common.world;

import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HeartNodeRulesTest {
	@Test
	void onlyAnApotheosPlantsAndOnlyOnTheGardensNetwork() {
		assertTrue(HeartNodeRules.mayPlant(true, true, true));
		assertFalse(HeartNodeRules.mayPlant(false, true, true));
		assertFalse(HeartNodeRules.mayPlant(true, false, true), "the Overworld biome is not the Gardens");
		assertFalse(HeartNodeRules.mayPlant(true, true, false), "only on network ground");
	}

	@Test
	void theFootprintIsACylinderAroundTheNode() {
		BlockPos node = new BlockPos(100, 70, -40);
		assertTrue(HeartNodeRules.inFootprint(node, node));
		assertTrue(HeartNodeRules.inFootprint(node, node.offset(8, 6, 0)));
		assertTrue(HeartNodeRules.inFootprint(node, node.offset(5, -6, 5)));
		assertFalse(HeartNodeRules.inFootprint(node, node.offset(9, 0, 0)));
		assertFalse(HeartNodeRules.inFootprint(node, node.offset(6, 0, 6)), "outside the radius on the diagonal");
		assertFalse(HeartNodeRules.inFootprint(node, node.offset(0, 7, 0)));
	}

	@Test
	void deathDropsAreCachedOnlyForAnApotheosWithANodeAndWithoutKeepInventory() {
		assertTrue(HeartNodeRules.shouldCache(true, false, true));
		assertFalse(HeartNodeRules.shouldCache(false, false, true));
		assertFalse(HeartNodeRules.shouldCache(true, true, true), "keepInventory already keeps everything");
		assertFalse(HeartNodeRules.shouldCache(true, false, false));
	}

	@Test
	void onlyTheOwnerOpensACache() {
		UUID owner = UUID.randomUUID();
		assertTrue(HeartNodeRules.mayOpen(owner, owner));
		assertFalse(HeartNodeRules.mayOpen(owner, UUID.randomUUID()));
		assertFalse(HeartNodeRules.mayOpen(null, owner));
	}

	@Test
	void eightRibsStandOnTheRingAroundTheNode() {
		BlockPos node = BlockPos.ZERO;
		Set<BlockPos> ribs = new HashSet<>();
		for (int rib = 0; rib < HeartNodeRules.RIBS; rib++) {
			BlockPos base = HeartNodeRules.ribBase(node, rib);
			double distance = Math.sqrt(base.getX() * base.getX() + base.getZ() * base.getZ());
			assertTrue(Math.abs(distance - HeartNodeRules.RIB_RADIUS) < 1.0, "rib " + rib + " at " + base);
			assertEquals(0, base.getY());
			ribs.add(base);
		}
		assertEquals(HeartNodeRules.RIBS, ribs.size(), "every rib has its own spot");
	}
}
