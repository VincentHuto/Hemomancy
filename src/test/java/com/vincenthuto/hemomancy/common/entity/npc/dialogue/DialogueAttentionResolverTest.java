package com.vincenthuto.hemomancy.common.entity.npc.dialogue;

import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DialogueAttentionResolverTest {
	@Test
	void returnsTheStrongestTopicAttention() {
		DialoguePresentation presentation = DialoguePresentation.hub(id("vicar"), id("vicar"), List.of(
				topic("lore", DialogueAttention.NOTICE), topic("reward", DialogueAttention.URGENT)));

		assertEquals(DialogueAttention.URGENT, DialogueAttentionResolver.from(presentation));
		assertEquals("reward", DialogueAttentionResolver.topic(presentation).targetNodeId());
	}

	@Test
	void focusedOrUnmarkedDialogueDoesNotRequestASigil() {
		assertEquals(DialogueAttention.NONE, DialogueAttentionResolver.from(DialoguePresentation.focused()));
		assertEquals(DialogueAttention.NONE, DialogueAttentionResolver.from(
				DialoguePresentation.hub(id("vicar"), id("vicar"), List.of(topic("chat", DialogueAttention.NONE)))));
	}

	private static DialogueTopic topic(String id, DialogueAttention attention) {
		return new DialogueTopic(id, DialogueCategory.LORE, "title", "summary", id, null,
				DialogueTopicState.AVAILABLE, null, null, null, attention != DialogueAttention.NONE, attention);
	}

	private static ResourceLocation id(String path) {
		return ResourceLocation.fromNamespaceAndPath("hemomancy", path);
	}
}
