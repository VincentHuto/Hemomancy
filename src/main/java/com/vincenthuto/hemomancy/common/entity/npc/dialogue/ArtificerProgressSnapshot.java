package com.vincenthuto.hemomancy.common.entity.npc.dialogue;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.livingstaff.ILivingStaffProgress;
import com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.mission.artificer.ArtificerProgressionRules;
import com.vincenthuto.hemomancy.common.mission.artificer.ArtificerAssignments;
import net.minecraft.server.level.ServerPlayer;

import static com.vincenthuto.hemomancy.common.mission.artificer.ArtificerProgressionRules.D7Lineage;
import static com.vincenthuto.hemomancy.common.mission.artificer.ArtificerProgressionRules.ForkFamily;
import static com.vincenthuto.hemomancy.common.mission.artificer.ArtificerProgressionRules.Step;

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
		ForkFamily fork = ArtificerAssignments.firstForkFamily(player);
		D7Lineage lineage = ArtificerAssignments.firstD7Lineage(player);

		boolean wornFitting = HarbingerAdvancementGranter.isArtificerHematicIronFitting(player);
		boolean forkFitting = HarbingerAdvancementGranter.isArtificerForkFitting(player);
		boolean crimsonFitting = HarbingerAdvancementGranter.isArtificerBloodLustFitting(player);
		boolean assumedFitting = HarbingerAdvancementGranter.isArtificerLivingArsenalFitting(player);
		boolean d7Fitting = HarbingerAdvancementGranter.isArtificerD7Fitting(player);

		boolean wornBriefed = has(player, ArtificerAssignments.WORN_VOW_BRIEFED);
		boolean armaturePlaced = HarbingerAdvancementGranter.isArtificerArmaturePlaced(player);
		boolean hematicUpgrade = HarbingerAdvancementGranter.isArtificerFirstHematicUpgrade(player);
		boolean wornRewarded = ArtificerAssignments.isArtificerLessonRewardClaimed(player,
				ArtificerAssignments.WORN_VOW_REWARD_CLAIM_KEY);
		boolean wornFullSet = com.vincenthuto.hemomancy.common.armor.ArmorSetHelper.hasFullHematicIronSet(player);
		Step wornStep = degree < 2 ? Step.LOCKED : ArtificerProgressionRules.nextWornVow(
				wornBriefed, armaturePlaced, hematicUpgrade, wornRewarded, wornFullSet, wornFitting);
		boolean forkBriefed = has(player, ArtificerAssignments.THREE_ANSWERS_BRIEFED);
		boolean forkUpgraded = HarbingerAdvancementGranter.isArtificerFirstForkUpgrade(player);
		boolean forkInspected = has(player, ArtificerAssignments.THREE_ANSWERS_INSPECTED);
		boolean forkCounseled = has(player, ArtificerAssignments.THREE_ANSWERS_COUNSELED)
				|| ArtificerAssignments.isArtificerLessonRewardClaimed(player,
				ArtificerAssignments.THREE_ANSWERS_REWARD_CLAIM_KEY);
		boolean forkFullSet = ArtificerAssignments.hasFullForkFamily(player, fork);
		boolean forkDemonstrated = has(player, ArtificerAssignments.THREE_ANSWERS_DEMONSTRATED);
		Step forkStep = degree < 3 ? Step.LOCKED : ArtificerProgressionRules.nextThreeAnswers(
				forkBriefed, forkUpgraded, fork != ForkFamily.NONE, forkInspected, forkCounseled,
				forkFullSet, forkDemonstrated, forkFitting);
		boolean crimsonBriefed = has(player, ArtificerAssignments.CRIMSON_VESTMENT_BRIEFED);
		boolean consecrated = HarbingerAdvancementGranter.isArtificerFrameConsecrated(player);
		boolean crimsonInspected = has(player, ArtificerAssignments.CRIMSON_VESTMENT_INSPECTED);
		boolean crimsonCounseled = has(player, ArtificerAssignments.CRIMSON_VESTMENT_COUNSELED)
				|| ArtificerAssignments.isArtificerLessonRewardClaimed(player,
				ArtificerAssignments.CRIMSON_VESTMENT_REWARD_CLAIM_KEY);
		boolean bloodLustUpgrade = HarbingerAdvancementGranter.isArtificerFirstBloodLustUpgrade(player);
		boolean bloodLustFullSet = com.vincenthuto.hemomancy.common.armor.ArmorSetHelper.hasFullBloodLustSet(player);
		boolean crimsonDemonstrated = has(player, ArtificerAssignments.CRIMSON_VESTMENT_DEMONSTRATED);
		Step crimsonStep = degree < 5 ? Step.LOCKED : ArtificerProgressionRules.nextCrimsonVestment(
				crimsonBriefed, consecrated, crimsonInspected, crimsonCounseled, bloodLustUpgrade,
				bloodLustFullSet, crimsonDemonstrated, crimsonFitting);
		boolean assumedBriefed = has(player, ArtificerAssignments.ASSUMED_LIMB_BRIEFED);
		boolean grafted = HarbingerAdvancementGranter.isArtificerFirstLivingGraft(player);
		boolean assumedRewarded = ArtificerAssignments.isArtificerLessonRewardClaimed(player,
				ArtificerAssignments.ASSUMED_LIMB_REWARD_CLAIM_KEY);
		boolean assumedDemonstrated = has(player, ArtificerAssignments.ASSUMED_LIMB_DEMONSTRATED);
		int knownForms = ArtificerAssignments.knownLivingWeaponFormCount(player);
		Step assumedStep = degree < 5 || !staffBond ? Step.LOCKED : ArtificerProgressionRules.nextAssumedLimb(
				assumedBriefed, grafted, assumedRewarded, assumedDemonstrated, knownForms, assumedFitting);
		boolean d7Briefed = has(player, ArtificerAssignments.WEIGHT_OF_FRAME_BRIEFED);
		boolean cornerstone = HarbingerAdvancementGranter.isArtificerMonolithicFrame(player);
		boolean d7Upgraded = HarbingerAdvancementGranter.isArtificerFirstD7Upgrade(player);
		boolean d7Inspected = has(player, ArtificerAssignments.WEIGHT_OF_FRAME_INSPECTED);
		boolean d7Rewarded = player.getPersistentData().getBoolean(ArtificerAssignments.D7_REWARD_CLAIM_KEY);
		boolean d7FullSet = ArtificerAssignments.hasFullD7Lineage(player, lineage);
		boolean d7Demonstrated = has(player, ArtificerAssignments.WEIGHT_OF_FRAME_DEMONSTRATED);
		Step d7Step = degree < 7 ? Step.LOCKED : ArtificerProgressionRules.nextWeightOfFrame(
				d7Briefed, cornerstone, d7Upgraded, lineage != D7Lineage.NONE, d7Inspected, d7Rewarded,
				d7FullSet, d7Demonstrated, d7Fitting);

		return new ArtificerProgressSnapshot(degree, activeBlood, purifying, clarity, staffBond, fork, lineage,
				wornStep, forkStep, crimsonStep, assumedStep, d7Step,
				wornFitting && !ArtificerAssignments.hasFitting(player, ItemInit.worn_vow_fitting.get()),
				forkFitting && !hasForkFitting(player, fork),
				crimsonFitting && !ArtificerAssignments.hasFitting(player, ItemInit.crimson_vestment_fitting.get()),
				assumedFitting && !ArtificerAssignments.hasFitting(player, ItemInit.assumed_limb_fitting.get()),
				d7Fitting && !ArtificerAssignments.hasFitting(player, ItemInit.monolithic_frame_fitting.get()));
	}

	private static boolean hasForkFitting(ServerPlayer player, ForkFamily family) {
		return switch (family) {
			case BARBED -> ArtificerAssignments.hasFitting(player, ItemInit.barbed_fitting.get());
			case CHITINITE -> ArtificerAssignments.hasFitting(player, ItemInit.chitinite_fitting.get());
			case PRISMATIC -> ArtificerAssignments.hasFitting(player, ItemInit.prismatic_fitting.get());
			case NONE -> false;
		};
	}

	private static boolean has(ServerPlayer player, net.minecraft.resources.ResourceLocation advancement) {
		return ArtificerAssignments.has(player, advancement);
	}
}
