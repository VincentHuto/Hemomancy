package com.vincenthuto.hemomancy.common.entity.npc.dialogue;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

/**
 * One card in a hub box. {@code lore} marks a Conversation card built from a lore option: it draws the lore glyph, and
 * opening it while unread records it under {@link #readPath()}.
 */
public record DialogueTopic(String id, DialogueCategory category, String titleKey, String summaryKey,
		String targetNodeId, ResourceLocation icon, DialogueTopicState state, String groupKey,
		DialogueProgress progress, ResourceLocation displayItemId, boolean unread, DialogueAttention attention,
		boolean lore) {
	private static final String CONVERSATION_PREFIX = "conversation/";
	private static final String LORE_READ_PREFIX = "lore/";

	public DialogueTopic(String id, DialogueCategory category, String titleKey, String summaryKey,
			String targetNodeId, ResourceLocation icon, DialogueTopicState state, String groupKey,
			DialogueProgress progress, ResourceLocation displayItemId, boolean unread) {
		this(id, category, titleKey, summaryKey, targetNodeId, icon, state, groupKey, progress,
				displayItemId, unread, DialogueAttention.NONE, false);
	}

	public DialogueTopic(String id, DialogueCategory category, String titleKey, String summaryKey,
			String targetNodeId, ResourceLocation icon, DialogueTopicState state, String groupKey,
			DialogueProgress progress, ResourceLocation displayItemId, boolean unread, DialogueAttention attention) {
		this(id, category, titleKey, summaryKey, targetNodeId, icon, state, groupKey, progress,
				displayItemId, unread, attention, false);
	}

	public DialogueTopic {
		if (attention == null) attention = DialogueAttention.NONE;
	}

	public static DialogueTopic available(String id, DialogueCategory category, String titleKey,
			String summaryKey, String targetNodeId) {
		return new DialogueTopic(id, category, titleKey, summaryKey, targetNodeId, null,
				DialogueTopicState.AVAILABLE, null, null, null, false, DialogueAttention.NONE, false);
	}

	/** The topic id of the Conversation card for an option slug. */
	public static String conversationId(String slug) {
		return CONVERSATION_PREFIX + slug;
	}

	/**
	 * The read-record path of a lore option slug. It keeps the {@code lore/<slug>} form the old Lore box used, so lore
	 * read before the Conversation merge stays read.
	 */
	public static String loreReadPath(String slug) {
		return LORE_READ_PREFIX + slug;
	}

	/** The path this topic is recorded under once read, below the dialogue id. */
	public String readPath() {
		return lore && id.startsWith(CONVERSATION_PREFIX)
				? loreReadPath(id.substring(CONVERSATION_PREFIX.length())) : id;
	}

	public DialogueTopic withUnread(boolean value) {
		return new DialogueTopic(id, category, titleKey, summaryKey, targetNodeId, icon, state,
				groupKey, progress, displayItemId, value, attention, lore);
	}

	public DialogueTopic withItem(ResourceLocation itemId) {
		return new DialogueTopic(id, category, titleKey, summaryKey, targetNodeId, icon, state,
				groupKey, progress, itemId, unread, attention, lore);
	}

	void toNetwork(FriendlyByteBuf buf) {
		buf.writeUtf(id);
		buf.writeVarInt(category.ordinal());
		buf.writeUtf(titleKey);
		buf.writeUtf(summaryKey);
		buf.writeUtf(targetNodeId);
		buf.writeBoolean(icon != null);
		if (icon != null) buf.writeResourceLocation(icon);
		buf.writeVarInt(state.ordinal());
		buf.writeBoolean(groupKey != null);
		if (groupKey != null) buf.writeUtf(groupKey);
		buf.writeBoolean(progress != null);
		if (progress != null) progress.toNetwork(buf);
		buf.writeBoolean(displayItemId != null);
		if (displayItemId != null) buf.writeResourceLocation(displayItemId);
		buf.writeBoolean(unread);
		buf.writeVarInt(attention.ordinal());
		buf.writeBoolean(lore);
	}

	static DialogueTopic fromNetwork(FriendlyByteBuf buf) {
		String id = buf.readUtf();
		DialogueCategory category = DialogueCategory.fromOrdinal(buf.readVarInt());
		String titleKey = buf.readUtf();
		String summaryKey = buf.readUtf();
		String target = buf.readUtf();
		ResourceLocation icon = buf.readBoolean() ? buf.readResourceLocation() : null;
		DialogueTopicState state = DialogueTopicState.fromOrdinal(buf.readVarInt());
		String group = buf.readBoolean() ? buf.readUtf() : null;
		DialogueProgress progress = buf.readBoolean() ? DialogueProgress.fromNetwork(buf) : null;
		ResourceLocation item = buf.readBoolean() ? buf.readResourceLocation() : null;
		boolean unread = buf.readBoolean();
		DialogueAttention attention = DialogueAttention.fromOrdinal(buf.readVarInt());
		boolean lore = buf.readBoolean();
		return new DialogueTopic(id, category, titleKey, summaryKey, target, icon, state, group, progress, item,
				unread, attention, lore);
	}
}
