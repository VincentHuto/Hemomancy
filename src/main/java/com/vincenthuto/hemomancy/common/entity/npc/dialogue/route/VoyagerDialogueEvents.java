package com.vincenthuto.hemomancy.common.entity.npc.dialogue.route;

import net.minecraft.server.level.ServerPlayer;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.*;

/**
 * The Voyager's dialogue event: being observed.
 */
final class VoyagerDialogueEvents {
	private VoyagerDialogueEvents() {
	}

	/** Handles the event when it is one of this NPC's; false lets the next route try. */
	public static boolean handle(DialogueEvent event) {
		ServerPlayer player = event.getPlayer();
		switch (event.getEventId()) {
		case VoyagerIntroductionDialogue.OBSERVE -> event.setRewardDelivered(
				com.vincenthuto.hemomancy.common.mission.vicar.VoyagerIntroduction.observe(
						player, player.level().getEntity(event.getEntityId())));
		default -> {
			return false;
		}
		}
		return true;
	}
}
