package com.vincenthuto.hemomancy.common.entity.npc.dialogue.route;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.upgrade.UpgradeStation;
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

	public static boolean hasClaimedMonolithCornerstone(ServerPlayer player) {
		return HemoCapabilityAccess.stationUpgrades(player).hasClaimed(UpgradeStation.ARMATURE, 2);
	}

	private static boolean handleMonolithCornerstone(ServerPlayer player, int entityId) {
		return ArtificerDialogueEvents.claimArmatureTier(player, 2, "hemomancy.dialogue.event.monolith_cornerstone");
	}
}
