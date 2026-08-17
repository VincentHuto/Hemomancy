package com.vincenthuto.hemomancy.common.entity.npc.dialogue;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bestiary.SpecimenBestiaryDefinitions;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bestiary.SpecimenBestiaryEvents;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bestiary.SpecimenBestiaryProgress;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodVolumeEvents;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.Bloodline;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodlineSavedData;
import com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.HemomancyDiscoverySource;
import com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.discovery.LiberEntryDefinitions;
import com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.discovery.LiberKnowledgeHelper;
import com.vincenthuto.hemomancy.common.capability.player.unstained.UnstainedProgressEvents;
import com.vincenthuto.hemomancy.common.capability.player.unstained.UnstainedStarterSupplyRules;
import com.vincenthuto.hemomancy.common.entity.summon.MorphlingPolypLayer;
import com.vincenthuto.hemomancy.common.entity.npc.harbinger.HarbingerHermitEntity;
import com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter;
import com.vincenthuto.hemomancy.common.entity.npc.unstained.UnstainedScoutEntity;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.mission.HarbingerArtificerAssignmentHelper;
import com.vincenthuto.hemomancy.common.mission.FirstBloodcraftAssignmentHelper;
import com.vincenthuto.hemomancy.common.mission.FirstSeparationAssignmentHelper;
import com.vincenthuto.hemomancy.common.mission.BodyAnswersAssignmentHelper;
import com.vincenthuto.hemomancy.common.mission.MnemonicReliquaryProgression;
import com.vincenthuto.hemomancy.common.mission.MnemonicRecipeKnowledge;
import com.vincenthuto.hemomancy.common.mission.RedTaxonomyRewardRules;
import com.vincenthuto.hemomancy.common.mission.UnstainedObservanceHelper;
import com.vincenthuto.hemomancy.common.util.SpecimenJarData;
import com.vincenthuto.hemomancy.common.item.shared.PreWrittenMemoItem;
import com.vincenthuto.hemomancy.common.item.shared.MnemonicBlueprintItem;
import com.vincenthuto.hemomancy.common.item.shared.MnemonicBlueprintTarget;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.BloodVolumeServerPacket;
import com.vincenthuto.hemomancy.common.network.particle.SpawnSanguineOmenEffectPacket;
import com.vincenthuto.hemomancy.common.worldgen.FungalGardenTravelHelper;
import com.vincenthuto.hemomancy.common.rite.TempleOathRules;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.EnumArchonPath;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.InitiatoryDegreeEvents;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import com.vincenthuto.hutoslib.common.network.HLPacketHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import java.util.List;

/**
 * Listens for {@link DialogueEvent}s fired by dialogue option selection and
 * executes gameplay-relevant side-effects such as starting a quest line or
 * changing NPC disposition.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public class DialogueEventHandler {
	private static final int BLOOD_SHOTTING_OVERLAY_TICKS = 1200;
	private static final float BLOOD_SHOTTING_OVERLAY_ALPHA = 0.30F;
	private static final String VICAR_CONSECRATION_KIT_CLAIM_KEY =
			"hemomancy.vicar_consecration_kit_claimed";
	private static final String MONOLITH_CORNERSTONE_CLAIM_KEY =
			"hemomancy.monolithic_cornerstone_claimed";

	@SubscribeEvent
	public static void onDialogueOption(DialogueEvent event) {
		ServerPlayer player = event.getPlayer();
		var mnemonistChoice = MnemonistStarterMemoryChoice.fromEventId(event.getEventId());
		if (mnemonistChoice.isPresent()) {
			handleMnemonistStarterMemory(player, event.getEntityId(), mnemonistChoice.get());
			return;
		}
		HarbingerAlchemistDialogueTrees.RedTaxonomySample redTaxonomySample =
				HarbingerAlchemistDialogueTrees.RedTaxonomySample.fromEventId(event.getEventId());
		if (redTaxonomySample != null) {
			handleAlchemistRedTaxonomy(player, redTaxonomySample);
			return;
		}
		if (isAlchemistBestiaryEvent(event.getEventId())) {
			handleAlchemistBestiary(player, event.getEntityId(), event.getEventId());
			return;
		}
		switch (event.getEventId()) {
			case HarbingerMnemonistDialogueTrees.EVENT_RELIQUARY_TAUGHT ->
					MnemonicReliquaryProgression.teach(player);
			case "acolyte_task_gather_ghost_pipe" -> UnstainedObservanceHelper.handle(player,
					UnstainedObservanceHelper.Observance.GATHER_GHOST_PIPE);
			case "acolyte_task_wreath" -> UnstainedObservanceHelper.handle(player,
					UnstainedObservanceHelper.Observance.WEAVE_WREATH);
			case "acolyte_task_hemolytic" -> UnstainedObservanceHelper.handle(player,
					UnstainedObservanceHelper.Observance.PREPARE_HEMOLYTIC);
			case "acolyte_task_consecrate" -> UnstainedObservanceHelper.handle(player,
					UnstainedObservanceHelper.Observance.CONSECRATE_COPPER);
			case "acolyte_task_chalice" -> UnstainedObservanceHelper.handle(player,
					UnstainedObservanceHelper.Observance.OFFER_CHALICE);
			case "zealot_task_still_waters" -> UnstainedObservanceHelper.handle(player,
					UnstainedObservanceHelper.Observance.CONDENSE_STILL_WATERS);
			case "zealot_task_pallid_icon" -> UnstainedObservanceHelper.handle(player,
					UnstainedObservanceHelper.Observance.BEAR_PALLID_ICON);
			case "guardian_task_plating" -> UnstainedObservanceHelper.handle(player,
					UnstainedObservanceHelper.Observance.PLATE_THE_WARD);
			case "guardian_task_bell" -> UnstainedObservanceHelper.handle(player,
					UnstainedObservanceHelper.Observance.RING_THE_PALE_WATCH);
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
			case "hermit_accept_guidance" -> {
				player.displayClientMessage(
						Component.translatable("hemomancy.dialogue.event.hermit_accept_guidance")
								.withStyle(ChatFormatting.DARK_RED),
						false);
			}
			case "hermit_heart_offered" -> {
				Entity entity = player.level().getEntity(event.getEntityId());
				if (entity instanceof HarbingerHermitEntity hermit) {
					TempleOathRules.bless(player, hermit.getUUID());
				}
				player.displayClientMessage(
						Component.translatable("hemomancy.dialogue.event.hermit_heart_offered")
								.withStyle(ChatFormatting.DARK_RED),
						false);
			}
			case "hermit_farewell_die" -> {
				// Find the hermit entity, drop the mnemonic blueprint, then end the hermit
				Entity entity = player.level().getEntity(event.getEntityId());
				if (entity instanceof HarbingerHermitEntity hermit) {
					Vec3 pos = hermit.position();
					// Drop a filled mnemonic blueprint configured for Sanguine Initiation.
					ItemStack blueprint = MnemonicBlueprintItem.create(ItemInit.mnemonic_blueprint.get(),
							new MnemonicBlueprintTarget(MnemonicBlueprintTarget.Type.CARDINAL_RITE,
									ResourceLocation.fromNamespaceAndPath(Hemomancy.MOD_ID,
											"cardinal_rite/sanguine_initiation")));
					ItemEntity drop = new ItemEntity(hermit.level(), pos.x, pos.y + 0.5, pos.z, blueprint);
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
					hermit.beginFarewellDeath();
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
			case HarbingerVicarDialogueTrees.EVENT_HERMIT_ROAD_REPORT -> {
				handleVicarHermitRoadReport(player, event.getEntityId());
			}
			case HarbingerVicarDialogueTrees.EVENT_CLAIM_FIRST_BLOODCRAFT_REWARD -> {
				handleVicarFirstBloodcraftReward(player, event.getEntityId());
			}
			case HarbingerAlchemistDialogueTrees.EVENT_FIRST_SEPARATION_BRIEF -> {
				handleAlchemistFirstSeparationBrief(player);
			}
			case HarbingerAlchemistDialogueTrees.EVENT_FIRST_SEPARATION_CLAIM -> {
				handleAlchemistFirstSeparationReward(player, event.getEntityId());
			}
			case HarbingerAlchemistDialogueTrees.EVENT_BODY_ANSWERS_BRIEF -> {
				handleAlchemistBodyAnswersBrief(player);
			}
			case HarbingerVicarDialogueTrees.EVENT_MASONS_RESPITE_DIRECTIVE -> {
				handleVicarMasonsRespiteDirective(player, event.getEntityId());
			}
			case HarbingerVicarDialogueTrees.EVENT_CONSECRATION_KIT -> {
				handleVicarConsecrationKit(player, event.getEntityId());
			}
			case SanguineMonolithDialogueTrees.EVENT_CORNERSTONE -> {
				handleMonolithCornerstone(player, event.getEntityId());
			}
			case HarbingerArtificerDialogueTrees.EVENT_CLAIM_WORN_VOW_REWARD -> {
				handleArtificerLessonReward(player, event.getEntityId(),
						HarbingerArtificerAssignmentHelper.WORN_VOW_REWARD_CLAIM_KEY,
						HarbingerAdvancementGranter.ADV_ARTIFICER_FIRST_HEMATIC_UPGRADE,
						new ItemStack(ItemInit.hematic_iron_scrap.get(), 4),
						"hemomancy.dialogue.event.artificer_worn_vow_reward_unready",
						"hemomancy.dialogue.event.artificer_worn_vow_reward_known",
						"hemomancy.dialogue.event.artificer_worn_vow_reward_granted");
			}
			case HarbingerArtificerDialogueTrees.EVENT_CLAIM_THREE_ANSWERS_REWARD -> {
				handleArtificerLessonReward(player, event.getEntityId(),
						HarbingerArtificerAssignmentHelper.THREE_ANSWERS_REWARD_CLAIM_KEY,
						HarbingerAdvancementGranter.ADV_ARTIFICER_FIRST_FORK_UPGRADE,
						matchingForkReagentReward(player),
						"hemomancy.dialogue.event.artificer_three_answers_reward_unready",
						"hemomancy.dialogue.event.artificer_three_answers_reward_known",
						"hemomancy.dialogue.event.artificer_three_answers_reward_granted");
			}
			case HarbingerArtificerDialogueTrees.EVENT_CLAIM_CRIMSON_VESTMENT_REWARD -> {
				handleArtificerLessonReward(player, event.getEntityId(),
						HarbingerArtificerAssignmentHelper.CRIMSON_VESTMENT_REWARD_CLAIM_KEY,
						HarbingerAdvancementGranter.ADV_ARTIFICER_FIRST_BLOOD_LUST_UPGRADE,
						new ItemStack(ItemInit.crimson_lacquer.get(), 1),
						"hemomancy.dialogue.event.artificer_crimson_vestment_reward_unready",
						"hemomancy.dialogue.event.artificer_crimson_vestment_reward_known",
						"hemomancy.dialogue.event.artificer_crimson_vestment_reward_granted");
			}
			case HarbingerArtificerDialogueTrees.EVENT_CLAIM_ASSUMED_LIMB_REWARD -> {
				handleArtificerLessonReward(player, event.getEntityId(),
						HarbingerArtificerAssignmentHelper.ASSUMED_LIMB_REWARD_CLAIM_KEY,
						HarbingerAdvancementGranter.ADV_ARTIFICER_FIRST_LIVING_GRAFT,
						new ItemStack(ItemInit.hematic_memory.get(), 1),
						"hemomancy.dialogue.event.artificer_assumed_limb_reward_unready",
						"hemomancy.dialogue.event.artificer_assumed_limb_reward_known",
						"hemomancy.dialogue.event.artificer_assumed_limb_reward_granted");
			}
			case HarbingerArtificerDialogueTrees.EVENT_CLAIM_HEMATIC_IRON_FITTING -> {
				handleArtificerFittingClaim(player, event.getEntityId(),
						claimant -> HarbingerArtificerAssignmentHelper.earnedHematicIronFitting(claimant),
						claimant -> HarbingerArtificerAssignmentHelper.tryGrantHematicIronFitting(claimant),
						"hemomancy.dialogue.event.artificer_hematic_iron_fitting_unready",
						"hemomancy.dialogue.event.artificer_hematic_iron_fitting_known",
						"hemomancy.dialogue.event.artificer_hematic_iron_fitting_granted",
						"hemomancy.dialogue.event.artificer_hematic_iron_fitting_reissued");
			}
			case HarbingerArtificerDialogueTrees.EVENT_CLAIM_FORK_FITTING -> {
				handleArtificerFittingClaim(player, event.getEntityId(),
						claimant -> HarbingerArtificerAssignmentHelper.earnedForkFitting(claimant),
						claimant -> HarbingerArtificerAssignmentHelper.tryGrantForkFitting(claimant),
						"hemomancy.dialogue.event.artificer_fork_fitting_unready",
						"hemomancy.dialogue.event.artificer_fork_fitting_known",
						"hemomancy.dialogue.event.artificer_fork_fitting_granted",
						"hemomancy.dialogue.event.artificer_fork_fitting_reissued");
			}
			case HarbingerArtificerDialogueTrees.EVENT_CLAIM_BLOOD_LUST_FITTING -> {
				handleArtificerFittingClaim(player, event.getEntityId(),
						claimant -> HarbingerArtificerAssignmentHelper.earnedBloodLustFitting(claimant),
						claimant -> HarbingerArtificerAssignmentHelper.tryGrantBloodLustFitting(claimant),
						"hemomancy.dialogue.event.artificer_blood_lust_fitting_unready",
						"hemomancy.dialogue.event.artificer_blood_lust_fitting_known",
						"hemomancy.dialogue.event.artificer_blood_lust_fitting_granted",
						"hemomancy.dialogue.event.artificer_blood_lust_fitting_reissued");
			}
			case HarbingerArtificerDialogueTrees.EVENT_CLAIM_D7_FITTING -> {
				handleArtificerFittingClaim(player, event.getEntityId(),
						claimant -> HarbingerArtificerAssignmentHelper.earnedD7Fitting(claimant),
						claimant -> HarbingerArtificerAssignmentHelper.tryGrantD7Fitting(claimant),
						"hemomancy.dialogue.event.artificer_d7_fitting_unready",
						"hemomancy.dialogue.event.artificer_d7_fitting_known",
						"hemomancy.dialogue.event.artificer_d7_fitting_granted",
						"hemomancy.dialogue.event.artificer_d7_fitting_reissued");
			}
			case HarbingerArtificerDialogueTrees.EVENT_CLAIM_LIVING_ARSENAL_FITTING -> {
				handleArtificerFittingClaim(player, event.getEntityId(),
						claimant -> HarbingerArtificerAssignmentHelper.earnedLivingArsenalFitting(claimant),
						claimant -> HarbingerArtificerAssignmentHelper.tryGrantLivingArsenalFitting(claimant),
						"hemomancy.dialogue.event.artificer_living_arsenal_fitting_unready",
						"hemomancy.dialogue.event.artificer_living_arsenal_fitting_known",
						"hemomancy.dialogue.event.artificer_living_arsenal_fitting_granted",
						"hemomancy.dialogue.event.artificer_living_arsenal_fitting_reissued");
			}
			case HarbingerCicatrixAnchoriteDialogueTrees.EVENT_FIRST_LESSON -> {
				handleVeinMasonFirstLesson(player, event.getEntityId());
			}
			case HarbingerCicatrixAnchoriteDialogueTrees.EVENT_CONTINUATION_REWARD -> {
				handleVeinMasonContinuationReward(player, event.getEntityId());
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
				player.getPersistentData().remove(FungalGardenTravelHelper.REVELATION_CHOICE_PENDING);
				// Archon chose to carry the truth in silence — they turn back from the Eighth Degree.
				HemoCapabilityAccess.getInitiatoryDegree(player).ifPresent(degree -> {
					degree.setArchonPath(EnumArchonPath.SILENT_PENDING);
					InitiatoryDegreeEvents.syncDegree(player, degree);
				});
				player.displayClientMessage(
						Component.translatable("hemomancy.dialogue.event.archon_choice_silence")
								.withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC),
						false);
			}
			case "archon_choice_eighth_degree" -> {
				player.getPersistentData().remove(FungalGardenTravelHelper.REVELATION_CHOICE_PENDING);
				// Archon chose to pursue the Eighth Degree — the Apotheos path opens.
				player.getPersistentData().putString(
						FungalGardenTravelHelper.ARCHON_CHOICE_KEY,
						FungalGardenTravelHelper.ARCHON_CHOICE_APOTHEOS);
				HemoCapabilityAccess.getInitiatoryDegree(player).ifPresent(degree -> {
					degree.setArchonPath(EnumArchonPath.APOTHEOS_PENDING);
					InitiatoryDegreeEvents.syncDegree(player, degree);
				});
				player.displayClientMessage(
						Component.translatable("hemomancy.dialogue.event.archon_choice_eighth_degree")
								.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC),
						false);
			}
			case "give_sanguine_monolith_blueprint" -> {
				handleGiveSanguineMonolithBlueprint(player, event.getEntityId());
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
			case HarbingerMnemonistDialogueTrees.EVENT_WOVEN_VESSEL_TURN_IN ->
					handleMnemonistWovenVessel(player, event.getEntityId());
			default -> {
				// Unknown event — log for development
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

	private static void handleVicarHermitRoadReport(ServerPlayer player, int entityId) {
		if (!HarbingerAdvancementGranter.hasAdvancement(player,
				HarbingerAdvancementGranter.ADV_HERMIT_ROAD_FIRST_REMNANT)) {
			player.displayClientMessage(
					Component.translatable("hemomancy.dialogue.event.vicar_hermit_road_unproven")
							.withStyle(ChatFormatting.GRAY),
					false);
			return;
		}
		if (HarbingerAdvancementGranter.hasAdvancement(player,
				HarbingerAdvancementGranter.ADV_HERMIT_ROAD_REPORTED)) {
			player.displayClientMessage(
					Component.translatable("hemomancy.dialogue.event.vicar_hermit_road_known")
							.withStyle(ChatFormatting.GRAY),
					false);
			return;
		}

		HarbingerAdvancementGranter.grantIfNotDone(player,
				HarbingerAdvancementGranter.ADV_HERMIT_ROAD_REPORTED);
		giveOrDropAtEntity(player, entityId, new ItemStack(BlockInit.befouling_ash_trail.get(), 4));
		player.displayClientMessage(
				Component.translatable("hemomancy.dialogue.event.vicar_hermit_road_reported")
						.withStyle(ChatFormatting.DARK_RED),
				false);
	}

	private static void handleVicarFirstBloodcraftReward(ServerPlayer player, int entityId) {
		if (!FirstBloodcraftAssignmentHelper.canClaim(player)) {
			String messageKey = FirstBloodcraftAssignmentHelper.isClaimed(player)
					? "hemomancy.dialogue.event.vicar_first_bloodcraft_reward_known"
					: "hemomancy.dialogue.event.vicar_first_bloodcraft_reward_unready";
			player.displayClientMessage(
					Component.translatable(messageKey).withStyle(ChatFormatting.GRAY),
					false);
			return;
		}

		if (!FirstBloodcraftAssignmentHelper.markClaimed(player)) {
			player.displayClientMessage(
					Component.translatable("hemomancy.dialogue.event.vicar_first_bloodcraft_reward_claim_failed")
							.withStyle(ChatFormatting.RED), false);
			return;
		}
		for (ItemStack stack : FirstBloodcraftAssignmentHelper.rewardStacks()) {
			giveOrDropAtEntity(player, entityId, stack);
		}
		player.displayClientMessage(
				Component.translatable("hemomancy.dialogue.event.vicar_first_bloodcraft_reward_granted")
						.withStyle(ChatFormatting.DARK_RED),
				false);
	}

	private static void handleAlchemistFirstSeparationBrief(ServerPlayer player) {
		if (!FirstSeparationAssignmentHelper.canBrief(player)) return;
		if (!FirstSeparationAssignmentHelper.markBriefed(player)) return;
		FirstSeparationAssignmentHelper.giveBriefingSupplies(player);
	}

	private static void handleAlchemistFirstSeparationReward(ServerPlayer player, int entityId) {
		if (!FirstSeparationAssignmentHelper.canClaim(player)) return;
		if (!FirstSeparationAssignmentHelper.markClaimed(player)) return;
		for (ItemStack stack : FirstSeparationAssignmentHelper.rewardStacks()) {
			giveOrDropAtEntity(player, entityId, stack);
		}
	}

	private static void handleVicarMasonsRespiteDirective(ServerPlayer player, int entityId) {
		if (HemoCapabilityAccess.getPlayerDegreeNumber(player) < 4) {
			player.displayClientMessage(
					Component.translatable("hemomancy.dialogue.event.vicar_masons_respite_unready")
							.withStyle(ChatFormatting.GRAY),
					false);
			return;
		}
		if (HarbingerAdvancementGranter.isVicarMasonsRespiteDirective(player)) {
			player.displayClientMessage(
					Component.translatable("hemomancy.dialogue.event.vicar_masons_respite_known")
							.withStyle(ChatFormatting.GRAY),
					false);
			return;
		}
		giveOrDropAtEntity(player, entityId, new ItemStack(ItemInit.masons_respite_map.get()));
		HarbingerAdvancementGranter.grantIfNotDone(player,
				HarbingerAdvancementGranter.ADV_VICAR_MASONS_RESPITE_DIRECTIVE);
		player.displayClientMessage(
				Component.translatable("hemomancy.dialogue.event.vicar_masons_respite_map")
						.withStyle(ChatFormatting.DARK_RED),
				false);
	}

	private static void handleVicarConsecrationKit(ServerPlayer player, int entityId) {
		if (HemoCapabilityAccess.getPlayerDegreeNumber(player) < 5) {
			player.displayClientMessage(
					Component.translatable("hemomancy.dialogue.event.vicar_consecration_kit_unready")
							.withStyle(ChatFormatting.GRAY),
					false);
			return;
		}
		if (player.getPersistentData().getBoolean(VICAR_CONSECRATION_KIT_CLAIM_KEY)) {
			player.displayClientMessage(
					Component.translatable("hemomancy.dialogue.event.vicar_consecration_kit_known")
							.withStyle(ChatFormatting.GRAY),
					false);
			return;
		}
		giveOrDropAtEntity(player, entityId, new ItemStack(ItemInit.vicars_consecration_kit.get()));
		player.getPersistentData().putBoolean(VICAR_CONSECRATION_KIT_CLAIM_KEY, true);
		player.displayClientMessage(
				Component.translatable("hemomancy.dialogue.event.vicar_consecration_kit_granted")
						.withStyle(ChatFormatting.DARK_RED),
				false);
	}

	private static void handleMonolithCornerstone(ServerPlayer player, int entityId) {
		if (HemoCapabilityAccess.getPlayerDegreeNumber(player) < 7) {
			player.displayClientMessage(
					Component.translatable("hemomancy.dialogue.event.monolith_cornerstone_unready")
							.withStyle(ChatFormatting.GRAY),
					false);
			return;
		}
		if (player.getPersistentData().getBoolean(MONOLITH_CORNERSTONE_CLAIM_KEY)) {
			player.displayClientMessage(
					Component.translatable("hemomancy.dialogue.event.monolith_cornerstone_known")
							.withStyle(ChatFormatting.GRAY),
					false);
			return;
		}
		giveOrDropAtEntity(player, entityId, new ItemStack(ItemInit.monolithic_cornerstone.get()));
		player.getPersistentData().putBoolean(MONOLITH_CORNERSTONE_CLAIM_KEY, true);
		player.displayClientMessage(
				Component.translatable("hemomancy.dialogue.event.monolith_cornerstone_granted")
						.withStyle(ChatFormatting.DARK_RED),
				false);
	}

	private static void handleArtificerLessonReward(ServerPlayer player, int entityId, String claimKey,
			ResourceLocation prerequisite, ItemStack reward, String unreadyKey, String knownKey, String grantedKey) {
		if (!HarbingerAdvancementGranter.hasAdvancement(player, prerequisite)) {
			player.displayClientMessage(
					Component.translatable(unreadyKey).withStyle(ChatFormatting.GRAY),
					false);
			return;
		}
		if (HarbingerArtificerAssignmentHelper.isArtificerLessonRewardClaimed(player, claimKey)) {
			player.displayClientMessage(
					Component.translatable(knownKey).withStyle(ChatFormatting.GRAY),
					false);
			return;
		}

		giveOrDropAtEntity(player, entityId, reward);
		HarbingerArtificerAssignmentHelper.markArtificerLessonRewardClaimed(player, claimKey);
		player.displayClientMessage(
				Component.translatable(grantedKey).withStyle(ChatFormatting.DARK_RED),
				false);
	}

	private static void handleArtificerFittingClaim(ServerPlayer player, int entityId,
			ArtificerFittingStackProvider earned, ArtificerFittingStackProvider grant,
			String unreadyKey, String knownKey, String grantedKey, String reissuedKey) {
		ItemStack fitting = earned.stackFor(player);
		if (!fitting.isEmpty()) {
			if (!playerHasFitting(player, fitting)) {
				giveOrDropAtEntity(player, entityId, fitting.copy());
				player.displayClientMessage(
						Component.translatable(reissuedKey).withStyle(ChatFormatting.DARK_RED),
						false);
				return;
			}
			player.displayClientMessage(
					Component.translatable(knownKey).withStyle(ChatFormatting.GRAY),
					false);
			return;
		}

		fitting = grant.stackFor(player);
		if (fitting.isEmpty()) {
			player.displayClientMessage(
					Component.translatable(unreadyKey).withStyle(ChatFormatting.GRAY),
					false);
			return;
		}
		giveOrDropAtEntity(player, entityId, fitting.copy());
		player.displayClientMessage(
				Component.translatable(grantedKey).withStyle(ChatFormatting.DARK_RED),
				false);
	}

	private static boolean playerHasFitting(ServerPlayer player, ItemStack fitting) {
		if (fitting.isEmpty()) {
			return false;
		}
		for (ItemStack stack : player.getInventory().items) {
			if (stack.is(fitting.getItem())) {
				return true;
			}
		}
		for (ItemStack stack : player.getInventory().armor) {
			if (stack.is(fitting.getItem())) {
				return true;
			}
		}
		for (ItemStack stack : player.getInventory().offhand) {
			if (stack.is(fitting.getItem())) {
				return true;
			}
		}
		return HemoCapabilityAccess.getEquipment(player)
				.map(equipment -> {
					for (int slot = 0; slot < equipment.getSlots(); slot++) {
						if (equipment.getStackInSlot(slot).is(fitting.getItem())) {
							return true;
						}
					}
					return false;
				})
				.orElse(false);
	}

	private static ItemStack matchingForkReagentReward(ServerPlayer player) {
		if (wearsAny(player, ItemInit.chitinite_helm.get(), ItemInit.chitinite_chestplate.get(),
				ItemInit.chitinite_leggings.get(), ItemInit.chitinite_boots.get())) {
			return new ItemStack(ItemInit.sclerotic_oleum.get());
		}
		if (wearsAny(player, ItemInit.prismatic_helm.get(), ItemInit.prismatic_chestplate.get(),
				ItemInit.prismatic_leggings.get(), ItemInit.prismatic_boots.get())) {
			return new ItemStack(ItemInit.chromatic_sublimate.get());
		}
		return new ItemStack(ItemInit.aculeate_vitriol.get());
	}

	private static boolean wearsAny(ServerPlayer player, Item... items) {
		for (EquipmentSlot slot : List.of(EquipmentSlot.HEAD, EquipmentSlot.CHEST,
				EquipmentSlot.LEGS, EquipmentSlot.FEET)) {
			ItemStack worn = player.getItemBySlot(slot);
			for (Item item : items) {
				if (worn.is(item)) {
					return true;
				}
			}
		}
		return false;
	}

	@FunctionalInterface
	private interface ArtificerFittingStackProvider {
		ItemStack stackFor(ServerPlayer player);
	}

	private static void handleVeinMasonFirstLesson(ServerPlayer player, int entityId) {
		int degree = HemoCapabilityAccess.getPlayerDegreeNumber(player);
		boolean activeBlood = HemoCapabilityAccess.getBloodVolume(player)
				.map(volume -> volume.isActive())
				.orElse(false);
		boolean purifying = HemoCapabilityAccess.getUnstainedProgress(player)
				.map(progress -> progress.hasBegunPurification())
				.orElse(false);
		boolean clarity = HemoCapabilityAccess.getUnstainedProgress(player)
				.map(progress -> progress.hasClarityUnlocked())
				.orElse(false);
		if (degree < 4 || !activeBlood || purifying || clarity) {
			player.displayClientMessage(
					Component.translatable("hemomancy.dialogue.event.vein_mason_unready")
							.withStyle(ChatFormatting.GRAY),
					false);
			return;
		}
		if (HarbingerAdvancementGranter.isVeinMasonFirstLesson(player)) {
			player.displayClientMessage(
					Component.translatable("hemomancy.dialogue.event.vein_mason_known")
							.withStyle(ChatFormatting.GRAY),
					false);
			return;
		}

		VeinMasonScarLesson.Lesson lesson = VeinMasonScarLesson.forPlayer(player);
		giveOrDropAtEntity(player, entityId, lesson.patternStack());
		giveOrDropAtEntity(player, entityId, new ItemStack(ItemInit.scar_blank.get()));
		giveOrDropAtEntity(player, entityId, new ItemStack(lesson.catalyst()));
		if (!hasAnyKnapper(player)) {
			giveOrDropAtEntity(player, entityId, new ItemStack(ItemInit.hematic_iron_knapper.get()));
		}
		HarbingerAdvancementGranter.grantIfNotDone(player,
				HarbingerAdvancementGranter.ADV_VEIN_MASON_FIRST_LESSON);
		player.displayClientMessage(
				Component.translatable("hemomancy.dialogue.event.vein_mason_first_lesson")
						.withStyle(ChatFormatting.DARK_RED),
				false);
	}

	private static void handleVeinMasonContinuationReward(ServerPlayer player, int entityId) {
		if (!HarbingerAdvancementGranter.isVeinMasonFirstEffigyLoadout(player)) {
			player.displayClientMessage(
					Component.translatable("hemomancy.dialogue.event.vein_mason_reward_unready")
							.withStyle(ChatFormatting.GRAY),
					false);
			return;
		}
		if (HarbingerAdvancementGranter.isVeinMasonRewardClaimed(player)) {
			player.displayClientMessage(
					Component.translatable("hemomancy.dialogue.event.vein_mason_reward_known")
							.withStyle(ChatFormatting.GRAY),
					false);
			return;
		}

		VeinMasonScarLesson.Lesson lesson = VeinMasonScarLesson.continuationForPlayer(player);
		giveOrDropAtEntity(player, entityId, lesson.patternStack());
		giveOrDropAtEntity(player, entityId, new ItemStack(ItemInit.scar_blank.get()));
		giveOrDropAtEntity(player, entityId, new ItemStack(lesson.catalyst()));
		giveOrDropAtEntity(player, entityId, new ItemStack(ItemInit.runic_motif_paper.get(), 4));
		HarbingerAdvancementGranter.grantIfNotDone(player,
				HarbingerAdvancementGranter.ADV_VEIN_MASON_REWARD_CLAIMED);
		player.displayClientMessage(
				Component.translatable("hemomancy.dialogue.event.vein_mason_reward_claimed")
						.withStyle(ChatFormatting.DARK_RED),
				false);
	}

	private static void handleAlchemistRedTaxonomy(ServerPlayer player,
			HarbingerAlchemistDialogueTrees.RedTaxonomySample sample) {
		if (HemoCapabilityAccess.getPlayerDegreeNumber(player) < 2) {
			player.displayClientMessage(
					Component.translatable("hemomancy.dialogue.event.alchemist_red_taxonomy_unready")
							.withStyle(ChatFormatting.GRAY),
					false);
			return;
		}

		ItemStack held = player.getMainHandItem();
		if (!sample.matches(held)) {
			player.displayClientMessage(
					Component.translatable("hemomancy.dialogue.event.alchemist_red_taxonomy_missing_sample")
							.withStyle(ChatFormatting.GRAY),
					false);
			return;
		}

		if (HarbingerAdvancementGranter.hasAdvancement(player, sample.advancement())) {
			player.displayClientMessage(
					Component.translatable("hemomancy.dialogue.event.alchemist_red_taxonomy_known",
									held.getHoverName())
							.withStyle(ChatFormatting.GRAY),
					false);
			return;
		}

		boolean taxonomyWasComplete = HarbingerAdvancementGranter.isRedTaxonomyComplete(player);
		int countBefore = HarbingerAdvancementGranter.getRedTaxonomySpecimenCount(player);
		if (!player.isCreative()) {
			held.shrink(1);
		}
		HarbingerAdvancementGranter.grantIfNotDone(player, sample.advancement());
		int count = HarbingerAdvancementGranter.getRedTaxonomySpecimenCount(player);
		if (RedTaxonomyRewardRules.grantsFirstFieldVial(countBefore, count > countBefore)) {
			giveOrDropAtEntity(player, player.getId(), new ItemStack(ItemInit.bloody_vial.get()));
			player.displayClientMessage(Component.translatable(
					"hemomancy.dialogue.event.alchemist_red_taxonomy_first_vial")
					.withStyle(ChatFormatting.DARK_RED), false);
		}
		boolean taxonomyCompletedNow = count >= 4 && !taxonomyWasComplete;
		if (taxonomyCompletedNow) {
			HarbingerAdvancementGranter.grantIfNotDone(player,
					HarbingerAdvancementGranter.ADV_RED_TAXONOMY_COMPLETE);
			giveOrDropAtEntity(player, player.getId(), new ItemStack(BlockInit.specimen_jar.get(), 4));
			giveOrDropAtEntity(player, player.getId(), new ItemStack(ItemInit.bloody_vial.get(), 4));
		}

		player.displayClientMessage(
				Component.translatable("hemomancy.dialogue.event.alchemist_red_taxonomy_recorded",
								held.getHoverName(), count)
						.withStyle(ChatFormatting.DARK_RED),
				false);
		if (taxonomyCompletedNow) {
			player.displayClientMessage(Component.translatable(
					"hemomancy.dialogue.event.alchemist_red_taxonomy_reward")
					.withStyle(ChatFormatting.DARK_RED), false);
		}
	}

	private static boolean isAlchemistBestiaryEvent(String eventId) {
		return HarbingerAlchemistDialogueTrees.EVENT_BESTIARY_RECORD.equals(eventId)
				|| HarbingerAlchemistDialogueTrees.EVENT_BESTIARY_SURRENDER.equals(eventId)
				|| (eventId != null && eventId.startsWith(
						HarbingerAlchemistDialogueTrees.EVENT_BESTIARY_SURRENDER_MORPHLING_PREFIX));
	}

	private static void handleAlchemistBestiary(ServerPlayer player, int entityId, String eventId) {
		if (HemoCapabilityAccess.getPlayerDegreeNumber(player) < 2) {
			player.displayClientMessage(
					Component.translatable("hemomancy.dialogue.event.alchemist_bestiary_unready")
							.withStyle(ChatFormatting.GRAY),
					false);
			return;
		}

		ItemStack held = player.getMainHandItem();
		if (!SpecimenJarData.hasSpecimen(held)) {
			player.displayClientMessage(
					Component.translatable("hemomancy.dialogue.event.alchemist_bestiary_missing_specimen")
							.withStyle(ChatFormatting.GRAY),
					false);
			return;
		}

		var specimen = SpecimenJarData.getSpecimen(held);
		ResourceLocation specimenId = SpecimenJarData.getSpecimenEntityId(specimen).orElse(null);
		Component specimenName = SpecimenJarData.getSpecimenName(specimen);
		if (!SpecimenBestiaryDefinitions.isResearchSpecimen(specimenId)) {
			player.displayClientMessage(
					Component.translatable("hemomancy.dialogue.event.alchemist_bestiary_not_research",
									specimenName)
							.withStyle(ChatFormatting.GRAY),
					false);
			return;
		}

		var progress = HemoCapabilityAccess.requireSpecimenBestiary(player);
		if (HarbingerAlchemistDialogueTrees.EVENT_BESTIARY_RECORD.equals(eventId)) {
			boolean newSpecimen = progress.recordSpecimen(specimenId);
			int newLayers = 0;
			for (MorphlingPolypLayer layer : SpecimenJarData.getMorphlingLayers(specimen)) {
				if (progress.recordMorphlingLayer(layer)) {
					newLayers++;
				}
			}
			player.displayClientMessage(
					Component.translatable(newSpecimen || newLayers > 0
									? SpecimenBestiaryDefinitions.recordedDialogueKey(specimenId)
									: "hemomancy.dialogue.event.alchemist_bestiary_known",
							specimenName, progress.recordedSpecimenCount(),
							SpecimenBestiaryDefinitions.totalResearchSpecimens())
							.withStyle(newSpecimen || newLayers > 0 ? ChatFormatting.DARK_RED : ChatFormatting.GRAY),
					false);
			SpecimenBestiaryEvents.sync(player, progress);
			return;
		}

		MorphlingPolypLayer requestedLayer = morphlingLayerFromEvent(eventId);
		if (requestedLayer != null) {
			handleAlchemistPolypSurrender(player, entityId, held, specimen, specimenId, requestedLayer, progress);
			return;
		}

		progress.recordSpecimen(specimenId);
		progress.surrenderSpecimen(specimenId);
		ItemStack reward = SpecimenBestiaryDefinitions.createSurrenderReward(specimenId);
		SpecimenJarData.clearSpecimen(held);
		if (!reward.isEmpty()) {
			giveOrDropAtEntity(player, entityId, reward);
		}
		player.displayClientMessage(
				Component.translatable(SpecimenBestiaryDefinitions.surrenderedDialogueKey(specimenId),
								specimenName, reward.getHoverName())
						.withStyle(ChatFormatting.DARK_RED),
				false);
		SpecimenBestiaryEvents.sync(player, progress);
	}

	private static void handleAlchemistPolypSurrender(ServerPlayer player, int entityId, ItemStack held,
			net.minecraft.nbt.CompoundTag specimen, ResourceLocation specimenId, MorphlingPolypLayer requestedLayer,
			SpecimenBestiaryProgress progress) {
		if (!SpecimenJarData.getMorphlingLayers(specimen).contains(requestedLayer)) {
			player.displayClientMessage(
					Component.translatable("hemomancy.dialogue.event.alchemist_bestiary_layer_missing")
							.withStyle(ChatFormatting.GRAY),
					false);
			return;
		}
		ItemStack morphling = SpecimenBestiaryDefinitions.createWildBoundMorphling(requestedLayer);
		if (morphling.isEmpty()) {
			return;
		}
		progress.recordSpecimen(specimenId);
		progress.recordMorphlingLayer(requestedLayer);
		progress.surrenderSpecimen(specimenId);
		SpecimenJarData.clearSpecimen(held);
		giveOrDropAtEntity(player, entityId, morphling);
		player.displayClientMessage(
				Component.translatable("hemomancy.dialogue.event.alchemist_bestiary_polyp_surrendered",
								morphling.getHoverName())
						.withStyle(ChatFormatting.DARK_RED),
				false);
		SpecimenBestiaryEvents.sync(player, progress);
	}

	private static MorphlingPolypLayer morphlingLayerFromEvent(String eventId) {
		if (eventId == null || !eventId.startsWith(
				HarbingerAlchemistDialogueTrees.EVENT_BESTIARY_SURRENDER_MORPHLING_PREFIX)) {
			return null;
		}
		String layerName = eventId.substring(
				HarbingerAlchemistDialogueTrees.EVENT_BESTIARY_SURRENDER_MORPHLING_PREFIX.length());
		for (MorphlingPolypLayer layer : MorphlingPolypLayer.values()) {
			if (layer.serializedName().equals(layerName)) {
				return layer;
			}
		}
		return null;
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

	private static void handleMnemonistWovenVessel(ServerPlayer player, int entityId) {
		int degree = HemoCapabilityAccess.getPlayerDegreeNumber(player);
		boolean purifying = HemoCapabilityAccess.getUnstainedProgress(player)
				.map(progress -> progress.hasBegunPurification())
				.orElse(false);
		boolean clarity = HemoCapabilityAccess.getUnstainedProgress(player)
				.map(progress -> progress.hasClarityUnlocked())
				.orElse(false);

		if (degree < 3 || purifying || clarity) {
			player.displayClientMessage(
					Component.translatable("hemomancy.dialogue.event.mnemonist_woven_vessel_unready")
							.withStyle(ChatFormatting.GRAY),
					false);
			return;
		}
		if (HarbingerAdvancementGranter.isMnemonistWovenVesselComplete(player)) {
			MnemonicRecipeKnowledge.awardCatalogue(player);
			player.displayClientMessage(
					Component.translatable("hemomancy.dialogue.event.mnemonist_woven_vessel_known")
							.withStyle(ChatFormatting.GRAY),
					false);
			return;
		}
		if (!hasItemCount(player, ItemInit.hematic_memory.get(), 1)) {
			player.displayClientMessage(
					Component.translatable("hemomancy.dialogue.event.mnemonist_woven_vessel_missing_memory")
							.withStyle(ChatFormatting.GRAY),
					false);
			return;
		}
		if (!hasItemCount(player, Items.BOOK, 1)
				|| !hasItemCount(player, Items.INK_SAC, 1)
				|| !hasItemCount(player, Items.PAPER, 3)) {
			player.displayClientMessage(
					Component.translatable("hemomancy.dialogue.event.mnemonist_woven_vessel_missing_archive")
							.withStyle(ChatFormatting.GRAY),
					false);
			return;
		}

		if (!player.isCreative()) {
			consumeItemCount(player, Items.BOOK, 1);
			consumeItemCount(player, Items.INK_SAC, 1);
			consumeItemCount(player, Items.PAPER, 3);
		}
		giveOrDropAtEntity(player, entityId, new ItemStack(ItemInit.bleeding_bulb.get()));
		giveOrDropAtEntity(player, entityId, new ItemStack(ItemInit.vivacious_enzyme.get()));
		HarbingerAdvancementGranter.grantIfNotDone(player,
				HarbingerAdvancementGranter.ADV_MNEMONIST_WOVEN_VESSEL_COMPLETE);
		MnemonicReliquaryProgression.teach(player);
		MnemonicRecipeKnowledge.awardCatalogue(player);
		if (HarbingerAdvancementGranter.isMnemonistFirstWeaveComplete(player)) {
			HarbingerAdvancementGranter.grantIfNotDone(player,
					HarbingerAdvancementGranter.ADV_MNEMONIST_WOVEN_VESSEL_FINISHED);
		}
		player.displayClientMessage(
				Component.translatable("hemomancy.dialogue.event.mnemonist_woven_vessel_complete")
						.withStyle(ChatFormatting.DARK_RED),
				false);
	}

	private static boolean hasItemCount(ServerPlayer player, Item item, int needed) {
		int count = 0;
		for (ItemStack stack : player.getInventory().items) {
			if (stack.is(item)) {
				count += stack.getCount();
				if (count >= needed) {
					return true;
				}
			}
		}
		return false;
	}

	private static boolean hasAnyKnapper(ServerPlayer player) {
		for (ItemStack stack : player.getInventory().items) {
			if (stack.getItem() instanceof com.vincenthuto.hutoslib.common.item.ItemKnapper) {
				return true;
			}
		}
		return false;
	}

	private static void consumeItemCount(ServerPlayer player, Item item, int amount) {
		int remaining = amount;
		for (ItemStack stack : player.getInventory().items) {
			if (remaining <= 0) {
				return;
			}
			if (stack.is(item)) {
				int consumed = Math.min(remaining, stack.getCount());
				stack.shrink(consumed);
				remaining -= consumed;
			}
		}
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
	 * Gives the player a mnemonic blueprint pre-configured for the Sanguine Monolith
	 * blood structure. The item is dropped
	 * at the NPC entity's position so the player can pick it up naturally.
	 * If the NPC entity cannot be found, the item is given directly to the player's
	 * inventory instead.
	 */
	private static void handleGiveSanguineMonolithBlueprint(ServerPlayer player, int entityId) {
		ItemStack hint = MnemonicBlueprintItem.create(ItemInit.mnemonic_blueprint.get(),
				new MnemonicBlueprintTarget(MnemonicBlueprintTarget.Type.BLOOD_STRUCTURE,
						ResourceLocation.tryBuild(Hemomancy.MOD_ID, "blood_structure/sanguine_monolith")));

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

	private static void handleAlchemistBodyAnswersBrief(ServerPlayer player) {
		if (!BodyAnswersAssignmentHelper.canBrief(player)) return;
		if (!BodyAnswersAssignmentHelper.markBriefed(player)) return;
		BodyAnswersAssignmentHelper.giveBriefingSupplies(player);
		MnemonicRecipeKnowledge.awardStarter(player);
	}

	private static void grantUnstainedStarterSupply(ServerPlayer player) {
		HemoCapabilityAccess.getUnstainedProgress(player).ifPresent(progress -> {
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
				giveOrDrop(player, new ItemStack(ItemInit.hemolytic_solution.get(), grant.solutionCount()));
			}
			if (grant.guide()) {
				giveOrDrop(player, new ItemStack(ItemInit.liber_immaculatus.get()));
			}
			progress.setClaimedChurchStarterSupply(true);
			UnstainedProgressEvents.syncProgress(player, progress);
			player.displayClientMessage(Component.translatable(
					"hemomancy.dialogue.event.unstained_starter_granted")
					.withStyle(ChatFormatting.AQUA), false);
		});
	}

	private static void giveOrDrop(ServerPlayer player, ItemStack stack) {
		if (!player.getInventory().add(stack)) player.drop(stack, false);
	}
}
