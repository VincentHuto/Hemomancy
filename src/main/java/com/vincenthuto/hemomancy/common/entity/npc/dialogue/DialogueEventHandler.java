package com.vincenthuto.hemomancy.common.entity.npc.dialogue;

import com.vincenthuto.hemomancy.Hemomancy;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Listens for {@link DialogueEvent}s fired by dialogue option selection and
 * executes gameplay-relevant side-effects such as starting a quest line or
 * changing NPC disposition.
 */
@Mod.EventBusSubscriber(modid = Hemomancy.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class DialogueEventHandler {

	@SubscribeEvent
	public static void onDialogueOption(DialogueEvent event) {
		ServerPlayer player = event.getPlayer();
		switch (event.getEventId()) {
			case "zealot_accept_church" -> {
				player.displayClientMessage(
						Component.translatable("hemomancy.dialogue.event.accept_church")
								.withStyle(ChatFormatting.GREEN),
						false);
			}
			case "zealot_reject_help" -> {
				player.displayClientMessage(
						Component.translatable("hemomancy.dialogue.event.reject_help")
								.withStyle(ChatFormatting.RED),
						false);
			}
			case "zealot_accept_purification" -> {
				player.displayClientMessage(
						Component.translatable("hemomancy.dialogue.event.accept_purification")
								.withStyle(ChatFormatting.AQUA),
						false);
			}
			case "hermit_accept_guidance" -> {
				player.displayClientMessage(
						Component.translatable("hemomancy.dialogue.event.hermit_accept_guidance")
								.withStyle(ChatFormatting.DARK_RED),
						false);
			}
			case "hermit_archon_wisdom" -> {
				player.displayClientMessage(
						Component.translatable("hemomancy.dialogue.event.hermit_archon_wisdom")
								.withStyle(ChatFormatting.DARK_RED),
						false);
			}
			case "whisper_dismiss" -> {
				// Player dismissed the whisper — no gameplay effect, just acknowledged
			}
			case "whisper_truth_acknowledged" -> {
				player.displayClientMessage(
						Component.translatable("hemomancy.dialogue.event.whisper_truth_acknowledged")
								.withStyle(ChatFormatting.DARK_GREEN),
						false);
			}
			default -> {
				// Unknown event — log for development
				Hemomancy.LOGGER.debug("Unhandled dialogue event: {}", event.getEventId());
			}
		}
	}
}
