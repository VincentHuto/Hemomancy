package com.vincenthuto.hemomancy.common.entity.npc.dialogue;

import net.minecraft.network.FriendlyByteBuf;

public record DialogueProgress(int current, int target, String rewardKey) {
	public DialogueProgress {
		current = Math.max(0, current);
		target = Math.max(0, target);
	}

	void toNetwork(FriendlyByteBuf buf) {
		buf.writeVarInt(current);
		buf.writeVarInt(target);
		buf.writeBoolean(rewardKey != null);
		if (rewardKey != null) buf.writeUtf(rewardKey);
	}

	static DialogueProgress fromNetwork(FriendlyByteBuf buf) {
		return new DialogueProgress(buf.readVarInt(), buf.readVarInt(), buf.readBoolean() ? buf.readUtf() : null);
	}
}
