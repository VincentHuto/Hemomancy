package com.vincenthuto.hemomancy.common.entity.npc.dialogue.route;

import com.vincenthuto.hemomancy.common.entity.npc.harbinger.HarbingerVicarEntity;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.HemomancyDiscoverySource;
import com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.discovery.LiberEntryDefinitions;
import com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.discovery.LiberKnowledgeHelper;
import com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.item.shared.MnemonicBlueprintItem;
import com.vincenthuto.hemomancy.common.item.shared.MnemonicBlueprintTarget;
import com.vincenthuto.hemomancy.common.mission.shared.OwedRewards;
import com.vincenthuto.hemomancy.common.mission.vicar.FirstBloodcraftAssignment;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.particle.SpawnSanguineOmenEffectPacket;
import com.vincenthuto.hemomancy.common.util.ProgressionGates;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.*;

import java.util.List;

/**
 * The Vicar's dialogue events: early initiation, the Voyager report, blood-shotting, the Hermit road report, the
 * first bloodcraft reward, blueprints, the Mason's Respite directive, the church map (the Zealot hands it out too)
 * and a Brine-Sealed Letter.
 */
final class VicarDialogueEvents {
	private VicarDialogueEvents() {
	}

	private static final float BLOOD_SHOTTING_OVERLAY_ALPHA = 0.30F;

	private static final int BLOOD_SHOTTING_OVERLAY_TICKS = 1200;

	/** Handles the event when it is one of this NPC's; false lets the next route try. */
	public static boolean handle(DialogueEvent event) {
		if (CoreLoopLessonRoutes.teach(event, CoreLoopLessons.VICAR, HarbingerVicarEntity.class)) return true;
		ServerPlayer player = event.getPlayer();
		switch (event.getEventId()) {
		case VoyagerIntroductionDialogue.REPORT -> event.setRewardDelivered(
				com.vincenthuto.hemomancy.common.mission.vicar.VoyagerIntroduction.report(
						player, player.level().getEntity(event.getEntityId())));
		case VoyagerIntroductionDialogue.BEARING -> event.setRewardDelivered(
				com.vincenthuto.hemomancy.common.mission.vicar.VoyagerIntroduction.tellBearing(
						player, player.level().getEntity(event.getEntityId())));
		case HarbingerVicarDialogueTrees.EVENT_LEAVE_BLOODLINE -> event.setRewardDelivered(
				com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodlineMembership.leave(player));
		case HarbingerVicarDialogueTrees.EVENT_BLOOD_SHOTTING -> {
			handleVicarBloodShotting(player);
		}
		case HarbingerVicarDialogueTrees.EVENT_HERMIT_ROAD_REPORT -> {
			handleVicarHermitRoadReport(player, event.getEntityId());
		}
		case HarbingerVicarDialogueTrees.EVENT_CLAIM_FIRST_BLOODCRAFT_REWARD -> {
			event.setRewardDelivered(handleVicarFirstBloodcraftReward(player, event.getEntityId()));
		}
		case HarbingerVicarDialogueTrees.EVENT_LIBER_BLUEPRINT ->
			handleVicarBloodcraftBlueprint(player, event.getEntityId(), "liber_sanguinum");
		case HarbingerVicarDialogueTrees.EVENT_IRON_BLUEPRINT ->
			handleVicarBloodcraftBlueprint(player, event.getEntityId(), "hematic_iron_block");
		case HarbingerVicarDialogueTrees.EVENT_MASONS_RESPITE_DIRECTIVE -> {
			handleVicarMasonsRespiteDirective(player, event.getEntityId());
		}
		case "give_sanguine_monolith_blueprint" -> {
			handleGiveSanguineMonolithBlueprint(player, event.getEntityId());
		}
		case "give_stained_church_map" -> {
			handleGiveStainedChurchMap(player, event.getEntityId());
		}
		case CourtDiscoveryDialogue.VICAR_LETTER -> event.setRewardDelivered(
				player.level().getEntity(event.getEntityId()) instanceof com.vincenthuto.hemomancy.common.entity.npc.harbinger.HarbingerVicarEntity
						&& com.vincenthuto.hemomancy.common.capability.player.harbinger.court.CourtDiscoveryProgress.recordLetter(player));
		case "vicar_begin_initiation" -> event.setRewardDelivered(com.vincenthuto.hemomancy.common.mission.vicar.EarlyInitiation.begin(player, player.level().getEntity(event.getEntityId())));
		case "vicar_release_charm" -> event.setRewardDelivered(com.vincenthuto.hemomancy.common.mission.vicar.EarlyInitiation.release(player, player.level().getEntity(event.getEntityId())));
		default -> {
			return false;
		}
		}
		return true;
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

		OwedRewards.owe(player, List.of(new ItemStack(BlockInit.befouling_ash_trail.get(), 4)));
		HarbingerAdvancementGranter.grantIfNotDone(player,
				HarbingerAdvancementGranter.ADV_HERMIT_ROAD_REPORTED);
		player.displayClientMessage(
				Component.translatable("hemomancy.dialogue.event.vicar_hermit_road_reported")
						.withStyle(ChatFormatting.DARK_RED),
				false);
		OwedRewards.deliver(player);
	}

	private static boolean handleVicarFirstBloodcraftReward(ServerPlayer player, int entityId) {
		if (!(player.level().getEntity(entityId) instanceof com.vincenthuto.hemomancy.common.entity.npc.harbinger.HarbingerVicarEntity vicar)
				|| !com.vincenthuto.hemomancy.common.mission.vicar.EarlyInitiation.near(player, vicar)) {
			refuse(player, DialogueRewards.TOO_FAR);
			return false;
		}
		if (!FirstBloodcraftAssignment.canClaim(player)) {
			String messageKey = FirstBloodcraftAssignment.isClaimed(player)
					? "hemomancy.dialogue.event.vicar_first_bloodcraft_reward_known"
					: "hemomancy.dialogue.event.vicar_first_bloodcraft_reward_unready";
			player.displayClientMessage(
					Component.translatable(messageKey).withStyle(ChatFormatting.GRAY),
					false);
			return false;
		}

		boolean issueKit = !FirstBloodcraftAssignment.isClaimed(player);
		if (issueKit) {
			List<ItemStack> owedBefore = HemoCapabilityAccess.owedRewards(player).owed();
			OwedRewards.owe(player, FirstBloodcraftAssignment.rewardStacks());
			if (!FirstBloodcraftAssignment.markClaimed(player)) {
				HemoCapabilityAccess.owedRewards(player).settle(owedBefore);
				player.displayClientMessage(
						Component.translatable("hemomancy.dialogue.event.vicar_first_bloodcraft_reward_claim_failed")
								.withStyle(ChatFormatting.RED), false);
				return false;
			}
		}
		if (!FirstBloodcraftAssignment.promote(player)) {
			refuse(player, DialogueRewards.NOT_NOW);
			return false;
		}
		player.displayClientMessage(
				Component.translatable("hemomancy.dialogue.event.vicar_first_bloodcraft_reward_granted")
						.withStyle(ChatFormatting.DARK_RED),
				false);
		OwedRewards.deliver(player);
		return true;
	}

	private static void handleVicarBloodcraftBlueprint(ServerPlayer player, int entityId, String recipe) {
		if (!(player.level().getEntity(entityId) instanceof com.vincenthuto.hemomancy.common.entity.npc.harbinger.HarbingerVicarEntity vicar)
				|| !com.vincenthuto.hemomancy.common.mission.vicar.EarlyInitiation.near(player, vicar)) {
			refuse(player, DialogueRewards.TOO_FAR);
			return;
		}
		if (!ProgressionGates.isDegree(player, 1)) {
			refuse(player, "hemomancy.dialogue.event.vicar_blueprint_unready");
			return;
		}
		var target = new MnemonicBlueprintTarget(MnemonicBlueprintTarget.Type.BLOOD_STRUCTURE,
				Hemomancy.rloc("blood_structure/" + recipe));
		if (player.getInventory().items.stream().anyMatch(stack -> target.equals(MnemonicBlueprintItem.getTarget(stack)))) {
			refuse(player, "hemomancy.dialogue.event.vicar_blueprint_carried");
			return;
		}
		DialogueRewards.giveOrDropAtEntity(player, entityId, MnemonicBlueprintItem.create(ItemInit.mnemonic_blueprint.get(), target));
	}

	private static void handleVicarMasonsRespiteDirective(ServerPlayer player, int entityId) {
		if (!ProgressionGates.hasDegree(player, 4)) {
			player.displayClientMessage(
					Component.translatable("hemomancy.dialogue.event.vicar_masons_respite_unready")
							.withStyle(ChatFormatting.GRAY),
					false);
			return;
		}
		boolean directed = HarbingerAdvancementGranter.isVicarMasonsRespiteDirective(player);
		if (directed && !needsMasonsRespiteMap(player)) {
			player.displayClientMessage(
					Component.translatable("hemomancy.dialogue.event.vicar_masons_respite_known")
							.withStyle(ChatFormatting.GRAY),
					false);
			return;
		}
		OwedRewards.owe(player, List.of(new ItemStack(ItemInit.masons_respite_map.get())));
		HarbingerAdvancementGranter.grantIfNotDone(player,
				HarbingerAdvancementGranter.ADV_VICAR_MASONS_RESPITE_DIRECTIVE);
		player.displayClientMessage(
				Component.translatable(directed
								? "hemomancy.dialogue.event.vicar_masons_respite_reissued"
								: "hemomancy.dialogue.event.vicar_masons_respite_map")
						.withStyle(ChatFormatting.DARK_RED),
				false);
		OwedRewards.deliver(player);
	}

	/** PT-4: a directed player who carries no Mason's Respite map, and is owed none, may ask the Vicar for another. */
	private static boolean needsMasonsRespiteMap(ServerPlayer player) {
		return !OwedRewards.carriesOrOwed(player, ItemInit.masons_respite_map.get());
	}

	/**
	 * The Monolith scrap: a mnemonic blueprint for the Sanguine Monolith Working craft, owed and delivered (PT-3). The
	 * Vicar gives one whenever none is carried or owed, never a second while one is held (PT-27, NQ-10-7).
	 */
	private static void handleGiveSanguineMonolithBlueprint(ServerPlayer player, int entityId) {
		// The Vicar tells the Monolith rumour that offers the scrap from the fifth Degree on, and not to a purifying player.
		if (!ProgressionGates.hasDegree(player, 5) || HemoCapabilityAccess.hasBegunPurification(player)) {
			refuse(player, "hemomancy.dialogue.event.vicar_scrap_refused");
			return;
		}
		MnemonicBlueprintTarget target = new MnemonicBlueprintTarget(MnemonicBlueprintTarget.Type.CARDINAL_RITE,
				ResourceLocation.tryBuild(Hemomancy.MOD_ID, "cardinal_rite/working/sanguine_monolith"));
		if (OwedRewards.carriesOrOwed(player, stack -> target.equals(MnemonicBlueprintItem.getTarget(stack)))) {
			refuse(player, "hemomancy.dialogue.event.vicar_scrap_carried");
			return;
		}
		OwedRewards.give(player, List.of(MnemonicBlueprintItem.create(ItemInit.mnemonic_blueprint.get(), target)));

		player.displayClientMessage(
				Component.translatable("hemomancy.dialogue.event.vicar_gives_scrap")
						.withStyle(ChatFormatting.DARK_RED),
				false);
	}

	private static void handleGiveStainedChurchMap(ServerPlayer player, int entityId) {
		if (!mayReceiveStainedChurchMap(player, player.level().getEntity(entityId))) {
			refuse(player, "hemomancy.dialogue.event.stained_church_map_refused");
			return;
		}
		OwedRewards.give(player, List.of(new ItemStack(ItemInit.stained_church_map.get())));

		player.displayClientMessage(
				Component.translatable("hemomancy.dialogue.event.gives_stained_church_map")
						.withStyle(ChatFormatting.DARK_RED),
				false);
	}

	/** PT-28: a reward the speaker will not give says so. */
	private static void refuse(ServerPlayer player, String key) {
		player.displayClientMessage(Component.translatable(key).withStyle(ChatFormatting.GRAY), false);
	}

	/**
	 * The trees that offer the map: the Vicar's from the seventh Degree, and the Zealot's once a purifying player has
	 * reached clarity or enlightenment.
	 */
	private static boolean mayReceiveStainedChurchMap(ServerPlayer player, net.minecraft.world.entity.Entity speaker) {
		if (speaker instanceof com.vincenthuto.hemomancy.common.entity.npc.harbinger.HarbingerVicarEntity) {
			return ProgressionGates.hasDegree(player, 7);
		}
		if (!(speaker instanceof com.vincenthuto.hemomancy.common.entity.npc.unstained.UnstainedZealotEntity)) return false;
		var unstained = com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess.getUnstainedProgress(player);
		return unstained.hasBegunPurification() && (unstained.hasClarityUnlocked() || unstained.isEnlightened());
	}
}
