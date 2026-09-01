package com.vincenthuto.hemomancy.common.network.dialogue;

import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DialogueTopicOpenedPacketTest {
	@Test
	void acceptsNamespacedDialogueTopicIds() {
		assertTrue(DialogueTopicOpenedPacket.isValidTopicId(
				ResourceLocation.fromNamespaceAndPath("hemomancy", "alchemist/lore/vial_centrifuge")));
	}

	@Test
	void rejectsReservedOrOversizedTopicIds() {
		assertFalse(DialogueTopicOpenedPacket.isValidTopicId(
				ResourceLocation.fromNamespaceAndPath("minecraft", "alchemist/lore/topic")));
		assertFalse(DialogueTopicOpenedPacket.isValidTopicId(
				ResourceLocation.fromNamespaceAndPath("hemomancy", "x".repeat(129))));
	}
}
