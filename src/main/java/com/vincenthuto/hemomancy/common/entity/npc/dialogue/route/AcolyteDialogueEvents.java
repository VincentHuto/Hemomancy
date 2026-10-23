package com.vincenthuto.hemomancy.common.entity.npc.dialogue.route;

import com.vincenthuto.hemomancy.common.mission.unstained.UnstainedObservances;
import net.minecraft.server.level.ServerPlayer;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.*;

/**
 * The Unstained Acolyte's observance tasks.
 */
final class AcolyteDialogueEvents {
	private AcolyteDialogueEvents() {
	}

	/** Handles the event when it is one of this NPC's; false lets the next route try. */
	public static boolean handle(DialogueEvent event) {
		ServerPlayer player = event.getPlayer();
		switch (event.getEventId()) {
		case "acolyte_task_gather_ghost_pipe" -> UnstainedObservances.handle(player,
				UnstainedObservances.Observance.GATHER_GHOST_PIPE);
		case "acolyte_task_wreath" -> UnstainedObservances.handle(player,
				UnstainedObservances.Observance.WEAVE_WREATH);
		case "acolyte_task_hemolytic" -> UnstainedObservances.handle(player,
				UnstainedObservances.Observance.PREPARE_HEMOLYTIC);
		case "acolyte_task_consecrate" -> UnstainedObservances.handle(player,
				UnstainedObservances.Observance.CONSECRATE_COPPER);
		case "acolyte_task_chalice" -> UnstainedObservances.handle(player,
				UnstainedObservances.Observance.OFFER_CHALICE);
		default -> {
			return false;
		}
		}
		return true;
	}
}
