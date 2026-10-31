package com.vincenthuto.hemomancy.common.entity.npc.dialogue.route;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.court.CourtDiscoveryProgress;
import com.vincenthuto.hemomancy.common.entity.npc.harbinger.HarbingerVoyagerEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.function.Predicate;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.*;

/**
 * The Voyager's dialogue events: being observed, and the Brined Court: explaining it, reading a Brine-Sealed Letter
 * and issuing the Relay Chart.
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
		case CourtDiscoveryDialogue.VOYAGER_LEARN -> event.setRewardDelivered(
				court(player, event.getEntityId(), CourtDiscoveryProgress::recordVoyager));
		case CourtDiscoveryDialogue.VOYAGER_LETTER -> event.setRewardDelivered(
				court(player, event.getEntityId(), CourtDiscoveryProgress::recordLetter));
		case CourtDiscoveryDialogue.VOYAGER_CHART -> event.setRewardDelivered(court(player, event.getEntityId(), ignored -> false));
		default -> {
			return false;
		}
		}
		return true;
	}

	/** Every Court answer from a Voyager ends with the Relay Chart, when the player knows the Court and has none. */
	private static boolean court(ServerPlayer player, int entityId, Predicate<ServerPlayer> record) {
		if (!(player.level().getEntity(entityId) instanceof HarbingerVoyagerEntity)) return false;
		boolean recorded = record.test(player);
		ItemStack chart = CourtDiscoveryProgress.takeChart(player);
		if (!chart.isEmpty()) {
			DialogueRewards.giveOrDropAtEntity(player, entityId, chart);
			player.displayClientMessage(Component.translatable("hemomancy.dialogue.event.relay_chart_issued")
					.withStyle(ChatFormatting.DARK_AQUA), false);
		} else if (CourtDiscoveryProgress.isKnown(player)) {
			player.displayClientMessage(Component.translatable("hemomancy.dialogue.event.relay_chart_carrying")
					.withStyle(ChatFormatting.GRAY), false);
		}
		return recorded || !chart.isEmpty();
	}
}
