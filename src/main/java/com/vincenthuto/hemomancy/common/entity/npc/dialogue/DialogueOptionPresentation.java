package com.vincenthuto.hemomancy.common.entity.npc.dialogue;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

/**
 * How an option is shown. {@code lore} marks a root option as lore: in the hub it becomes a Conversation card with the
 * lore glyph and an unread dot. {@code quest} files a root option under Quest Work whatever its id says.
 * {@code rapportTier} (an {@code NpcTradeRules} tier) gates a lore option on the player's rapport with the speaker:
 * below it the hub shows a locked lore card with the option's teaser ({@code <text>.teaser}) and no way in.
 */
public record DialogueOptionPresentation(ResourceLocation icon, String detailKey, boolean enabled,
		DialogueOptionStyle style, DialogueAttention attention, String promptKey, boolean lore, boolean quest,
		int rapportTier) {
	public DialogueOptionPresentation(ResourceLocation icon, String detailKey, boolean enabled,
			DialogueOptionStyle style) {
		this(icon, detailKey, enabled, style, DialogueAttention.NONE, null);
	}

	public DialogueOptionPresentation(ResourceLocation icon, String detailKey, boolean enabled,
			DialogueOptionStyle style, DialogueAttention attention) {
		this(icon, detailKey, enabled, style, attention, null);
	}

	public DialogueOptionPresentation(ResourceLocation icon, String detailKey, boolean enabled,
			DialogueOptionStyle style, DialogueAttention attention, String promptKey) {
		this(icon, detailKey, enabled, style, attention, promptKey, false, false);
	}

	public DialogueOptionPresentation(ResourceLocation icon, String detailKey, boolean enabled,
			DialogueOptionStyle style, DialogueAttention attention, String promptKey, boolean lore, boolean quest) {
		this(icon, detailKey, enabled, style, attention, promptKey, lore, quest, 0);
	}

	public DialogueOptionPresentation {
		if (attention == null) attention = DialogueAttention.NONE;
		rapportTier = Math.max(0, rapportTier);
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

	/** A plain lore option. Not {@code lore()}: that name is the record's accessor. */
	public static DialogueOptionPresentation loreLine() {
		return normal().withLore();
	}

	/** A plain quest option. */
	public static DialogueOptionPresentation questWork() {
		return normal().withQuest();
	}

	public DialogueOptionPresentation withPrompt(String promptKey) {
		return new DialogueOptionPresentation(icon, detailKey, enabled, style, attention, promptKey, lore, quest,
				rapportTier);
	}

	public DialogueOptionPresentation withLore() {
		return new DialogueOptionPresentation(icon, detailKey, enabled, style, attention, promptKey, true, quest,
				rapportTier);
	}

	public DialogueOptionPresentation withQuest() {
		return new DialogueOptionPresentation(icon, detailKey, enabled, style, attention, promptKey, lore, true,
				rapportTier);
	}

	/** Gates this option on rapport tier {@code tier} with the speaker. */
	public DialogueOptionPresentation withRapportTier(int tier) {
		return new DialogueOptionPresentation(icon, detailKey, enabled, style, attention, promptKey, lore, quest, tier);
	}

	/** This option shown but not choosable, with {@code detailKey} saying why. */
	public DialogueOptionPresentation asDisabled(String detailKey) {
		return new DialogueOptionPresentation(icon, detailKey, false, style, attention, promptKey, lore, quest,
				rapportTier);
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
		buf.writeBoolean(lore);
		buf.writeBoolean(quest);
		buf.writeVarInt(rapportTier);
	}

	static DialogueOptionPresentation fromNetwork(FriendlyByteBuf buf) {
		ResourceLocation icon = buf.readBoolean() ? buf.readResourceLocation() : null;
		String detailKey = buf.readBoolean() ? buf.readUtf() : null;
		boolean enabled = buf.readBoolean();
		DialogueOptionStyle style = DialogueOptionStyle.fromOrdinal(buf.readVarInt());
		DialogueAttention attention = DialogueAttention.fromOrdinal(buf.readVarInt());
		String promptKey = buf.readBoolean() ? buf.readUtf() : null;
		boolean lore = buf.readBoolean();
		boolean quest = buf.readBoolean();
		int rapportTier = buf.readVarInt();
		return new DialogueOptionPresentation(icon, detailKey, enabled, style, attention, promptKey, lore, quest,
				rapportTier);
	}
}
