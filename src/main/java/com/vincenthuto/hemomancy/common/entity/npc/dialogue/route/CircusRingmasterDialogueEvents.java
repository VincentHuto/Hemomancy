package com.vincenthuto.hemomancy.common.entity.npc.dialogue.route;

import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusRingmasterEntity;
import net.minecraft.server.level.ServerPlayer;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.*;

/**
 * The Circus Ringmaster's dialogue choices.
 */
final class CircusRingmasterDialogueEvents {
	private CircusRingmasterDialogueEvents() {
	}

	public static boolean handle(DialogueEvent event) {
		ServerPlayer player = event.getPlayer();
		if (event.getEventId().startsWith("circus_")
				&& player.level().getEntity(event.getEntityId()) instanceof CircusRingmasterEntity ringmaster) {
			ringmaster.handleChoice(player, event.getEventId());
			return true;
		}
		return false;
	}
}
