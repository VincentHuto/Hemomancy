package com.vincenthuto.hemomancy.common.entity.npc.dialogue;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public record DialogueOptionPresentation(ResourceLocation icon, String detailKey, boolean enabled,
		DialogueOptionStyle style, DialogueAttention attention, String promptKey) {
	public DialogueOptionPresentation(ResourceLocation icon, String detailKey, boolean enabled,
			DialogueOptionStyle style) {
		this(icon, detailKey, enabled, style, DialogueAttention.NONE, null);
	}

	public DialogueOptionPresentation(ResourceLocation icon, String detailKey, boolean enabled,
			DialogueOptionStyle style, DialogueAttention attention) {
		this(icon, detailKey, enabled, style, attention, null);
	}

	public DialogueOptionPresentation {
		if (attention == null) attention = DialogueAttention.NONE;
	}

	public static DialogueOptionPresentation normal() {
		return new DialogueOptionPresentation(null, null, true, DialogueOptionStyle.NORMAL, DialogueAttention.NONE, null);
	}

	public static DialogueOptionPresentation disabled(String detailKey) {
		return new DialogueOptionPresentation(null, detailKey, false, DialogueOptionStyle.NORMAL, DialogueAttention.NONE, null);
	}

	public static DialogueOptionPresentation attention(DialogueAttention attention) {
		return new DialogueOptionPresentation(null, null, true, DialogueOptionStyle.EMPHASIZED, attention, null);
	}

	public static DialogueOptionPresentation prompt(String promptKey) {
		return normal().withPrompt(promptKey);
	}

	public static DialogueOptionPresentation attention(DialogueAttention attention, String promptKey) {
		return attention(attention).withPrompt(promptKey);
	}

	public DialogueOptionPresentation withPrompt(String promptKey) {
		return new DialogueOptionPresentation(icon, detailKey, enabled, style, attention, promptKey);
	}

	void toNetwork(FriendlyByteBuf buf) {
		buf.writeBoolean(icon != null);
		if (icon != null) buf.writeResourceLocation(icon);
		buf.writeBoolean(detailKey != null);
		if (detailKey != null) buf.writeUtf(detailKey);
		buf.writeBoolean(enabled);
		buf.writeVarInt(style.ordinal());
		buf.writeVarInt(attention.ordinal());
		buf.writeBoolean(promptKey != null);
		if (promptKey != null) buf.writeUtf(promptKey);
	}

	static DialogueOptionPresentation fromNetwork(FriendlyByteBuf buf) {
		ResourceLocation icon = buf.readBoolean() ? buf.readResourceLocation() : null;
		String detailKey = buf.readBoolean() ? buf.readUtf() : null;
		boolean enabled = buf.readBoolean();
		DialogueOptionStyle style = DialogueOptionStyle.fromOrdinal(buf.readVarInt());
		DialogueAttention attention = DialogueAttention.fromOrdinal(buf.readVarInt());
		String promptKey = buf.readBoolean() ? buf.readUtf() : null;
		return new DialogueOptionPresentation(icon, detailKey, enabled, style, attention, promptKey);
	}
}
