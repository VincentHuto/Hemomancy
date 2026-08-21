package com.vincenthuto.hemomancy.common.entity.npc.dialogue;

import net.minecraft.server.level.ServerPlayer;

public interface ProgressionDialogueNpc {
	DialogueTree progressionDialogue(ServerPlayer player);

	String progressionDialogueId();

	default DialogueAttention progressionAttention(ServerPlayer player) {
		DialogueTree tree = DialogueHubFactory.decorate(progressionDialogue(player), progressionDialogueId(), player);
		return DialogueAttentionResolver.from(tree.presentation());
	}
}
