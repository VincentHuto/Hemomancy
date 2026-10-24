package com.vincenthuto.hemomancy.common.entity.npc.dialogue.route;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.mission.unstained.UnstainedDaggerReplacementRules;
import com.vincenthuto.hemomancy.common.mission.unstained.UnstainedObservances;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.*;

/**
 * The Unstained Guardian's dialogue events: observance tasks and the absolution dagger.
 */
final class GuardianDialogueEvents {
	private GuardianDialogueEvents() {
	}

	/** Handles the event when it is one of this NPC's; false lets the next route try. */
	public static boolean handle(DialogueEvent event) {
		ServerPlayer player = event.getPlayer();
		switch (event.getEventId()) {
		case "guardian_task_plating" -> UnstainedObservances.handle(player,
				UnstainedObservances.Observance.PLATE_THE_WARD);
		case "guardian_task_bell" -> UnstainedObservances.handle(player,
				UnstainedObservances.Observance.RING_THE_PALE_WATCH);
		case "guardian_replace_absolution_dagger" -> replaceAbsolutionDagger(player);
		default -> {
			return false;
		}
		}
		return true;
	}

	private static void replaceAbsolutionDagger(ServerPlayer player) {
		var progress = HemoCapabilityAccess.getUnstainedProgress(player);
		if (progress == null) return;
		boolean hasDagger = player.getInventory().contains(stack -> stack.is(ItemInit.absolution_dagger.get()));
		if (!UnstainedDaggerReplacementRules.canExchange(progress.hasClarityUnlocked(), hasDagger,
				player.getInventory().countItem(ItemInit.pale_silver_ingot.get()),
				player.getInventory().countItem(ItemInit.cleansed_blood_crystal_shard.get()))) {
			player.displayClientMessage((hasDagger ? Component.translatable("message.hemomancy.guardian_dialogue_events.the_guardian_will_not_replace_a") : Component.translatable("message.hemomancy.guardian_dialogue_events.the_guardian_requires_two_pale_silver")).withStyle(ChatFormatting.GRAY), false);
			return;
		}
		DialogueRewards.consumeItemCount(player, ItemInit.pale_silver_ingot.get(), 2);
		DialogueRewards.consumeItemCount(player, ItemInit.cleansed_blood_crystal_shard.get(), 1);
		DialogueRewards.giveOrDrop(player, new ItemStack(ItemInit.absolution_dagger.get()));
	}
}
