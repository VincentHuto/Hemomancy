package com.vincenthuto.hemomancy.common.entity.npc.dialogue;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public record DialoguePresentation(ResourceLocation dialogueId, ResourceLocation styleId,
		DialogueScreenMode mode, List<DialogueTopic> topics) {
	private static final List<DialogueCategory> CATEGORIES = List.of(DialogueCategory.QUESTS,
			DialogueCategory.INQUIRIES, DialogueCategory.LORE, DialogueCategory.CONVERSATION);

	public DialoguePresentation {
		topics = List.copyOf(topics);
	}

	public static DialoguePresentation focused() {
		return new DialoguePresentation(ResourceLocation.fromNamespaceAndPath("hemomancy", "focused"),
				ResourceLocation.fromNamespaceAndPath("hemomancy", "default"),
				DialogueScreenMode.FOCUSED, List.of());
	}

	public static DialoguePresentation hub(ResourceLocation dialogueId, ResourceLocation styleId,
			List<DialogueTopic> topics) {
		return new DialoguePresentation(dialogueId, styleId, DialogueScreenMode.TOPIC_HUB, topics);
	}

	public List<DialogueCategory> categories() {
		return CATEGORIES;
	}

	public boolean hasTopics(DialogueCategory category) {
		return topics.stream().anyMatch(topic -> topic.category() == category && topic.state().enabled());
	}

	public List<DialogueTopic> topics(DialogueCategory category) {
		List<DialogueTopic> result = new ArrayList<>();
		for (DialogueTopic topic : topics) if (topic.category() == category) result.add(topic);
		return List.copyOf(result);
	}

	void toNetwork(FriendlyByteBuf buf) {
		buf.writeResourceLocation(dialogueId);
		buf.writeResourceLocation(styleId);
		buf.writeVarInt(mode.ordinal());
		buf.writeVarInt(topics.size());
		for (DialogueTopic topic : topics) topic.toNetwork(buf);
	}

	static DialoguePresentation fromNetwork(FriendlyByteBuf buf) {
		ResourceLocation dialogueId = buf.readResourceLocation();
		ResourceLocation styleId = buf.readResourceLocation();
		DialogueScreenMode mode = DialogueScreenMode.fromOrdinal(buf.readVarInt());
		int size = buf.readVarInt();
		List<DialogueTopic> topics = new ArrayList<>(size);
		for (int i = 0; i < size; i++) topics.add(DialogueTopic.fromNetwork(buf));
		return new DialoguePresentation(dialogueId, styleId, mode, topics);
	}
}
