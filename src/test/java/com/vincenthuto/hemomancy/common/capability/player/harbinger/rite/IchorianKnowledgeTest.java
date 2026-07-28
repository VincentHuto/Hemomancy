package com.vincenthuto.hemomancy.common.capability.player.harbinger.rite;

import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

final class IchorianKnowledgeTest {
	private static final ResourceLocation RESERVOIR =
			ResourceLocation.fromNamespaceAndPath("hemomancy", "reservoir");

	@Test
	void canonicalNodesPersistPartiallyAndUnlockOnlyAtCompletion() {
		IchorianKnowledge knowledge = new IchorianKnowledge();
		assertFalse(knowledge.recordNode(RESERVOIR, 0, 4));
		assertFalse(knowledge.recordNode(RESERVOIR, 2, 4));
		assertEquals(2, knowledge.discoveredNodeCount(RESERVOIR));
		assertFalse(knowledge.isKnown(RESERVOIR));

		IchorianKnowledge copy = new IchorianKnowledge();
		copy.deserializeNBT(null, knowledge.serializeNBT(null));
		assertEquals(2, copy.discoveredNodeCount(RESERVOIR));
		assertFalse(copy.recordNode(RESERVOIR, 1, 4));
		assertTrue(copy.recordNode(RESERVOIR, 3, 4));
		assertTrue(copy.isKnown(RESERVOIR));
		assertEquals(1.0D, copy.discoveryProgress(RESERVOIR, 4));
	}

	@Test
	void invalidIndicesNeverBecomeKnowledge() {
		IchorianKnowledge knowledge = new IchorianKnowledge();
		assertFalse(knowledge.recordNode(RESERVOIR, -1, 4));
		assertFalse(knowledge.recordNode(RESERVOIR, 4, 4));
		assertEquals(0, knowledge.discoveredNodeCount(RESERVOIR));
	}
}
