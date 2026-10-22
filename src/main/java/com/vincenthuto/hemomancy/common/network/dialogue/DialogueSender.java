package com.vincenthuto.hemomancy.common.network.dialogue;

import com.vincenthuto.hemomancy.common.entity.npc.dialogue.MonolithDialogueContext;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.PendingWhispers;
import com.vincenthuto.hemomancy.common.succession.SuccessionDialogue;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Routes every outgoing dialogue. {@code PacketHandler.sendToPlayer} hands each {@link OpenDialoguePacket} here, so
 * callers keep sending dialogue the ordinary way.
 */
public final class DialogueSender {
	private DialogueSender() {
	}

	/**
	 * Whispers wait in the player's queue instead of opening. A dialogue with a speaker ends any Monolith
	 * conversation, and a succession resident's persona decorates the tree before it is sent.
	 */
	public static void open(ServerPlayer player, OpenDialoguePacket open) {
		if (PendingWhispers.isWhisper(open.tree())) {
			PendingWhispers.enqueue(player, open.tree());
			return;
		}
		if (open.tree().entityId() != -1) {
			MonolithDialogueContext.clear(player);
		}
		var entity = player.level().getEntity(open.tree().entityId());
		PacketDistributor.sendToPlayer(player,
				new OpenDialoguePacket(SuccessionDialogue.decorate(player, entity, open.tree())));
	}
}
