package com.vincenthuto.hemomancy.common.world;

import com.vincenthuto.hemomancy.support.ResourceReader;
import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HeartNodeSavedDataTest {
	private final UUID alice = UUID.randomUUID();
	private final UUID bob = UUID.randomUUID();

	@Test
	void aNodeCannotBePlantedWhereItsSanctumWouldOverlapAnothers() {
		HeartNodeSavedData data = new HeartNodeSavedData();
		data.plant(alice, BlockPos.ZERO);
		assertTrue(data.crowds(bob, new BlockPos(10, 0, 0)), "footprints would overlap");
		assertFalse(data.crowds(bob, new BlockPos(17, 0, 0)));
		assertFalse(data.crowds(alice, new BlockPos(4, 0, 0)), "an owner may move their own node");
	}

	@Test
	void anyForeignFootprintRefusesAStranger() {
		HeartNodeSavedData data = new HeartNodeSavedData();
		data.plant(alice, BlockPos.ZERO);
		assertTrue(data.strangerTo(bob, new BlockPos(3, 0, 3)));
		assertFalse(data.strangerTo(alice, new BlockPos(3, 0, 3)));
		assertFalse(data.strangerTo(bob, new BlockPos(30, 0, 0)));
	}

	@Test
	void onlyAForgottenNodesSpawnIsCleared() {
		HeartNodeSavedData data = new HeartNodeSavedData();
		BlockPos node = new BlockPos(5, 70, 5);
		data.plant(alice, node);
		data.remove(alice, node);
		assertFalse(data.takeRemovedSpawn(alice, new BlockPos(0, 80, 0)), "another forced spawn is left alone");
		assertTrue(data.takeRemovedSpawn(alice, node.above()));
		assertFalse(data.takeRemovedSpawn(alice, node.above()), "the record is used once");
	}

	@Test
	void theCacheAndNodeAreImmuneToTheDragonAndTheWither() {
		for (String tag : new String[] {"wither_immune", "dragon_immune"}) {
			String text = ResourceReader.text("data/minecraft/tags/block/" + tag + ".json");
			assertTrue(text.contains("hemomancy:fruiting_body_cache"), tag);
			assertTrue(text.contains("hemomancy:heart_node"), tag);
		}
	}
}
