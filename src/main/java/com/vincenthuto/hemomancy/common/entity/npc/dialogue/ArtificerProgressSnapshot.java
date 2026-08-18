package com.vincenthuto.hemomancy.common.entity.npc.dialogue;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.livingstaff.ILivingStaffProgress;
import com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.mission.ArtificerProgressionRules;
import com.vincenthuto.hemomancy.common.mission.HarbingerArtificerAssignmentHelper;
import net.minecraft.server.level.ServerPlayer;

import static com.vincenthuto.hemomancy.common.mission.ArtificerProgressionRules.D7Lineage;
import static com.vincenthuto.hemomancy.common.mission.ArtificerProgressionRules.ForkFamily;
import static com.vincenthuto.hemomancy.common.mission.ArtificerProgressionRules.Step;

public record ArtificerProgressSnapshot(int degree, boolean activeBlood, boolean purifying, boolean clarity,
		boolean livingStaffBond, ForkFamily forkFamily, D7Lineage d7Lineage,
		Step wornVow, Step threeAnswers, Step crimsonVestment, Step assumedLimb, Step weightOfFrame,
		boolean missingWornVowFitting, boolean missingForkFitting, boolean missingCrimsonFitting,
		boolean missingAssumedLimbFitting, boolean missingD7Fitting) {
	public boolean needsForkRecovery() { return threeAnswers == Step.RECOVER_BRANCH; }
	public boolean needsD7Recovery() { return weightOfFrame == Step.RECOVER_BRANCH; }

	public static ArtificerProgressSnapshot from(ServerPlayer player) {
		int degree = HemoCapabilityAccess.getPlayerDegreeNumber(player);
		boolean activeBlood = HemoCapabilityAccess.getBloodVolume(player).map(volume -> volume.isActive()).orElse(false);
		boolean purifying = HemoCapabilityAccess.getUnstainedProgress(player)
				.map(progress -> progress.hasBegunPurification()).orElse(false);
		boolean clarity = HemoCapabilityAccess.getUnstainedProgress(player)
				.map(progress -> progress.hasClarityUnlocked()).orElse(false);
		boolean staffBond = HemoCapabilityAccess.getLivingStaffProgress(player)
				.map(ILivingStaffProgress::hasLivingStaffBond).orElse(false);
		ForkFamily fork = HarbingerArtificerAssignmentHelper.firstForkFamily(player);
		D7Lineage lineage = HarbingerArtificerAssignmentHelper.firstD7Lineage(player);

		boolean wornFitting = HarbingerAdvancementGranter.isArtificerHematicIronFitting(player);
		boolean forkFitting = HarbingerAdvancementGranter.isArtificerForkFitting(player);
		boolean crimsonFitting = HarbingerAdvancementGranter.isArtificerBloodLustFitting(player);
		boolean assumedFitting = HarbingerAdvancementGranter.isArtificerLivingArsenalFitting(player);
		boolean d7Fitting = HarbingerAdvancementGranter.isArtificerD7Fitting(player);

		boolean wornBriefed = has(player, HarbingerArtificerAssignmentHelper.WORN_VOW_BRIEFED);
		boolean armaturePlaced = HarbingerAdvancementGranter.isArtificerArmaturePlaced(player);
		boolean hematicUpgrade = HarbingerAdvancementGranter.isArtificerFirstHematicUpgrade(player);
		boolean wornRewarded = HarbingerArtificerAssignmentHelper.isArtificerLessonRewardClaimed(player,
				HarbingerArtificerAssignmentHelper.WORN_VOW_REWARD_CLAIM_KEY);
		boolean wornFullSet = com.vincenthuto.hemomancy.common.armor.ArmorSetHelper.hasFullHematicIronSet(player);
		Step wornStep = degree < 2 ? Step.LOCKED : ArtificerProgressionRules.nextWornVow(
				wornBriefed, armaturePlaced, hematicUpgrade, wornRewarded, wornFullSet, wornFitting);
		boolean forkBriefed = has(player, HarbingerArtificerAssignmentHelper.THREE_ANSWERS_BRIEFED);
		boolean forkUpgraded = HarbingerAdvancementGranter.isArtificerFirstForkUpgrade(player);
		boolean forkInspected = has(player, HarbingerArtificerAssignmentHelper.THREE_ANSWERS_INSPECTED);
		boolean forkCounseled = has(player, HarbingerArtificerAssignmentHelper.THREE_ANSWERS_COUNSELED)
				|| HarbingerArtificerAssignmentHelper.isArtificerLessonRewardClaimed(player,
				HarbingerArtificerAssignmentHelper.THREE_ANSWERS_REWARD_CLAIM_KEY);
		boolean forkFullSet = HarbingerArtificerAssignmentHelper.hasFullForkFamily(player, fork);
		boolean forkDemonstrated = has(player, HarbingerArtificerAssignmentHelper.THREE_ANSWERS_DEMONSTRATED);
		Step forkStep = degree < 3 ? Step.LOCKED : ArtificerProgressionRules.nextThreeAnswers(
				forkBriefed, forkUpgraded, fork != ForkFamily.NONE, forkInspected, forkCounseled,
				forkFullSet, forkDemonstrated, forkFitting);
		boolean crimsonBriefed = has(player, HarbingerArtificerAssignmentHelper.CRIMSON_VESTMENT_BRIEFED);
		boolean consecrated = HarbingerAdvancementGranter.isArtificerFrameConsecrated(player);
		boolean crimsonInspected = has(player, HarbingerArtificerAssignmentHelper.CRIMSON_VESTMENT_INSPECTED);
		boolean crimsonCounseled = has(player, HarbingerArtificerAssignmentHelper.CRIMSON_VESTMENT_COUNSELED)
				|| HarbingerArtificerAssignmentHelper.isArtificerLessonRewardClaimed(player,
				HarbingerArtificerAssignmentHelper.CRIMSON_VESTMENT_REWARD_CLAIM_KEY);
		boolean bloodLustUpgrade = HarbingerAdvancementGranter.isArtificerFirstBloodLustUpgrade(player);
		boolean bloodLustFullSet = com.vincenthuto.hemomancy.common.armor.ArmorSetHelper.hasFullBloodLustSet(player);
		boolean crimsonDemonstrated = has(player, HarbingerArtificerAssignmentHelper.CRIMSON_VESTMENT_DEMONSTRATED);
		Step crimsonStep = degree < 5 ? Step.LOCKED : ArtificerProgressionRules.nextCrimsonVestment(
				crimsonBriefed, consecrated, crimsonInspected, crimsonCounseled, bloodLustUpgrade,
				bloodLustFullSet, crimsonDemonstrated, crimsonFitting);
		boolean assumedBriefed = has(player, HarbingerArtificerAssignmentHelper.ASSUMED_LIMB_BRIEFED);
		boolean grafted = HarbingerAdvancementGranter.isArtificerFirstLivingGraft(player);
		boolean assumedRewarded = HarbingerArtificerAssignmentHelper.isArtificerLessonRewardClaimed(player,
				HarbingerArtificerAssignmentHelper.ASSUMED_LIMB_REWARD_CLAIM_KEY);
		boolean assumedDemonstrated = has(player, HarbingerArtificerAssignmentHelper.ASSUMED_LIMB_DEMONSTRATED);
		int knownForms = HarbingerArtificerAssignmentHelper.knownLivingWeaponFormCount(player);
		Step assumedStep = degree < 5 || !staffBond ? Step.LOCKED : ArtificerProgressionRules.nextAssumedLimb(
				assumedBriefed, grafted, assumedRewarded, assumedDemonstrated, knownForms, assumedFitting);
		boolean d7Briefed = has(player, HarbingerArtificerAssignmentHelper.WEIGHT_OF_FRAME_BRIEFED);
		boolean cornerstone = HarbingerAdvancementGranter.isArtificerMonolithicFrame(player);
		boolean d7Upgraded = HarbingerAdvancementGranter.isArtificerFirstD7Upgrade(player);
		boolean d7Inspected = has(player, HarbingerArtificerAssignmentHelper.WEIGHT_OF_FRAME_INSPECTED);
		boolean d7Rewarded = player.getPersistentData().getBoolean(HarbingerArtificerAssignmentHelper.D7_REWARD_CLAIM_KEY);
		boolean d7FullSet = HarbingerArtificerAssignmentHelper.hasFullD7Lineage(player, lineage);
		boolean d7Demonstrated = has(player, HarbingerArtificerAssignmentHelper.WEIGHT_OF_FRAME_DEMONSTRATED);
		Step d7Step = degree < 7 ? Step.LOCKED : ArtificerProgressionRules.nextWeightOfFrame(
				d7Briefed, cornerstone, d7Upgraded, lineage != D7Lineage.NONE, d7Inspected, d7Rewarded,
				d7FullSet, d7Demonstrated, d7Fitting);

		return new ArtificerProgressSnapshot(degree, activeBlood, purifying, clarity, staffBond, fork, lineage,
				wornStep, forkStep, crimsonStep, assumedStep, d7Step,
				wornFitting && !HarbingerArtificerAssignmentHelper.hasFitting(player, ItemInit.worn_vow_fitting.get()),
				forkFitting && !hasForkFitting(player, fork),
				crimsonFitting && !HarbingerArtificerAssignmentHelper.hasFitting(player, ItemInit.crimson_vestment_fitting.get()),
				assumedFitting && !HarbingerArtificerAssignmentHelper.hasFitting(player, ItemInit.assumed_limb_fitting.get()),
				d7Fitting && !HarbingerArtificerAssignmentHelper.hasFitting(player, ItemInit.monolithic_frame_fitting.get()));
	}

	private static boolean hasForkFitting(ServerPlayer player, ForkFamily family) {
		return switch (family) {
			case BARBED -> HarbingerArtificerAssignmentHelper.hasFitting(player, ItemInit.barbed_fitting.get());
			case CHITINITE -> HarbingerArtificerAssignmentHelper.hasFitting(player, ItemInit.chitinite_fitting.get());
			case PRISMATIC -> HarbingerArtificerAssignmentHelper.hasFitting(player, ItemInit.prismatic_fitting.get());
			case NONE -> false;
		};
	}

	private static boolean has(ServerPlayer player, net.minecraft.resources.ResourceLocation advancement) {
		return HarbingerArtificerAssignmentHelper.has(player, advancement);
	}
}
