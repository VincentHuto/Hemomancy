package com.vincenthuto.hemomancy.common.world;

import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HeartNodeFootprintTest {
	private static final BlockPos HEART = new BlockPos(40, 60, -30);
	private final UUID alice = UUID.randomUUID();
	private final UUID bob = UUID.randomUUID();

	private HeartNodeFootprint node(BlockPos... synapses) {
		return new HeartNodeFootprint(alice, HEART, List.of(synapses));
	}

	@Test
	void theHeartLobeIsATwelveBlockCircle() {
		HeartNodeFootprint node = node();
		assertTrue(node.contains(HEART));
		assertTrue(node.contains(HEART.offset(12, 0, 0)));
		assertFalse(node.contains(HEART.offset(13, 0, 0)));
		assertTrue(node.contains(HEART.offset(8, 0, 8)));
		assertFalse(node.contains(HEART.offset(9, 0, 9)), "outside the radius on the diagonal");
	}

	@Test
	void eachLobeHoldsOnlyItsVerticalBand() {
		HeartNodeFootprint node = node(HEART.offset(18, -10, 0));
		assertTrue(node.contains(HEART.offset(0, 8, 0)));
		assertFalse(node.contains(HEART.offset(0, 9, 0)), "the cavern above is not the sanctum");
		assertFalse(node.contains(HEART.offset(0, -9, 0)));
		assertTrue(node.contains(HEART.offset(18, -18, 0)), "a synapse's band follows the synapse");
		assertFalse(node.contains(HEART.offset(22, 0, 0)), "past the heart lobe, and above the synapse's band");
	}

	@Test
	void synapsesExtendTheEnvelopeByEightBlockLobes() {
		BlockPos synapse = HEART.offset(16, 0, 0);
		HeartNodeFootprint node = node(synapse);
		assertTrue(node.contains(HEART.offset(24, 0, 0)));
		assertFalse(node.contains(HEART.offset(25, 0, 0)));
		assertEquals(24.0D, node.maxRadius(), 1e-9);
		assertEquals(12.0D, node().maxRadius(), 1e-9);
	}

	@Test
	void theBenefitTapersToHalfAtTheFarEdgeAndStopsOutside() {
		HeartNodeFootprint node = node(HEART.offset(16, 0, 0));
		assertEquals(1.0D, node.strength(HEART), 1e-9);
		assertEquals(0.75D, node.strength(HEART.offset(12, 0, 0)), 1e-9);
		assertEquals(0.5D, node.strength(HEART.offset(24, 0, 0)), 1e-9);
		assertEquals(0.0D, node.strength(HEART.offset(25, 0, 0)));
		assertEquals(0.0D, node.strength(HEART.offset(0, 9, 0)), "nothing outside the band");
		assertEquals(0.5D, node().strength(HEART.offset(12, 0, 0)), 1e-9);
	}

	@Test
	void synapsesChainByOverlap() {
		HeartNodeFootprint node = node();
		assertTrue(node.canAddSynapse(HEART.offset(20, 0, 0)), "touches the heart lobe");
		assertFalse(node.canAddSynapse(HEART.offset(21, 0, 0)), "too far from the heart");
		assertFalse(node.canAddSynapse(HEART.offset(10, 9, 0)), "outside the heart's band");
		HeartNodeFootprint one = node.withSynapse(HEART.offset(20, 0, 0));
		assertTrue(one.canAddSynapse(HEART.offset(36, 0, 0)), "chains off the synapse");
		assertFalse(one.canAddSynapse(HEART.offset(37, 0, 0)));
		assertTrue(one.canAddSynapse(HEART.offset(36, -8, 0)), "steps down a level along a tunnel");
		assertFalse(one.canAddSynapse(HEART.offset(20, 0, 0)), "a synapse is raised once");
	}

	@Test
	void theBudgetIsSixSynapses() {
		HeartNodeFootprint node = node();
		for (int i = 1; i <= HeartNodeRules.SYNAPSE_BUDGET; i++) {
			BlockPos next = HEART.offset(i * 10, 0, 0);
			assertTrue(node.canAddSynapse(next), "synapse " + i);
			node = node.withSynapse(next);
		}
		assertFalse(node.canAddSynapse(HEART.offset(0, 0, 10)), "the budget is spent");
		assertTrue(node.withoutSynapse(HEART.offset(60, 0, 0)).canAddSynapse(HEART.offset(0, 0, 10)));
		assertFalse(node.withoutHeart().canAddSynapse(HEART), "no heart, no synapses");
		assertEquals(List.of(), node.withoutHeart().synapses());
	}

	@Test
	void anotherOwnersEnvelopeIsNeverTouched() {
		HeartNodeFootprint alices = node(HEART.offset(20, 0, 0));
		assertTrue(alices.wouldOverlap(HEART.offset(40, 0, 0), 12), "touches the synapse lobe");
		assertFalse(alices.wouldOverlap(HEART.offset(41, 0, 0), 12));
		assertTrue(alices.wouldOverlap(HEART.offset(0, 16, 24), 12), "the bands meet");
		assertFalse(alices.wouldOverlap(HEART.offset(0, 17, 0), 12), "a whole layer above");

		HeartNodeFootprint bobs = new HeartNodeFootprint(bob, HEART.offset(40, 0, 0), List.of());
		assertTrue(alices.overlapsAnother(bobs));
		assertTrue(bobs.overlapsAnother(alices));
		assertFalse(alices.overlapsAnother(new HeartNodeFootprint(bob, HEART.offset(41, 0, 0), List.of())));
		assertFalse(alices.overlapsAnother(node(HEART.offset(5, 0, 0))), "an owner never crowds themself");
	}

	@Test
	void connectedSynapsesFollowTheChainBackToTheHeart() {
		BlockPos near = HEART.offset(18, 0, 0);
		BlockPos middle = HEART.offset(32, 0, 0);
		BlockPos outer = HEART.offset(46, 0, 0);
		assertEquals(List.of(near, middle, outer), node(near, middle, outer).connectedSynapses());
		assertEquals(List.of(near), node(near, outer).connectedSynapses(), "the gap cuts the outer lobe off");
		assertEquals(List.of(), node(middle, outer).connectedSynapses(), "nothing reaches the heart");
		assertEquals(List.of(near, middle), node(middle, near).connectedSynapses().stream().sorted(
				java.util.Comparator.comparingInt(BlockPos::getX)).toList(), "found whatever order they were raised");
		assertEquals(List.of(), node(near).withoutHeart().connectedSynapses());
		assertEquals(List.of(), node(HEART.offset(18, 17, 0)).connectedSynapses(), "out of the heart's band");
	}

	@Test
	void eachConnectedSynapsesThreadRunsFromTheLobeItChainsTo() {
		BlockPos near = HEART.offset(18, 0, 0);
		BlockPos middle = HEART.offset(32, 0, 0);
		BlockPos outer = HEART.offset(46, 0, 0);
		HeartNodeFootprint chain = node(near, middle, outer);
		assertEquals(HEART, chain.chainedFrom(near));
		assertEquals(near, chain.chainedFrom(middle));
		assertEquals(middle, chain.chainedFrom(outer));
		assertEquals(near, node(middle, near).chainedFrom(middle), "raised before its link, it still threads to it");
		assertEquals(null, node(near, outer).chainedFrom(outer), "a cut-off synapse has no thread");
		assertEquals(null, chain.chainedFrom(HEART.offset(5, 0, 5)), "not a synapse of this sanctum");
	}
}
