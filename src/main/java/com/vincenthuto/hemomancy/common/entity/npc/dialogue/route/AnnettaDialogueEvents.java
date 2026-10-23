package com.vincenthuto.hemomancy.common.entity.npc.dialogue.route;

import net.minecraft.server.level.ServerPlayer;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.*;

/**
 * Annetta Knowles's dialogue choices; every annetta_ event stops here.
 */
final class AnnettaDialogueEvents {
	private AnnettaDialogueEvents() {
	}

	public static boolean handle(DialogueEvent event) {
		if (!event.getEventId().startsWith("annetta_")) return false;
		ServerPlayer player = event.getPlayer();
		if (player.level().getEntity(event.getEntityId())
				instanceof com.vincenthuto.hemomancy.common.entity.boss.annetta.AnnettaKnowlesEntity annetta) {
			event.setRewardDelivered(annetta.handleDialogueChoice(player, event.getEventId()));
		}
		return true;
	}
}
