package com.vincenthuto.hemomancy.common.entity.npc.dialogue.route;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.HemomancyDiscoverySource;
import com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.discovery.LiberEntryDefinitions;
import com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.discovery.LiberKnowledgeHelper;
import com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.item.shared.MnemonicBlueprintItem;
import com.vincenthuto.hemomancy.common.item.shared.MnemonicBlueprintTarget;
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

/**
 * The Vicar's dialogue events: early initiation, the Voyager report, blood-shotting, the Hermit road report, the
 * first bloodcraft reward, blueprints, the Mason's Respite directive and the church map (the Zealot hands it out too).
 */
final class VicarDialogueEvents {
	private VicarDialogueEvents() {
	}

	private static final float BLOOD_SHOTTING_OVERLAY_ALPHA = 0.30F;

	private static final int BLOOD_SHOTTING_OVERLAY_TICKS = 1200;

	/** Handles the event when it is one of this NPC's; false lets the next route try. */
	public static boolean handle(DialogueEvent event) {
		ServerPlayer player = event.getPlayer();
		switch (event.getEventId()) {
		case VoyagerIntroductionDialogue.REPORT -> event.setRewardDelivered(
				com.vincenthuto.hemomancy.common.mission.vicar.VoyagerIntroduction.report(
						player, player.level().getEntity(event.getEntityId())));
		case VoyagerIntroductionDialogue.BEARING -> event.setRewardDelivered(
				com.vincenthuto.hemomancy.common.mission.vicar.VoyagerIntroduction.tellBearing(
						player, player.level().getEntity(event.getEntityId())));
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

		HarbingerAdvancementGranter.grantIfNotDone(player,
				HarbingerAdvancementGranter.ADV_HERMIT_ROAD_REPORTED);
		DialogueRewards.giveOrDropAtEntity(player, entityId, new ItemStack(BlockInit.befouling_ash_trail.get(), 4));
		player.displayClientMessage(
				Component.translatable("hemomancy.dialogue.event.vicar_hermit_road_reported")
						.withStyle(ChatFormatting.DARK_RED),
				false);
	}

	private static boolean handleVicarFirstBloodcraftReward(ServerPlayer player, int entityId) {
		if (!(player.level().getEntity(entityId) instanceof com.vincenthuto.hemomancy.common.entity.npc.harbinger.HarbingerVicarEntity vicar)
				|| !com.vincenthuto.hemomancy.common.mission.vicar.EarlyInitiation.near(player, vicar)) return false;
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
		if (issueKit && !FirstBloodcraftAssignment.markClaimed(player)) {
			player.displayClientMessage(
					Component.translatable("hemomancy.dialogue.event.vicar_first_bloodcraft_reward_claim_failed")
							.withStyle(ChatFormatting.RED), false);
			return false;
		}
		if (!FirstBloodcraftAssignment.promote(player)) return false;
		if (issueKit) {
			for (ItemStack stack : FirstBloodcraftAssignment.rewardStacks()) {
				DialogueRewards.giveOrDropAtEntity(player, entityId, stack);
			}
		}
		player.displayClientMessage(
				Component.translatable("hemomancy.dialogue.event.vicar_first_bloodcraft_reward_granted")
						.withStyle(ChatFormatting.DARK_RED),
				false);
		return true;
	}

	private static void handleVicarBloodcraftBlueprint(ServerPlayer player, int entityId, String recipe) {
		if (!ProgressionGates.isDegree(player, 1)
				|| !(player.level().getEntity(entityId) instanceof com.vincenthuto.hemomancy.common.entity.npc.harbinger.HarbingerVicarEntity vicar)
				|| !com.vincenthuto.hemomancy.common.mission.vicar.EarlyInitiation.near(player, vicar)) return;
		var target = new MnemonicBlueprintTarget(MnemonicBlueprintTarget.Type.BLOOD_STRUCTURE,
				Hemomancy.rloc("blood_structure/" + recipe));
		if (player.getInventory().items.stream().anyMatch(stack -> target.equals(MnemonicBlueprintItem.getTarget(stack)))) return;
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
		if (HarbingerAdvancementGranter.isVicarMasonsRespiteDirective(player)) {
			player.displayClientMessage(
					Component.translatable("hemomancy.dialogue.event.vicar_masons_respite_known")
							.withStyle(ChatFormatting.GRAY),
					false);
			return;
		}
		DialogueRewards.giveOrDropAtEntity(player, entityId, new ItemStack(ItemInit.masons_respite_map.get()));
		HarbingerAdvancementGranter.grantIfNotDone(player,
				HarbingerAdvancementGranter.ADV_VICAR_MASONS_RESPITE_DIRECTIVE);
		player.displayClientMessage(
				Component.translatable("hemomancy.dialogue.event.vicar_masons_respite_map")
						.withStyle(ChatFormatting.DARK_RED),
				false);
	}

	/**
	 * Gives the player a mnemonic blueprint pre-configured for the Sanguine Monolith
	 * Working craft. The item is dropped
	 * at the NPC entity's position so the player can pick it up naturally.
	 * If the NPC entity cannot be found, the item is given directly to the player's
	 * inventory instead.
	 */
	/**
	 * Gives the player a mnemonic blueprint pre-configured for the Sanguine Monolith
	 * Working craft. The item is dropped
	 * at the NPC entity's position so the player can pick it up naturally.
	 * If the NPC entity cannot be found, the item is given directly to the player's
	 * inventory instead.
	 */
	private static void handleGiveSanguineMonolithBlueprint(ServerPlayer player, int entityId) {
		ItemStack hint = MnemonicBlueprintItem.create(ItemInit.mnemonic_blueprint.get(),
				new MnemonicBlueprintTarget(MnemonicBlueprintTarget.Type.CARDINAL_RITE,
						ResourceLocation.tryBuild(Hemomancy.MOD_ID, "cardinal_rite/working/sanguine_monolith")));

		DialogueRewards.giveOrDropAtEntity(player, entityId, hint);

		player.displayClientMessage(
				Component.translatable("hemomancy.dialogue.event.vicar_gives_scrap")
						.withStyle(ChatFormatting.DARK_RED),
				false);
	}

	private static void handleGiveStainedChurchMap(ServerPlayer player, int entityId) {
		ItemStack map = new ItemStack(ItemInit.stained_church_map.get());
		DialogueRewards.giveOrDropAtEntity(player, entityId, map);

		player.displayClientMessage(
				Component.translatable("hemomancy.dialogue.event.gives_stained_church_map")
						.withStyle(ChatFormatting.DARK_RED),
				false);
	}
}
