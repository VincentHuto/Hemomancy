package com.vincenthuto.hemomancy.common.entity.npc.dialogue.route;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.entity.npc.harbinger.HarbingerHermitEntity;
import com.vincenthuto.hemomancy.common.rite.TempleOathRules;
import com.vincenthuto.hemomancy.common.util.ProgressionGates;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import com.vincenthuto.hutoslib.common.network.HLPacketHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.*;

/**
 * The Hermit's dialogue events: guidance, the offered heart and the farewell.
 */
final class HermitDialogueEvents {
	private HermitDialogueEvents() {
	}

	/** Handles the event when it is one of this NPC's; false lets the next route try. */
	public static boolean handle(DialogueEvent event) {
		ServerPlayer player = event.getPlayer();
		switch (event.getEventId()) {
		case "hermit_accept_guidance" -> {
			player.displayClientMessage(
					Component.translatable("hemomancy.dialogue.event.hermit_accept_guidance")
							.withStyle(ChatFormatting.DARK_RED),
					false);
		}
		case "hermit_heart_offered" -> {
			Entity entity = player.level().getEntity(event.getEntityId());
                if (entity instanceof HarbingerHermitEntity hermit
                        && com.vincenthuto.hemomancy.common.mission.vicar.EarlyInitiation.near(player, hermit)
                        && !hermit.isFarewellDying() && ProgressionGates.isDegree(player, 0)
                        && !HemoCapabilityAccess.getBloodVolume(player).isActive()) {
                    TempleOathRules.bless(player, hermit.getUUID());
                    player.displayClientMessage(Component.translatable("hemomancy.dialogue.event.hermit_heart_offered"), false);
                }
            }
		case "hermit_farewell_die" -> {
			// Find the hermit entity, drop the mnemonic blueprint, then end the hermit
			Entity entity = player.level().getEntity(event.getEntityId());
                if (entity instanceof HarbingerHermitEntity hermit
                        && !hermit.isFarewellDying() && TempleOathRules.hasClaimedHeartFrom(player, hermit.getUUID())
                        && com.vincenthuto.hemomancy.common.mission.vicar.EarlyInitiation.near(player, hermit)
                        && com.vincenthuto.hemomancy.common.mission.vicar.EarlyInitiation.attached(player)) {
                    Vec3 pos = hermit.position();
				// Passing text passage — the mortal display was the hermits heart
				player.displayClientMessage(
						Component.translatable("hemomancy.dialogue.event.hermit_farewell_die")
								.withStyle(ChatFormatting.DARK_RED),
						false);
				player.displayClientMessage(
						Component.translatable("hemomancy.dialogue.event.hermit_passing")
								.withStyle(ChatFormatting.GRAY),
						false);
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
				hermit.beginFarewellDeath();
			}
		}
		case "hermit_archon_wisdom" -> {
			player.displayClientMessage(
					Component.translatable("hemomancy.dialogue.event.hermit_archon_wisdom")
							.withStyle(ChatFormatting.DARK_RED),
					false);
		}
		default -> {
			return false;
		}
		}
		return true;
	}
}
