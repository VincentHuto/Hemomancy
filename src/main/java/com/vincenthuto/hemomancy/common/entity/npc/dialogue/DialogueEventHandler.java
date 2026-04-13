package com.vincenthuto.hemomancy.common.entity.npc.dialogue;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.npc.HarbingerHermitEntity;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import com.vincenthuto.hutoslib.common.network.HLPacketHandler;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
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
			case "hermit_heart_offered" -> {
				player.displayClientMessage(
						Component.translatable("hemomancy.dialogue.event.hermit_heart_offered")
								.withStyle(ChatFormatting.DARK_RED),
						false);
			}
			case "hermit_farewell_die" -> {
				player.displayClientMessage(
						Component.translatable("hemomancy.dialogue.event.hermit_farewell_die")
								.withStyle(ChatFormatting.DARK_RED),
						false);
				// Find the hermit entity and kill it, dropping the rite hint
				Entity entity = player.level().getEntity(event.getEntityId());
				if (entity instanceof HarbingerHermitEntity hermit) {
					Vec3 pos = hermit.position();
					// Drop the rite hint item
					ItemEntity drop = new ItemEntity(hermit.level(), pos.x, pos.y + 0.5, pos.z,
							new ItemStack(ItemInit.harbinger_rite_hint.get()));
					hermit.level().addFreshEntity(drop);
					// Visual effects: blood particles rising from the hermit
					for (int i = 0; i < 8; i++) {
						Vec3 particlePos = pos.add(
								hermit.level().random.nextDouble() - 0.5,
								hermit.level().random.nextDouble() * 1.5,
								hermit.level().random.nextDouble() - 0.5);
						HLPacketHandler.sendLightningSpawn(pos.add(0, 1, 0), particlePos,
								64.0f, hermit.level().dimension(),
								ParticleColor.BLOOD, 2, 15, 6, 0.8f);
					}
					// The hermit is invulnerable by default (see HarbingerHermitEntity constructor),
					// so clear the flag before killing. Its duty is fulfilled.
					hermit.setInvulnerable(false);
					hermit.kill();
				}
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
