package com.vincenthuto.hemomancy.common.item.harbinger;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.Bloodline;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.EnumArchonPath;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bestiary.SpecimenBestiaryDefinitions;
import com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.mission.artificer.ArtificerAssignments;
import com.vincenthuto.hemomancy.common.mission.shared.HarbingerChapterMilestone;
import com.vincenthuto.hemomancy.common.mission.shared.HarbingerChapterProgression;
import com.vincenthuto.hemomancy.common.mission.alchemist.FirstSeparationAssignment;
import com.vincenthuto.hemomancy.common.mission.alchemist.BodyAnswersAssignment;
import com.vincenthuto.hemomancy.common.mission.cicatrix_anchorite.VeinMasonAssignments;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.mission.OpenHarbingerAssignmentLedgerPacket;
import com.vincenthuto.hemomancy.common.rite.harbinger.QliphothBloomSavedData;
import com.vincenthuto.hutoslib.common.item.ItemGuideBook;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.stats.Stats;

import java.util.List;

public class HarbingerAssignmentLedgerItem extends ItemGuideBook {
	public HarbingerAssignmentLedgerItem(Properties properties, ResourceLocation texture) {
		super(properties, texture);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
			var completedChapters = HarbingerChapterProgression.completedChapters(serverPlayer);
			var degreeData = HemoCapabilityAccess.getInitiatoryDegree(serverPlayer);
			int pomesConsumed = degreeData.map(degree -> degree.getTotalPomesConsumed()).orElse(0);
			boolean qliphothCommunionComplete = degreeData
					.map(degree -> degree.isQliphothCommunionDone()).orElse(false);
			EnumArchonPath archonPath = degreeData.map(degree -> degree.getArchonPath())
					.orElse(EnumArchonPath.NONE);
			QliphothBloomSavedData blooms = QliphothBloomSavedData.get(serverPlayer.getServer().overworld());
			boolean severedPortalOpen = blooms.getBlooms().stream()
					.filter(bloom -> bloom.ownerUUID().equals(serverPlayer.getUUID()))
					.anyMatch(bloom -> blooms.getState(bloom.center()).isPortalOpen());
			Bloodline playerBloodline = HemoCapabilityAccess.getBloodVolume(serverPlayer)
					.map(volume -> volume.getBloodLine()).orElse(Bloodline.NOBLOODLINE);
			boolean foundedBloodline = playerBloodline.isValid()
					&& serverPlayer.getUUID().equals(playerBloodline.getLeaderUUID());
			boolean artificerArmaturePlaced = HarbingerAdvancementGranter.isArtificerArmaturePlaced(serverPlayer);
			boolean artificerFirstHematicUpgrade =
					HarbingerAdvancementGranter.isArtificerFirstHematicUpgrade(serverPlayer);
			boolean artificerHematicIronFitting =
					HarbingerAdvancementGranter.isArtificerHematicIronFitting(serverPlayer);
			boolean artificerFirstForkUpgrade =
					HarbingerAdvancementGranter.isArtificerFirstForkUpgrade(serverPlayer);
			boolean artificerForkFitting = HarbingerAdvancementGranter.isArtificerForkFitting(serverPlayer);
			boolean artificerFrameConsecrated =
					HarbingerAdvancementGranter.isArtificerFrameConsecrated(serverPlayer);
			boolean artificerFirstBloodLustUpgrade =
					HarbingerAdvancementGranter.isArtificerFirstBloodLustUpgrade(serverPlayer);
			boolean artificerBloodLustFitting =
					HarbingerAdvancementGranter.isArtificerBloodLustFitting(serverPlayer);
			boolean artificerMonolithicFrame =
					HarbingerAdvancementGranter.isArtificerMonolithicFrame(serverPlayer);
			boolean artificerFirstD7Upgrade =
					HarbingerAdvancementGranter.isArtificerFirstD7Upgrade(serverPlayer);
			boolean artificerD7Fitting = HarbingerAdvancementGranter.isArtificerD7Fitting(serverPlayer);
			boolean artificerFirstLivingGraft =
					HarbingerAdvancementGranter.isArtificerFirstLivingGraft(serverPlayer);
			int artificerLivingWeaponFormCount =
					ArtificerAssignments.knownLivingWeaponFormCount(serverPlayer);
			boolean artificerLivingArsenalFitting =
					HarbingerAdvancementGranter.isArtificerLivingArsenalFitting(serverPlayer);
			var artificerProgress = com.vincenthuto.hemomancy.common.entity.npc.dialogue.ArtificerProgressSnapshot.from(serverPlayer);
			int artificerProgressSteps = com.vincenthuto.hemomancy.common.mission.artificer.ArtificerProgressionRules.packSteps(
					artificerProgress.wornVow(), artificerProgress.threeAnswers(), artificerProgress.crimsonVestment(),
					artificerProgress.assumedLimb(), artificerProgress.weightOfFrame());
			PacketHandler.sendToPlayer(serverPlayer, new OpenHarbingerAssignmentLedgerPacket(
					HemoCapabilityAccess.getPlayerDegreeNumber(serverPlayer),
					HarbingerAdvancementGranter.hasAdvancement(serverPlayer,
							Hemomancy.rloc("hemomancy/the_first_awakening")),
					HarbingerAdvancementGranter.hasAdvancement(serverPlayer,
							HarbingerAdvancementGranter.ADV_DEGREE_1_NEOPHYTE),
					HarbingerAdvancementGranter.isVesselFilled(serverPlayer),
					HarbingerAdvancementGranter.isLiberSanguinumCrafted(serverPlayer),
					HarbingerAdvancementGranter.isHematicIronBlockCrafted(serverPlayer),
					HarbingerAdvancementGranter.hasAdvancement(serverPlayer,
							HarbingerAdvancementGranter.ADV_HERMIT_ROAD_FIRST_REMNANT),
					HarbingerAdvancementGranter.hasAdvancement(serverPlayer,
							HarbingerAdvancementGranter.ADV_HERMIT_ROAD_REPORTED),
					hasVialCentrifuge(serverPlayer),
					hasSampledBloodVial(serverPlayer),
					HarbingerAdvancementGranter.isFirstSeparationStarted(serverPlayer),
					hasAnyEnzyme(serverPlayer),
					HarbingerAdvancementGranter.hasAdvancement(serverPlayer,
							BodyAnswersAssignment.ADV_BRIEFED),
					HarbingerAdvancementGranter.hasAdvancement(serverPlayer,
							BodyAnswersAssignment.ADV_COMPLETE),
					serverPlayer.getData(com.vincenthuto.hemomancy.common.capability.HemoAttachmentTypes.MUSCLE_MEMORY).knownCount(),
					HarbingerAdvancementGranter.getRedTaxonomySpecimenCount(serverPlayer),
					HarbingerAdvancementGranter.isRedTaxonomyComplete(serverPlayer),
					HarbingerAdvancementGranter.getEnzymeMasteryCount(serverPlayer),
					HarbingerAdvancementGranter.isEnzymeMasteryComplete(serverPlayer),
					HemoCapabilityAccess.getSpecimenBestiary(serverPlayer)
							.map(progress -> progress.recordedSpecimenCount()).orElse(0),
					SpecimenBestiaryDefinitions.totalResearchSpecimens(),
					HemoCapabilityAccess.getSpecimenBestiary(serverPlayer)
							.map(progress -> progress.recordedMorphlingLayerCount()).orElse(0),
					hasBlankHematicMemory(serverPlayer),
					HarbingerAdvancementGranter.isMnemonistWovenVesselComplete(serverPlayer),
					HarbingerAdvancementGranter.isMnemonistFirstWeaveComplete(serverPlayer),
					HarbingerAdvancementGranter.isVicarMasonsRespiteDirective(serverPlayer),
					HarbingerAdvancementGranter.isVeinMasonFirstLesson(serverPlayer),
					HarbingerAdvancementGranter.isVeinMasonFirstScarCarved(serverPlayer),
					HarbingerAdvancementGranter.isVeinMasonFirstScarLearned(serverPlayer),
					HarbingerAdvancementGranter.isVeinMasonFirstEffigyPattern(serverPlayer),
					HarbingerAdvancementGranter.isVeinMasonFirstEffigyLoadout(serverPlayer),
					anchoriteD5Progress(serverPlayer), anchoriteD6Progress(serverPlayer),
					artificerArmaturePlaced, artificerFirstHematicUpgrade, artificerHematicIronFitting,
					artificerFirstForkUpgrade, artificerForkFitting, artificerFrameConsecrated,
					artificerFirstBloodLustUpgrade, artificerBloodLustFitting, artificerMonolithicFrame,
					artificerFirstD7Upgrade, artificerD7Fitting, artificerFirstLivingGraft,
					artificerLivingWeaponFormCount, artificerLivingArsenalFitting,
					artificerProgressSteps,
					foundedBloodline,
					completedChapters.contains(HarbingerChapterMilestone.COVENANT_WRITTEN_IN_PLACE),
					HarbingerAdvancementGranter.hasAdvancement(serverPlayer,
							HarbingerAdvancementGranter.ADV_CHAMBER_RETURNED),
					HarbingerAdvancementGranter.hasAdvancement(serverPlayer,
							HarbingerAdvancementGranter.ADV_COVENANT_THRONE_BOUND),
					HarbingerAdvancementGranter.hasAdvancement(serverPlayer,
							HarbingerAdvancementGranter.ADV_COVENANT_VIGIL_COMPLETED),
					completedChapters.contains(HarbingerChapterMilestone.LIVING_COVENANT),
					pomesConsumed, qliphothCommunionComplete,
					archonPath == EnumArchonPath.SILENT_PENDING, severedPortalOpen,
					archonPath == EnumArchonPath.SILENT_ARCHON));
		}
		return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
	}

	private static int anchoriteD5Progress(ServerPlayer player) {
		int progress = VeinMasonAssignments.has(player, VeinMasonAssignments.D5_VARICOSE) ? 1 : 0;
		if (VeinMasonAssignments.has(player, VeinMasonAssignments.D5_DIAGNOSED)) progress++;
		if (VeinMasonAssignments.has(player, VeinMasonAssignments.D5_TREATED)) progress++;
		if (HemoCapabilityAccess.getInitiatoryDegree(player).map(degree -> degree.hasHematicFortification()).orElse(false)) progress++;
		return progress;
	}

	private static int anchoriteD6Progress(ServerPlayer player) {
		int progress = VeinMasonAssignments.has(player, VeinMasonAssignments.D6_COUNSEL) ? 1 : 0;
		if (VeinMasonAssignments.has(player, VeinMasonAssignments.D6_FIRST_ROUTE)) progress++;
		if (VeinMasonAssignments.has(player, VeinMasonAssignments.D6_LOADOUT)) progress++;
		if (VeinMasonAssignments.has(player, VeinMasonAssignments.D6_SECOND_ROUTE)) progress++;
		return progress;
	}

	private static boolean hasBlankHematicMemory(ServerPlayer player) {
		return player.getInventory().items.stream()
				.anyMatch(stack -> stack.is(ItemInit.hematic_memory.get()));
	}

	private static boolean hasVialCentrifuge(ServerPlayer player) {
		boolean acquired = FirstSeparationAssignment.hasCentrifugeAcquired(player)
				|| player.getStats().getValue(Stats.ITEM_CRAFTED.get(BlockInit.vial_centrifuge.get().asItem())) > 0
				|| player.getInventory().items.stream()
						.anyMatch(stack -> stack.is(BlockInit.vial_centrifuge.get().asItem()));
		if (acquired) FirstSeparationAssignment.markCentrifugeAcquired(player);
		return acquired;
	}

	private static boolean hasSampledBloodVial(ServerPlayer player) {
		return player.getInventory().items.stream()
				.anyMatch(stack -> stack.getItem() instanceof BloodVialItem
						&& BloodVialItem.getEntityType(stack) != null);
	}

	private static boolean hasAnyEnzyme(ServerPlayer player) {
		return HarbingerAdvancementGranter.isFirstSeparationComplete(player);
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
		super.appendHoverText(stack, context, tooltip, flag);
		tooltip.add(Component.translatable("item.hemomancy.harbinger_assignment_ledger.tooltip")
				.withStyle(ChatFormatting.GRAY));
		tooltip.add(Component.translatable("item.hemomancy.harbinger_assignment_ledger.tooltip.use")
				.withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
	}
}
