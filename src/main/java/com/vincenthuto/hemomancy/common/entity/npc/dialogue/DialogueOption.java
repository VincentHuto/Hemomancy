package com.vincenthuto.hemomancy.common.entity.npc.dialogue;

import net.minecraft.network.FriendlyByteBuf;

/**
 * A single clickable response option in a dialogue node.
 *
 * @param text       Translation key for the option text shown to the player.
 * @param nextNodeId The id of the {@link DialogueNode} this option leads to,
 *                   or {@code null} to end the conversation.
 * @param eventId    Optional event identifier fired when the player picks this
 *                   option (e.g. {@code "accept_church"}, {@code "reject_help"}).
 *                   May be {@code null} if no event should fire.
 */
public record DialogueOption(String text, String nextNodeId, String eventId, DialogueOptionPresentation presentation) {

	public DialogueOption(String text, String nextNodeId, String eventId) {
		this(text, nextNodeId, eventId, DialogueOptionPresentation.normal());
	}

	public DialogueOption {
		if (presentation == null) presentation = DialogueOptionPresentation.normal();
	}

	public void toNetwork(FriendlyByteBuf buf) {
		buf.writeUtf(text);
		buf.writeBoolean(nextNodeId != null);
		if (nextNodeId != null) buf.writeUtf(nextNodeId);
		buf.writeBoolean(eventId != null);
		if (eventId != null) buf.writeUtf(eventId);
		presentation.toNetwork(buf);
	}

	public static DialogueOption fromNetwork(FriendlyByteBuf buf) {
		String text = buf.readUtf();
		String next = buf.readBoolean() ? buf.readUtf() : null;
		String event = buf.readBoolean() ? buf.readUtf() : null;
		return new DialogueOption(text, next, event, DialogueOptionPresentation.fromNetwork(buf));
	}
}
