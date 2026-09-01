package com.vincenthuto.hemomancy.common.capability.player.shared.knowledge;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DialogueKnowledgeTest {
	@Test
	void readTopicsAreIdempotentAndPersisted() {
		DialogueKnowledge knowledge = new DialogueKnowledge();
		ResourceLocation topic = ResourceLocation.fromNamespaceAndPath("hemomancy", "alchemist/lore/vial_centrifuge");

		assertTrue(knowledge.markRead(topic));
		assertFalse(knowledge.markRead(topic));

		CompoundTag tag = knowledge.serializeNBT(null);
		DialogueKnowledge restored = new DialogueKnowledge();
		restored.deserializeNBT(null, tag);

		assertTrue(restored.hasRead(topic));
	}
}
