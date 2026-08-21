package com.vincenthuto.hemomancy.common.entity.npc.dialogue;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;

import net.minecraft.resources.ResourceLocation;

class DialoguePresentationTest {
	@Test
	void hubPresentationAlwaysExposesFourOrderedCategories() {
		DialoguePresentation presentation = DialoguePresentation.hub(
				id("alchemist"), id("alchemist"), List.of(
						DialogueTopic.available("machines", DialogueCategory.LORE,
								"topic.machines", "topic.machines.summary", "machines")));

		assertEquals(List.of(DialogueCategory.QUESTS, DialogueCategory.INQUIRIES,
				DialogueCategory.LORE, DialogueCategory.CONVERSATION), presentation.categories());
		assertTrue(presentation.hasTopics(DialogueCategory.LORE));
		assertFalse(presentation.hasTopics(DialogueCategory.QUESTS));
	}

	@Test
	void legacyTreeConstructorDefaultsToFocusedPresentation() {
		DialogueTree tree = DialogueTree.builder("speaker", id("textures/gui/mystery_speaker.png"), 0)
				.addNode(new DialogueNode("root", List.of("line"), List.of()))
				.build();

		assertEquals(DialogueScreenMode.FOCUSED, tree.presentation().mode());
	}

	@Test
	void disabledOptionCannotBeSelected() {
		DialogueOption option = new DialogueOption("option", "next", null,
				DialogueOptionPresentation.disabled("option.locked"));

		assertFalse(option.presentation().enabled());
		assertEquals(DialogueOptionStyle.NORMAL, option.presentation().style());
	}

	@Test
	void treePresentationRoundTripsAcrossNetwork() {
		DialogueTopic topic = new DialogueTopic("quests/sample", DialogueCategory.QUESTS,
				"title", "summary", "quest", id("icons/quest"), DialogueTopicState.TURN_IN,
				"assignments", new DialogueProgress(3, 3, "reward"), id("sample_item"), true,
				DialogueAttention.URGENT);
		DialogueTree original = DialogueTree.builder("speaker", id("portrait"), 7)
				.presentation(DialoguePresentation.hub(id("alchemist"), id("alchemist"), List.of(topic)))
				.addNode(new DialogueNode("root", List.of("line"), List.of(
						new DialogueOption("claim", null, "event",
								new DialogueOptionPresentation(id("icons/reward"), "detail", true,
										DialogueOptionStyle.EMPHASIZED, DialogueAttention.URGENT)))))
				.build();
		FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
		original.toNetwork(buffer);

		DialogueTree restored = DialogueTree.fromNetwork(buffer);

		assertEquals(original.presentation(), restored.presentation());
		assertEquals(original.getStartNode().options(), restored.getStartNode().options());
	}

	private static ResourceLocation id(String path) {
		return ResourceLocation.fromNamespaceAndPath("hemomancy", path);
	}
}
