package com.vincenthuto.hemomancy.common.entity.npc.dialogue.route;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodVolumeEvents;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.Bloodline;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodlineSavedData;
import com.vincenthuto.hemomancy.common.capability.player.unstained.UnstainedAccessRules;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import com.vincenthuto.hutoslib.common.network.HLPacketHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.*;

/**
 * Recruiting a Harbinger NPC into the player's bloodline, and expelling one; offered by the Vicar, the
 * Alchemist and the Mnemonist.
 */
final class RecruitmentDialogueEvents {
	private RecruitmentDialogueEvents() {
	}

	/** Handles the event when it is one of this NPC's; false lets the next route try. */
	public static boolean handle(DialogueEvent event) {
		ServerPlayer player = event.getPlayer();
		switch (event.getEventId()) {
		case "recruit_harbinger" -> {
			handleRecruitHarbinger(player, event.getEntityId());
		}
		case "expel_harbinger" -> {
			handleExpelHarbinger(player, event.getEntityId());
		}
		default -> {
			return false;
		}
		}
		return true;
	}

	/**
	 * Recruits a Harbinger NPC into the player's bloodline. The NPC's entity
	 * UUID is added as a phantom member — it counts toward the shared blood
	 * pool capacity without needing to be an online player. This allows
	 * single-player users to grow their pool without multiplayer partners.
	 */
	private static void handleRecruitHarbinger(ServerPlayer player, int entityId) {
		if (UnstainedAccessRules.blocksHarbingerProgress(HemoCapabilityAccess.getUnstainedProgress(player))) return;
		var volume = HemoCapabilityAccess.getBloodVolume(player);
		Bloodline bloodline = volume.getBloodLine();

		// Must have a bloodline first (signed the ledger)
		if (!bloodline.isValid()) {
			player.displayClientMessage(
					Component.translatable("hemomancy.dialogue.recruit.no_bloodline")
							.withStyle(ChatFormatting.RED),
					false);
			return;
		}

		Entity entity = player.level().getEntity(entityId);
		if (entity == null) {
			return;
		}
		ResourceLocation npcType = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
		String npcOutpost = HarbingerRecruitmentRules.findOutpostKey(entity);

		if (bloodline.hasNpcMember(entity.getUUID())) {
			player.displayClientMessage(
					Component.translatable("hemomancy.dialogue.recruit.already_member")
							.withStyle(ChatFormatting.GRAY),
					false);
			return;
		}
		if (bloodline.hasNpcMemberType(npcType)) {
			player.displayClientMessage(
					Component.translatable("hemomancy.dialogue.recruit.type_already_member")
							.withStyle(ChatFormatting.GRAY),
					false);
			return;
		}
		if (bloodline.hasNpcMemberOutpost(npcOutpost)) {
			player.displayClientMessage(
					Component.translatable("hemomancy.dialogue.recruit.outpost_already_member")
							.withStyle(ChatFormatting.GRAY),
					false);
			return;
		}

		// Add the NPC to the bloodline in world-level saved data
		ServerLevel overworld = player.server.overworld();
		BloodlineSavedData savedData = BloodlineSavedData.get(overworld);
		Bloodline updatedLine = savedData.addNpcMember(
				bloodline.getBloodlineUUID(), entity.getUUID(), npcType, npcOutpost);

		if (updatedLine != null) {
			// Update the player's local bloodline reference
			volume.setBloodLine(updatedLine);

			// Sync updated bloodline state to client
			BloodVolumeEvents.syncVolume(player, volume);

			// Success feedback
			String npcName = entity.getName().getString();
			player.displayClientMessage(
					Component.translatable("hemomancy.dialogue.recruit.success", npcName)
							.withStyle(ChatFormatting.DARK_RED),
					false);
			player.displayClientMessage(
					Component.translatable("hemomancy.dialogue.recruit.pool_increased",
							(int) updatedLine.getMaxBloodVolume(),
							updatedLine.getNpcMemberCount())
							.withStyle(ChatFormatting.DARK_RED),
					false);

			// Blood particles from the NPC as a visual oath
			Vec3 npcPos = entity.position();
			Vec3 playerPos = player.position();
			for (int i = 0; i < 6; i++) {
				Vec3 particlePos = npcPos.add(
						entity.level().random.nextDouble() - 0.5,
						entity.level().random.nextDouble() * 1.5 + 0.5,
						entity.level().random.nextDouble() - 0.5);
				HLPacketHandler.sendLightningSpawn(particlePos, playerPos.add(0, 1, 0),
						64.0f, entity.level().dimension(),
						ParticleColor.BLOOD, 2, 12, 4, 0.6f);
			}
		}
	}

	/**
	 * Removes a recruited Harbinger NPC from the player's bloodline. Only the
	 * bloodline progenitor/leader may expel members.
	 */
	private static void handleExpelHarbinger(ServerPlayer player, int entityId) {
		var volume = HemoCapabilityAccess.getBloodVolume(player);
		Bloodline bloodline = volume.getBloodLine();
		if (!bloodline.isValid()) {
			player.displayClientMessage(
					Component.translatable("hemomancy.dialogue.recruit.no_bloodline")
							.withStyle(ChatFormatting.RED),
					false);
			return;
		}

		if (!player.getUUID().equals(bloodline.getLeaderUUID())) {
			player.displayClientMessage(
					Component.translatable("hemomancy.dialogue.recruit.not_leader")
							.withStyle(ChatFormatting.RED),
					false);
			return;
		}

		Entity entity = player.level().getEntity(entityId);
		if (entity == null) {
			return;
		}
		ResourceLocation npcType = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
		String npcOutpost = HarbingerRecruitmentRules.findOutpostKey(entity);

		if (!bloodline.hasNpcMember(entity.getUUID())) {
			player.displayClientMessage(
					Component.translatable("hemomancy.dialogue.recruit.not_member")
							.withStyle(ChatFormatting.GRAY),
					false);
			return;
		}

		ServerLevel overworld = player.server.overworld();
		BloodlineSavedData savedData = BloodlineSavedData.get(overworld);
		savedData.removeNpcMember(bloodline.getBloodlineUUID(), entity.getUUID(), npcType, npcOutpost);
		Bloodline updatedLine = savedData.getBloodline(bloodline.getBloodlineUUID());
		if (updatedLine == null) {
			return;
		}

		for (ServerPlayer online : player.server.getPlayerList().getPlayers()) {
			var memberVolume = HemoCapabilityAccess.getBloodVolume(online);
			if (updatedLine.hasMember(online.getUUID())) {
				memberVolume.setBloodLine(updatedLine);
				BloodVolumeEvents.syncVolume(online, memberVolume);
			}
		}

		player.displayClientMessage(
				Component.translatable("hemomancy.dialogue.recruit.expel.success", entity.getName())
						.withStyle(ChatFormatting.DARK_RED),
				false);
	}
}
