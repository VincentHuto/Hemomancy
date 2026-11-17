package com.vincenthuto.hemomancy.common.world;

import com.vincenthuto.hemomancy.support.ResourceReader;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HeartNodeSavedDataTest {
	private final UUID alice = UUID.randomUUID();
	private final UUID bob = UUID.randomUUID();
	private static final ResourceKey<Level> OVERWORLD = ResourceKey.create(Registries.DIMENSION,
			ResourceLocation.withDefaultNamespace("overworld"));

	@Test
	void aNodeCannotBePlantedWhereItsHeartLobeWouldTouchAnotherOwnersEnvelope() {
		HeartNodeSavedData data = new HeartNodeSavedData();
		data.plant(alice, BlockPos.ZERO, 0L);
		double reach = HeartNodeRules.HEART_RADIUS;
		assertTrue(data.wouldOverlap(bob, new BlockPos(24, 0, 0), reach), "heart lobes would touch");
		assertFalse(data.wouldOverlap(bob, new BlockPos(25, 0, 0), reach));
		assertFalse(data.wouldOverlap(alice, new BlockPos(4, 0, 0), reach), "an owner may move their own node");

		assertTrue(data.addSynapse(alice, new BlockPos(20, 0, 0)));
		assertTrue(data.wouldOverlap(bob, new BlockPos(40, 0, 0), reach), "a synapse's lobe is sanctum too");
		assertFalse(data.wouldOverlap(bob, new BlockPos(41, 0, 0), reach));
	}

	@Test
	void anyForeignEnvelopeRefusesAStranger() {
		HeartNodeSavedData data = new HeartNodeSavedData();
		data.plant(alice, BlockPos.ZERO, 0L);
		assertTrue(data.strangerTo(bob, new BlockPos(8, 0, 8)));
		assertFalse(data.strangerTo(alice, new BlockPos(8, 0, 8)));
		assertFalse(data.strangerTo(bob, new BlockPos(13, 0, 0)));
		assertFalse(data.strangerTo(bob, new BlockPos(0, 9, 0)), "the layer above is not the sanctum");
		data.addSynapse(alice, new BlockPos(18, 0, 0));
		assertTrue(data.strangerTo(bob, new BlockPos(25, 0, 0)), "the synapse's lobe is private too");
		assertEquals(alice, data.footprintAt(new BlockPos(25, 0, 0)).orElseThrow().owner());
		assertTrue(data.footprintAt(new BlockPos(27, 0, 0)).isEmpty());
	}

	@Test
	void aStrangerOnTheHeartGetsNothingAndTheOwnersShareTapers() {
		HeartNodeSavedData data = new HeartNodeSavedData();
		BlockPos heart = new BlockPos(0, 64, 0);
		data.plant(alice, heart, 0L);
		data.addSynapse(alice, heart.offset(16, 0, 0));
		assertEquals(0.0D, data.ownerStrength(bob, heart), "no benefit for anyone but the planter");
		assertFalse(data.insideOwnEnvelope(bob, heart), "no cache for a stranger");
		assertEquals(1.0D, data.ownerStrength(alice, heart), 1e-9);
		assertEquals(0.5D, data.ownerStrength(alice, heart.offset(24, 0, 0)), 1e-9);
		assertEquals(0.0D, data.ownerStrength(alice, heart.offset(25, 0, 0)), "it stops at the edge");
		assertEquals(0.0D, data.ownerStrength(alice, heart.offset(0, 9, 0)), "and above the band");
		assertTrue(data.insideOwnEnvelope(alice, heart.offset(24, 0, 0)));
		assertFalse(data.insideOwnEnvelope(alice, heart.offset(0, -9, 0)));

		data.plant(bob, heart.offset(100, 0, 0), 0L);
		assertEquals(0.0D, data.ownerStrength(bob, heart), "owning a node elsewhere changes nothing here");
	}

	@Test
	void synapsesChainWithinTheBudgetAndNeverIntoAnotherSanctum() {
		HeartNodeSavedData data = new HeartNodeSavedData();
		assertFalse(data.addSynapse(alice, BlockPos.ZERO), "no node, no synapse");
		data.plant(alice, BlockPos.ZERO, 0L);
		data.plant(bob, new BlockPos(50, 0, 0), 0L);
		assertTrue(data.addSynapse(alice, new BlockPos(20, 0, 0)));
		assertFalse(data.addSynapse(alice, new BlockPos(34, 0, 0)), "its lobe would touch bob's heart lobe");
		assertFalse(data.addSynapse(alice, new BlockPos(0, 0, 40)), "does not chain");
		assertTrue(data.addSynapse(alice, new BlockPos(0, 0, 20)));
		assertTrue(data.removeSynapse(alice, new BlockPos(0, 0, 20)));
		assertFalse(data.removeSynapse(alice, new BlockPos(0, 0, 20)));
		assertEquals(List.of(new BlockPos(20, 0, 0)), data.entry(alice).synapses());
	}

	/** DS-2: taking out a middle synapse prunes the one beyond it, which no longer chains back to the heart. */
	@Test
	void removingAMiddleSynapsePrunesTheOnesItCutsOff() {
		HeartNodeSavedData data = new HeartNodeSavedData();
		data.plant(alice, BlockPos.ZERO, 0L);
		BlockPos near = new BlockPos(18, 0, 0);
		BlockPos middle = new BlockPos(32, 0, 0);
		BlockPos outer = new BlockPos(46, 0, 0);
		assertTrue(data.addSynapse(alice, near));
		assertTrue(data.addSynapse(alice, middle));
		assertTrue(data.addSynapse(alice, outer));
		assertTrue(data.removeSynapse(alice, middle));
		assertEquals(List.of(near), data.entry(alice).synapses(), "the cut-off outer synapse leaves the envelope");
		assertFalse(data.insideOwnEnvelope(alice, outer), "its lobe is gone");
		assertTrue(data.removeSynapse(alice, near));
		assertEquals(List.of(), data.entry(alice).synapses());
	}

	@Test
	void replantingOrLosingTheNodeForgetsItsSynapses() {
		HeartNodeSavedData data = new HeartNodeSavedData();
		data.plant(alice, BlockPos.ZERO, 100L);
		data.addSynapse(alice, new BlockPos(20, 0, 0));
		HeartNodeEntry old = data.plant(alice, new BlockPos(200, 0, 0), 500L).orElseThrow();
		assertEquals(List.of(new BlockPos(20, 0, 0)), old.synapses(), "the caller learns which synapses went inert");
		assertEquals(List.of(), data.entry(alice).synapses());
		assertEquals(500L, data.entry(alice).plantedAt());
		assertTrue(data.plant(alice, new BlockPos(200, 0, 0), 600L).isEmpty(), "the same spot replaces nothing");

		data.addSynapse(alice, new BlockPos(220, 0, 0));
		assertTrue(data.remove(alice, new BlockPos(5, 0, 5)).isEmpty(), "only the node's own spot removes it");
		assertEquals(List.of(new BlockPos(220, 0, 0)), data.remove(alice, new BlockPos(200, 0, 0))
				.orElseThrow().synapses());
		assertNull(data.entry(alice));
		assertFalse(data.strangerTo(bob, new BlockPos(220, 0, 0)));
	}

	@Test
	void onlyAForgottenNodesSpawnIsCleared() {
		HeartNodeSavedData data = new HeartNodeSavedData();
		BlockPos node = new BlockPos(5, 70, 5);
		data.plant(alice, node, 0L);
		data.remove(alice, node);
		assertFalse(data.takeRemovedSpawn(alice, new BlockPos(0, 80, 0)), "another forced spawn is left alone");
		assertTrue(data.takeRemovedSpawn(alice, node.above()));
		assertFalse(data.takeRemovedSpawn(alice, node.above()), "the record is used once");
	}

	/** PT-80: the node keeps the respawn it took over, through a replant, a save and its own loss. */
	@Test
	void theRespawnBeforeTheNodeIsRememberedAndGivenBack() {
		HeartNodeSavedData data = new HeartNodeSavedData();
		PriorRespawn bed = new PriorRespawn(OVERWORLD, new BlockPos(10, 64, -3), 90f, false);
		BlockPos first = new BlockPos(5, 70, 5);
		data.plant(alice, first, 0L, bed);
		assertEquals(bed, data.entry(alice).priorRespawn());

		BlockPos second = new BlockPos(300, 70, 5);
		data.plant(alice, second, 10L, new PriorRespawn(OVERWORLD, first.above(), 0f, true));
		assertEquals(bed, data.entry(alice).priorRespawn(), "a replant keeps the bed, not the old node's spawn");

		HeartNodeSavedData back = HeartNodeSavedData.load(data.save(new CompoundTag(), null), null);
		assertEquals(bed, back.entry(alice).priorRespawn(), "the bed survives the save");
		back.remove(alice, second);
		HeartNodeSavedData afterLoss = HeartNodeSavedData.load(back.save(new CompoundTag(), null), null);
		assertEquals(bed, afterLoss.takeRemovedSpawnRecord(alice, second.above()).orElseThrow().prior(),
				"the lost node gives the bed back at the next login");

		data.plant(bob, first, 0L, null);
		assertNull(data.entry(bob).priorRespawn(), "a player with no bed remembers none");
		data.remove(bob, first);
		data.plant(bob, second, 0L, new PriorRespawn(OVERWORLD, first.above(), 0f, true));
		assertNull(data.entry(bob).priorRespawn(), "replanting before the login keeps the lost node's answer, not its spawn");
	}

	@Test
	void aSaveFromBeforeSynapsesLoadsAsANodeWithNoneAndDefaultSettings() {
		BlockPos node = new BlockPos(-120, 44, 310);
		BlockPos lost = new BlockPos(7, 50, 7);
		CompoundTag old = new CompoundTag();
		ListTag nodes = new ListTag();
		CompoundTag entry = new CompoundTag();
		entry.putUUID("Owner", alice);
		entry.put("Pos", NbtUtils.writeBlockPos(node));
		nodes.add(entry);
		old.put("Nodes", nodes);
		ListTag removed = new ListTag();
		CompoundTag gone = new CompoundTag();
		gone.putUUID("Owner", bob);
		gone.put("Pos", NbtUtils.writeBlockPos(lost));
		removed.add(gone);
		old.put("Removed", removed);

		HeartNodeSavedData data = HeartNodeSavedData.load(old, null);
		HeartNodeEntry loaded = data.entry(alice);
		assertEquals(node, loaded.heart());
		assertEquals(List.of(), loaded.synapses());
		assertEquals(HeartNodeSettings.DEFAULT, loaded.settings());
		assertEquals(HeartNodeRules.MATURE_STAGE, loaded.stage(0L), "an old node is grown, even on a young clock");
		assertTrue(data.takeRemovedSpawn(bob, lost.above()), "the respawn clear survives the load");
	}

	@Test
	void entriesRoundTripThroughTheSave() {
		HeartNodeSavedData data = new HeartNodeSavedData();
		data.plant(alice, BlockPos.ZERO, 1234L);
		data.addSynapse(alice, new BlockPos(20, 0, 0));
		data.addSynapse(alice, new BlockPos(30, -6, 10));
		HeartNodeSettings quiet = new HeartNodeSettings(HeartNodeSettings.SPREAD, 0);
		data.setSettings(alice, quiet);

		HeartNodeEntry back = HeartNodeSavedData.load(data.save(new CompoundTag(), null), null).entry(alice);
		assertEquals(data.entry(alice), back);
		assertEquals(List.of(new BlockPos(20, 0, 0), new BlockPos(30, -6, 10)), back.synapses(), "order is kept");
		assertEquals(quiet, back.settings());
	}

	@Test
	void waitingCachesAreKeptPerOwnerAndOutliveTheNode() {
		HeartNodeSavedData data = new HeartNodeSavedData();
		data.plant(alice, BlockPos.ZERO, 0L);
		BlockPos first = new BlockPos(3, 1, 4);
		BlockPos second = new BlockPos(-6, 0, 2);
		data.addCache(alice, first);
		data.addCache(alice, second);
		data.addCache(alice, first);
		data.addCache(bob, new BlockPos(90, 0, 90));
		assertEquals(List.of(first, second), data.caches(alice), "oldest first, each once");

		data.remove(alice, BlockPos.ZERO);
		HeartNodeSavedData back = HeartNodeSavedData.load(data.save(new CompoundTag(), null), null);
		assertEquals(List.of(first, second), back.caches(alice), "a lost node leaves its owner's gear waiting");
		assertEquals(List.of(new BlockPos(90, 0, 90)), back.caches(bob));

		assertTrue(back.removeCache(first));
		assertFalse(back.removeCache(first));
		assertEquals(List.of(second), back.caches(alice));
		assertTrue(back.removeCache(new BlockPos(90, 0, 90)));
		assertEquals(List.of(), back.caches(bob));
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
