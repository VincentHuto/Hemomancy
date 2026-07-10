package com.vincenthuto.hemomancy.common.entity.npc.dialogue;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public record DialogueOptionPresentation(ResourceLocation icon, String detailKey, boolean enabled,
		DialogueOptionStyle style) {
	public static DialogueOptionPresentation normal() {
		return new DialogueOptionPresentation(null, null, true, DialogueOptionStyle.NORMAL);
	}

	public static DialogueOptionPresentation disabled(String detailKey) {
		return new DialogueOptionPresentation(null, detailKey, false, DialogueOptionStyle.NORMAL);
	}

	void toNetwork(FriendlyByteBuf buf) {
		buf.writeBoolean(icon != null);
		if (icon != null) buf.writeResourceLocation(icon);
		buf.writeBoolean(detailKey != null);
		if (detailKey != null) buf.writeUtf(detailKey);
		buf.writeBoolean(enabled);
		buf.writeVarInt(style.ordinal());
	}

	static DialogueOptionPresentation fromNetwork(FriendlyByteBuf buf) {
		ResourceLocation icon = buf.readBoolean() ? buf.readResourceLocation() : null;
		String detailKey = buf.readBoolean() ? buf.readUtf() : null;
		return new DialogueOptionPresentation(icon, detailKey, buf.readBoolean(),
				DialogueOptionStyle.fromOrdinal(buf.readVarInt()));
	}
}
