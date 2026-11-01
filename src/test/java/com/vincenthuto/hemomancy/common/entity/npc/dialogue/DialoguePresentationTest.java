package com.vincenthuto.hemomancy.common.entity.npc.dialogue;

import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DialoguePresentationTest {
	@Test
	void hubPresentationAlwaysExposesFourOrderedCategories() {
		DialoguePresentation presentation = DialoguePresentation.hub(
				id("alchemist"), id("alchemist"), List.of(
						DialogueTopic.available("machines", DialogueCategory.CONVERSATION,
								"topic.machines", "topic.machines.summary", "machines")));

		assertEquals(List.of(DialogueCategory.QUESTS, DialogueCategory.INQUIRIES,
				DialogueCategory.CONVERSATION, DialogueCategory.TRADE), presentation.categories());
		assertTrue(presentation.hasTopics(DialogueCategory.CONVERSATION));
		assertFalse(presentation.hasTopics(DialogueCategory.QUESTS));
		assertFalse(presentation.hasTopics(DialogueCategory.TRADE), "the Trade box stays empty until it has cards");
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
		DialogueTopic lore = new DialogueTopic("conversation/history", DialogueCategory.CONVERSATION,
				"title", "summary", "history", DialogueHubFactory.LORE_ICON, DialogueTopicState.AVAILABLE,
				null, null, null, true, DialogueAttention.NOTICE, true);
		DialogueTree original = DialogueTree.builder("speaker", id("portrait"), 7)
				.presentation(DialoguePresentation.hub(id("alchemist"), id("alchemist"), List.of(topic, lore)))
				.addNode(new DialogueNode("root", List.of("line"), List.of(
						new DialogueOption("claim", null, "event",
								new DialogueOptionPresentation(id("icons/reward"), "detail", true,
										DialogueOptionStyle.EMPHASIZED, DialogueAttention.URGENT,
										"reward.prompt")),
						new DialogueOption("history", "history", null).withLore(),
						new DialogueOption("trial", null, "trial").withQuest())))
				.build();
		FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
		original.toNetwork(buffer);

		DialogueTree restored = DialogueTree.fromNetwork(buffer);

		assertEquals(original.presentation(), restored.presentation());
		assertEquals(original.getStartNode().options(), restored.getStartNode().options());
		assertEquals("lore/history", restored.presentation().topics().get(1).readPath(),
				"lore cards keep the old Lore box read path");
	}

	private static ResourceLocation id(String path) {
		return ResourceLocation.fromNamespaceAndPath("hemomancy", path);
	}
}
