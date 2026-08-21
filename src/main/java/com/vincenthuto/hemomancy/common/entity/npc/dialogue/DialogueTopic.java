package com.vincenthuto.hemomancy.common.entity.npc.dialogue;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public record DialogueTopic(String id, DialogueCategory category, String titleKey, String summaryKey,
		String targetNodeId, ResourceLocation icon, DialogueTopicState state, String groupKey,
		DialogueProgress progress, ResourceLocation displayItemId, boolean unread, DialogueAttention attention) {
	public DialogueTopic(String id, DialogueCategory category, String titleKey, String summaryKey,
			String targetNodeId, ResourceLocation icon, DialogueTopicState state, String groupKey,
			DialogueProgress progress, ResourceLocation displayItemId, boolean unread) {
		this(id, category, titleKey, summaryKey, targetNodeId, icon, state, groupKey, progress,
				displayItemId, unread, DialogueAttention.NONE);
	}

	public DialogueTopic {
		if (attention == null) attention = DialogueAttention.NONE;
	}

	public static DialogueTopic available(String id, DialogueCategory category, String titleKey,
			String summaryKey, String targetNodeId) {
		return new DialogueTopic(id, category, titleKey, summaryKey, targetNodeId, null,
				DialogueTopicState.AVAILABLE, null, null, null, false, DialogueAttention.NONE);
	}

	public DialogueTopic withUnread(boolean value) {
		return new DialogueTopic(id, category, titleKey, summaryKey, targetNodeId, icon, state,
				groupKey, progress, displayItemId, value, attention);
	}

	public DialogueTopic withItem(ResourceLocation itemId) {
		return new DialogueTopic(id, category, titleKey, summaryKey, targetNodeId, icon, state,
				groupKey, progress, itemId, unread, attention);
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
		return new DialogueTopic(id, category, titleKey, summaryKey, target, icon, state, group, progress, item,
				unread, attention);
	}
}
