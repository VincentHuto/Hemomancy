package com.vincenthuto.hemomancy.common.entity.npc.dialogue.route;

import com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.HemomancyDiscoverySource;
import com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.discovery.LiberEntryDefinitions;
import com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.discovery.LiberKnowledgeHelper;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.DialogueEvent;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/**
 * Our Lady's whispers: the three closing choices that leave a line behind. Hearing the Rite of Clarity Ascension
 * also opens its Liber Immaculatus page, so the words the line promises are there to read.
 */
final class OurLadyDialogueEvents {
	static final String CLARITY_RITE_MEMORY = "lady_clarity_rite_memory";
	static final String ENLIGHTENED_TRUTH_1 = "lady_enlightened_truth_1";
	static final String ENLIGHTENED_TRUTH_2 = "lady_enlightened_truth_2";

	private OurLadyDialogueEvents() {
	}

	public static boolean handle(DialogueEvent event) {
		if (!event.getEventId().startsWith("lady_")) return false;
		ServerPlayer player = event.getPlayer();
		switch (event.getEventId()) {
			case CLARITY_RITE_MEMORY -> {
				LiberKnowledgeHelper.unlockEntry(player, LiberEntryDefinitions.IMMACULATUS_CLARITY_PRICE,
						HemomancyDiscoverySource.DIALOGUE);
				say(player, CLARITY_RITE_MEMORY);
			}
			case ENLIGHTENED_TRUTH_1, ENLIGHTENED_TRUTH_2 -> say(player, event.getEventId());
			default -> {
				return false;
			}
		}
		return true;
	}

	private static void say(ServerPlayer player, String eventId) {
		player.displayClientMessage(Component.translatable("hemomancy.dialogue.event." + eventId)
				.withStyle(ChatFormatting.AQUA, ChatFormatting.ITALIC), false);
	}
}
