package com.vincenthuto.hemomancy.common.entity.npc.dialogue.route;

import net.minecraft.server.level.ServerPlayer;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.*;

/**
 * The Sanguine Monolith's dialogue events (a block, not an entity): pressing further and the cornerstone.
 */
public final class SanguineMonolithDialogueEvents {
	private SanguineMonolithDialogueEvents() {
	}

	/** Handles the event when it is one of this NPC's; false lets the next route try. */
	public static boolean handle(DialogueEvent event) {
		ServerPlayer player = event.getPlayer();
		switch (event.getEventId()) {
		case SanguineMonolithDialogueTrees.EVENT_SHATTER -> {
			event.setRewardDelivered(com.vincenthuto.hemomancy.common.block.harbinger.functional.SanguineMonolithBlock.pressFurther(player));
		}
		case SanguineMonolithDialogueTrees.EVENT_CORNERSTONE -> {
			event.setRewardDelivered(handleMonolithCornerstone(player, event.getEntityId()));
		}
		default -> {
			return false;
		}
		}
		return true;
	}

	/** A stone that has begun to break gives up nothing, even to a conversation opened before the press. */
	private static boolean handleMonolithCornerstone(ServerPlayer player, int entityId) {
		var stone = MonolithDialogueContext.speakingTo(player);
		if (stone != null && (stone.isBreaking() || stone.isBroken())) {
			player.displayClientMessage(net.minecraft.network.chat.Component.translatable("hemomancy.monolith.already_breaking"), true);
			return false;
		}
		return ArtificerDialogueEvents.claimArmatureTier(player, 2, "hemomancy.dialogue.event.monolith_cornerstone");
	}
}
