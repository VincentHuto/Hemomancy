package com.vincenthuto.hemomancy.common.entity.npc.dialogue;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodVolumeEvents;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.Bloodline;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodlineSavedData;
import com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.HemomancyDiscoverySource;
import com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.discovery.LiberEntryDefinitions;
import com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.discovery.LiberKnowledgeHelper;
import com.vincenthuto.hemomancy.common.entity.npc.harbinger.HarbingerHermitEntity;
import com.vincenthuto.hemomancy.common.entity.npc.unstained.UnstainedScoutEntity;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.BloodStructureHintItem;
import com.vincenthuto.hemomancy.common.item.shared.PreWrittenMemoItem;
import com.vincenthuto.hemomancy.common.item.shared.RiteHintItem;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.BloodVolumeServerPacket;
import com.vincenthuto.hemomancy.common.network.particle.SpawnSanguineOmenEffectPacket;
import com.vincenthuto.hemomancy.common.worldgen.FungalGardenTravelHelper;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import com.vincenthuto.hutoslib.common.network.HLPacketHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

/**
 * Listens for {@link DialogueEvent}s fired by dialogue option selection and
 * executes gameplay-relevant side-effects such as starting a quest line or
 * changing NPC disposition.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public class DialogueEventHandler {
	private static final int BLOOD_SHOTTING_OVERLAY_TICKS = 1200;
	private static final float BLOOD_SHOTTING_OVERLAY_ALPHA = 0.30F;

	@SubscribeEvent
	public static void onDialogueOption(DialogueEvent event) {
		ServerPlayer player = event.getPlayer();
		var mnemonistChoice = MnemonistStarterMemoryChoice.fromEventId(event.getEventId());
		if (mnemonistChoice.isPresent()) {
			handleMnemonistStarterMemory(player, event.getEntityId(), mnemonistChoice.get());
			return;
		}
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
				// Find the hermit entity, drop the rite hint, then end the hermit
				Entity entity = player.level().getEntity(event.getEntityId());
				if (entity instanceof HarbingerHermitEntity hermit) {
					Vec3 pos = hermit.position();
					// Drop the rite hint item — configured for the Sanguine Initiation rite
					ItemStack riteHint = RiteHintItem.createForRite(
							ItemInit.rite_hint.get(), ResourceLocation.fromNamespaceAndPath(Hemomancy.MOD_ID,
									"cardinal_rite/sanguine_initiation"));
					ItemEntity drop = new ItemEntity(hermit.level(), pos.x, pos.y + 0.5, pos.z, riteHint);
					hermit.level().addFreshEntity(drop);
					// Passing text passage — the mortal display was the hermitâ€™s heart
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
					// The hermit is invulnerable by default (see HarbingerHermitEntity constructor),
					// so clear the flag before ending it. Its duty is fulfilled.
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
			case "recruit_harbinger" -> {
				handleRecruitHarbinger(player, event.getEntityId());
			}
			case "expel_harbinger" -> {
				handleExpelHarbinger(player, event.getEntityId());
			}
			case HarbingerVicarDialogueTrees.EVENT_BLOOD_SHOTTING -> {
				handleVicarBloodShotting(player);
			}
			case "qliphoth_communion_done" -> {
				// Player completed the full Qliphoth Communion — nine pomes consumed.
				// This is a narrative milestone; the apotheos_rite path is now spiritually prepared.
				player.displayClientMessage(
						Component.translatable("hemomancy.dialogue.event.qliphoth_communion_done")
								.withStyle(ChatFormatting.DARK_GREEN),
						false);
			}
			case "archon_choice_silence" -> {
				// Archon chose to carry the truth in silence — they turn back from the Eighth Degree.
				player.getPersistentData().putString(
						FungalGardenTravelHelper.ARCHON_CHOICE_KEY,
						FungalGardenTravelHelper.ARCHON_CHOICE_SILENCE);
				FungalGardenTravelHelper.performReturnTravel(player);
				player.displayClientMessage(
						Component.translatable("hemomancy.dialogue.event.archon_choice_silence")
								.withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC),
						false);
			}
			case "archon_choice_eighth_degree" -> {
				// Archon chose to pursue the Eighth Degree — the Apotheos path opens.
				player.getPersistentData().putString(
						FungalGardenTravelHelper.ARCHON_CHOICE_KEY,
						FungalGardenTravelHelper.ARCHON_CHOICE_APOTHEOS);
				FungalGardenTravelHelper.performReturnTravel(player);
				player.displayClientMessage(
						Component.translatable("hemomancy.dialogue.event.archon_choice_eighth_degree")
								.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC),
						false);
			}
			case "give_blood_structure_hint" -> {
				handleGiveBloodStructureHint(player, event.getEntityId());
			}
			case "give_stained_church_map" -> {
				handleGiveStainedChurchMap(player, event.getEntityId());
			}
			case "scout_give_notes" -> {
				handleScoutGiveNotes(player, event.getEntityId());
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

	private static void handleVicarBloodShotting(ServerPlayer player) {
		boolean changed = LiberKnowledgeHelper.unlockEntry(player, LiberEntryDefinitions.ABOCIPHER_LITERACY,
				HemomancyDiscoverySource.DIALOGUE);
		PacketHandler.sendToPlayer(player, new SpawnSanguineOmenEffectPacket(player.position(),
				BLOOD_SHOTTING_OVERLAY_TICKS, BLOOD_SHOTTING_OVERLAY_ALPHA,
				player.serverLevel().random.nextInt(), true));
		player.displayClientMessage(
				Component.translatable(changed
								? "hemomancy.dialogue.event.vicar_blood_shotting"
								: "hemomancy.dialogue.event.vicar_blood_shotting_known")
						.withStyle(ChatFormatting.DARK_RED),
				false);
	}

	private static void handleMnemonistStarterMemory(ServerPlayer player, int entityId, MnemonistStarterMemoryChoice choice) {
		boolean purifying = HemoCapabilityAccess.getUnstainedProgress(player)
				.map(progress -> progress.hasBegunPurification())
				.orElse(false);
		boolean clarity = HemoCapabilityAccess.getUnstainedProgress(player)
				.map(progress -> progress.hasClarityUnlocked())
				.orElse(false);
		boolean claimed = player.getPersistentData().getBoolean(MnemonistStarterMemoryChoice.CLAIM_KEY);
		int degree = HemoCapabilityAccess.getPlayerDegreeNumber(player);

		if (!MnemonistStarterMemoryChoice.canClaim(degree, purifying, clarity, claimed)) {
			player.displayClientMessage(
					Component.translatable("hemomancy.dialogue.event.mnemonist_starter_unavailable")
							.withStyle(ChatFormatting.DARK_RED),
					false);
			return;
		}
		if (knowsStarterManipulation(player, choice.manipulationName())) {
			player.displayClientMessage(
					Component.translatable("hemomancy.dialogue.event.mnemonist_starter_already_known")
							.withStyle(ChatFormatting.GRAY),
					false);
			return;
		}

		ItemStack stack = starterStack(choice);
		if (stack.isEmpty()) {
			return;
		}
		giveOrDropAtEntity(player, entityId, stack);
		player.getPersistentData().putBoolean(MnemonistStarterMemoryChoice.CLAIM_KEY, true);
		player.displayClientMessage(
				Component.translatable("hemomancy.dialogue.event.mnemonist_starter_granted")
						.withStyle(ChatFormatting.DARK_RED),
				false);
	}

	private static boolean knowsStarterManipulation(ServerPlayer player, String manipulationName) {
		return HemoCapabilityAccess.getKnownManipulations(player)
				.map(known -> known.getKnownManips().keySet().stream()
						.anyMatch(manip -> manip != null && manipulationName.equals(manip.getName())))
				.orElse(false);
	}

	private static ItemStack starterStack(MnemonistStarterMemoryChoice choice) {
		return switch (choice) {
			case BLOOD_SHOT -> new ItemStack(ItemInit.crude_memory_blood_shot.get());
			case BLOOD_RUSH -> new ItemStack(ItemInit.crude_memory_blood_rush.get());
			case DEADLY_GAZE -> new ItemStack(ItemInit.crude_memory_deadly_gaze.get());
		};
	}

	private static void giveOrDropAtEntity(ServerPlayer player, int entityId, ItemStack stack) {
		Entity entity = player.level().getEntity(entityId);
		if (entity != null) {
			Vec3 pos = entity.position();
			ItemEntity drop = new ItemEntity(entity.level(), pos.x, pos.y + 0.5, pos.z, stack);
			entity.level().addFreshEntity(drop);
		} else if (!player.getInventory().add(stack)) {
			player.drop(stack, false);
		}
	}

	/**
	 * Gives the player an ancient scrap of parchment — a {@link BloodStructureHintItem}
	 * pre-configured for the Sanguine Monolith blood structure. The item is dropped
	 * at the NPC entity's position so the player can pick it up naturally.
	 * If the NPC entity cannot be found, the item is given directly to the player's
	 * inventory instead.
	 */
	private static void handleGiveBloodStructureHint(ServerPlayer player, int entityId) {
		ItemStack hint = BloodStructureHintItem.createForStructure(
				ItemInit.blood_structure_hint.get(),
				 ResourceLocation.tryBuild(Hemomancy.MOD_ID, "blood_structure/sanguine_monolith"));

		Entity entity = player.level().getEntity(entityId);
		if (entity != null) {
			Vec3 pos = entity.position();
			ItemEntity drop = new ItemEntity(entity.level(), pos.x, pos.y + 0.5, pos.z, hint);
			entity.level().addFreshEntity(drop);
		} else {
			// Fallback: give directly to inventory, drop if full
			if (!player.getInventory().add(hint)) {
				player.drop(hint, false);
			}
		}

		player.displayClientMessage(
				Component.translatable("hemomancy.dialogue.event.vicar_gives_scrap")
						.withStyle(ChatFormatting.DARK_RED),
				false);
	}

	private static void handleGiveStainedChurchMap(ServerPlayer player, int entityId) {
		ItemStack map = new ItemStack(ItemInit.stained_church_map.get());
		Entity entity = player.level().getEntity(entityId);
		if (entity != null) {
			Vec3 pos = entity.position();
			ItemEntity drop = new ItemEntity(entity.level(), pos.x, pos.y + 0.5, pos.z, map);
			entity.level().addFreshEntity(drop);
		} else if (!player.getInventory().add(map)) {
			player.drop(map, false);
		}

		player.displayClientMessage(
				Component.translatable("hemomancy.dialogue.event.gives_stained_church_map")
						.withStyle(ChatFormatting.DARK_RED),
				false);
	}

	/**
	 * Recruits a Harbinger NPC into the player's bloodline. The NPC's entity
	 * UUID is added as a phantom member — it counts toward the shared blood
	 * pool capacity without needing to be an online player. This allows
	 * single-player users to grow their pool without multiplayer partners.
	 */

	/**
	 * Drops the scout's field notes at the entity's position, then kills the
	 * scout so the player understands she has truly expired. The notes contain
	 * memos for both Harbinger and Unstained paths so that any player can
	 * eventually dictate the correct version into their book.
	 */
	private static void handleScoutGiveNotes(ServerPlayer player, int entityId) {
		ItemStack notes = PreWrittenMemoItem.create(
				ItemInit.scout_field_notes.get(),
				ResourceLocation.tryBuild(Hemomancy.MOD_ID, "annetta_insect_observation"),
				ResourceLocation.tryBuild(Hemomancy.MOD_ID, "annetta_insect_observation_immaculatus"));

		Entity entity = player.level().getEntity(entityId);
		if (entity != null) {
			Vec3 pos = entity.position();
			ItemEntity drop = new ItemEntity(entity.level(), pos.x, pos.y + 0.5, pos.z, notes);
			entity.level().addFreshEntity(drop);
			if (entity instanceof UnstainedScoutEntity scout) {
				scout.setInvulnerable(false);
				scout.kill();
			}
		} else {
			if (!player.getInventory().add(notes)) {
				player.drop(notes, false);
			}
		}

		player.displayClientMessage(
				Component.translatable("hemomancy.dialogue.event.scout_gives_notes")
						.withStyle(ChatFormatting.AQUA),
				false);
	}

	private static void handleRecruitHarbinger(ServerPlayer player, int entityId) {
		HemoCapabilityAccess.getBloodVolume(player).ifPresent(volume -> {
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

			// Check if this NPC is already recruited
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

			// Add the NPC to the bloodline in world-level saved data
			ServerLevel overworld = player.server.overworld();
			BloodlineSavedData savedData = BloodlineSavedData.get(overworld);
			Bloodline updatedLine = savedData.addNpcMember(
					bloodline.getBloodlineUUID(), entity.getUUID(), npcType);

			if (updatedLine != null) {
				// Update the player's local bloodline reference
				volume.setBloodLine(updatedLine);

				// Sync updated bloodline state to client
				PacketHandler.sendToPlayer(player, new BloodVolumeServerPacket(volume));

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
		});
	}

	/**
	 * Removes a recruited Harbinger NPC from the player's bloodline. Only the
	 * bloodline progenitor/leader may expel members.
	 */
	private static void handleExpelHarbinger(ServerPlayer player, int entityId) {
		HemoCapabilityAccess.getBloodVolume(player).ifPresent(volume -> {
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

			if (!bloodline.hasNpcMember(entity.getUUID())) {
				player.displayClientMessage(
						Component.translatable("hemomancy.dialogue.recruit.not_member")
								.withStyle(ChatFormatting.GRAY),
						false);
				return;
			}

			ServerLevel overworld = player.server.overworld();
			BloodlineSavedData savedData = BloodlineSavedData.get(overworld);
			savedData.removeNpcMember(bloodline.getBloodlineUUID(), entity.getUUID(), npcType);
			Bloodline updatedLine = savedData.getBloodline(bloodline.getBloodlineUUID());
			if (updatedLine == null) {
				return;
			}

			for (ServerPlayer online : player.server.getPlayerList().getPlayers()) {
				HemoCapabilityAccess.getBloodVolume(online).ifPresent(memberVolume -> {
					if (updatedLine.hasMember(online.getUUID())) {
						memberVolume.setBloodLine(updatedLine);
						BloodVolumeEvents.syncVolume(online, memberVolume);
					}
				});
			}

			player.displayClientMessage(
					Component.translatable("hemomancy.dialogue.recruit.expel.success", entity.getName())
							.withStyle(ChatFormatting.DARK_RED),
					false);
		});
	}
}
