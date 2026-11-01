package com.vincenthuto.hemomancy.common.entity.npc.dialogue;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.npc.trade.NpcTradeBoard;
import com.vincenthuto.hemomancy.common.entity.npc.trade.NpcTradeCard;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

/**
 * How a dialogue opens: focused on its start node, or as a topic hub. {@code trade} is the hub's Trade box (its cards,
 * or the speaker's refusal), {@link NpcTradeBoard#NONE} when the speaker has none.
 */
public record DialoguePresentation(ResourceLocation dialogueId, ResourceLocation styleId,
		DialogueScreenMode mode, List<DialogueTopic> topics, NpcTradeBoard trade) {
	private static final List<DialogueCategory> CATEGORIES = List.of(DialogueCategory.QUESTS,
			DialogueCategory.INQUIRIES, DialogueCategory.CONVERSATION, DialogueCategory.TRADE);

	public DialoguePresentation {
		topics = List.copyOf(topics);
		if (trade == null) trade = NpcTradeBoard.NONE;
	}

	public DialoguePresentation(ResourceLocation dialogueId, ResourceLocation styleId, DialogueScreenMode mode,
			List<DialogueTopic> topics) {
		this(dialogueId, styleId, mode, topics, NpcTradeBoard.NONE);
	}

	public static DialoguePresentation focused() {
		return new DialoguePresentation(Hemomancy.rloc("focused"),
				Hemomancy.rloc("default"),
				DialogueScreenMode.FOCUSED, List.of());
	}

	public static DialoguePresentation hub(ResourceLocation dialogueId, ResourceLocation styleId,
			List<DialogueTopic> topics) {
		return new DialoguePresentation(dialogueId, styleId, DialogueScreenMode.TOPIC_HUB, topics);
	}

	/** This presentation with {@code board} as its Trade box and {@code tradeTopics} added to its topics. */
	public DialoguePresentation withTrade(NpcTradeBoard board, List<DialogueTopic> tradeTopics) {
		List<DialogueTopic> all = new ArrayList<>(topics);
		all.addAll(tradeTopics);
		return new DialoguePresentation(dialogueId, styleId, mode, all, board);
	}

	public List<NpcTradeCard> tradeCards() {
		return trade.cards();
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
		trade.toNetwork(buf);
	}

	static DialoguePresentation fromNetwork(FriendlyByteBuf buf) {
		ResourceLocation dialogueId = buf.readResourceLocation();
		ResourceLocation styleId = buf.readResourceLocation();
		DialogueScreenMode mode = DialogueScreenMode.fromOrdinal(buf.readVarInt());
		int size = buf.readVarInt();
		List<DialogueTopic> topics = new ArrayList<>(size);
		for (int i = 0; i < size; i++) topics.add(DialogueTopic.fromNetwork(buf));
		NpcTradeBoard trade = NpcTradeBoard.fromNetwork(buf);
		return new DialoguePresentation(dialogueId, styleId, mode, topics, trade);
	}
}
