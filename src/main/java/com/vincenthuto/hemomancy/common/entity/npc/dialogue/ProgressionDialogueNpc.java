package com.vincenthuto.hemomancy.common.entity.npc.dialogue;

import com.vincenthuto.hemomancy.common.entity.npc.trade.NpcTradeSpeaker;
import net.minecraft.server.level.ServerPlayer;

public interface ProgressionDialogueNpc extends NpcTradeSpeaker {
	DialogueTree progressionDialogue(ServerPlayer player);

	String progressionDialogueId();

	@Override
	default String tradeSpeakerId() {
		return progressionDialogueId();
	}

	default DialogueAttention progressionAttention(ServerPlayer player) {
		DialogueTree tree = DialogueHubFactory.decorate(progressionDialogue(player), progressionDialogueId(), player);
		return DialogueAttentionResolver.from(tree.presentation());
	}
}
