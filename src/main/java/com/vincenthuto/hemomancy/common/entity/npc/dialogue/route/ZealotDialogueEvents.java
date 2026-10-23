package com.vincenthuto.hemomancy.common.entity.npc.dialogue.route;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.unstained.UnstainedProgressEvents;
import com.vincenthuto.hemomancy.common.capability.player.unstained.UnstainedStarterSupplyRules;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.mission.unstained.UnstainedObservances;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.*;

/**
 * The Unstained Zealot's dialogue events: observance tasks, the novitiate and the church's starter supply.
 */
final class ZealotDialogueEvents {
	private ZealotDialogueEvents() {
	}

	/** Handles the event when it is one of this NPC's; false lets the next route try. */
	public static boolean handle(DialogueEvent event) {
		ServerPlayer player = event.getPlayer();
		switch (event.getEventId()) {
		case "zealot_task_still_waters" -> UnstainedObservances.handle(player,
				UnstainedObservances.Observance.CONDENSE_STILL_WATERS);
		case "zealot_task_pallid_icon" -> UnstainedObservances.handle(player,
				UnstainedObservances.Observance.BEAR_PALLID_ICON);
		case "zealot_begin_novitiate" -> UnstainedObservances.beginNovitiate(player);
		case "novitiate_gather_remedies" -> UnstainedObservances.handle(player,
				UnstainedObservances.Observance.NOVITIATE_GATHER_REMEDIES);
		case "novitiate_gentle_separation" -> UnstainedObservances.handle(player,
				UnstainedObservances.Observance.NOVITIATE_GENTLE_SEPARATION);
		case "novitiate_stillwater_labor" -> UnstainedObservances.handle(player,
				UnstainedObservances.Observance.NOVITIATE_STILLWATER_LABOR);
		case "novitiate_clean_labor" -> UnstainedObservances.handle(player,
				UnstainedObservances.Observance.NOVITIATE_CLEAN_LABOR);
		case "novitiate_shelter_afflicted" -> UnstainedObservances.handle(player,
				UnstainedObservances.Observance.NOVITIATE_SHELTER_AFFLICTED);
		case "zealot_accept_church" -> {
			grantUnstainedStarterSupply(player);
		}
		case "zealot_reject_help" -> {
			player.displayClientMessage(
					Component.translatable("hemomancy.dialogue.event.reject_help")
							.withStyle(ChatFormatting.RED),
					false);
		}
		case "zealot_accept_purification" -> {
			grantUnstainedStarterSupply(player);
		}
		default -> {
			return false;
		}
		}
		return true;
	}

	private static void grantUnstainedStarterSupply(ServerPlayer player) {
		var progress = HemoCapabilityAccess.getUnstainedProgress(player);
		var grant = UnstainedStarterSupplyRules.grantFor(
				progress.hasClaimedChurchStarterSupply(),
				player.getInventory().countItem(ItemInit.hemolytic_solution.get()),
				player.getInventory().contains(new ItemStack(ItemInit.liber_immaculatus.get())));
		if (grant == UnstainedStarterSupplyRules.Grant.NONE) {
			player.displayClientMessage(Component.translatable(
					"hemomancy.dialogue.event.unstained_starter_already_claimed")
					.withStyle(ChatFormatting.GRAY), false);
			return;
		}
		if (grant.solutionCount() > 0) {
			DialogueRewards.giveOrDrop(player, new ItemStack(ItemInit.hemolytic_solution.get(), grant.solutionCount()));
		}
		if (grant.guide()) {
			DialogueRewards.giveOrDrop(player, new ItemStack(ItemInit.liber_immaculatus.get()));
		}
		progress.setClaimedChurchStarterSupply(true);
		UnstainedProgressEvents.syncProgress(player, progress);
		player.displayClientMessage(Component.translatable(
				"hemomancy.dialogue.event.unstained_starter_granted")
				.withStyle(ChatFormatting.AQUA), false);
	}
}
