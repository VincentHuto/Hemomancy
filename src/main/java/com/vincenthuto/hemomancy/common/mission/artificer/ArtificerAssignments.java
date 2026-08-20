package com.vincenthuto.hemomancy.common.mission.artificer;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.armor.ArmorSetHelper;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.item.component.LivingWeaponForm;
import com.vincenthuto.hemomancy.common.recipe.ArmatureUpgradeRules;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Set;

import static com.vincenthuto.hemomancy.common.mission.artificer.ArtificerProgressionRules.D7Lineage;
import static com.vincenthuto.hemomancy.common.mission.artificer.ArtificerProgressionRules.ForkFamily;

public final class ArtificerAssignments {
	public static final String WORN_VOW_REWARD_CLAIM_KEY =
			"hemomancy.artificer.worn_vow_reward_claimed";
	public static final String THREE_ANSWERS_REWARD_CLAIM_KEY =
			"hemomancy.artificer.three_answers_reward_claimed";
	public static final String CRIMSON_VESTMENT_REWARD_CLAIM_KEY =
			"hemomancy.artificer.crimson_vestment_reward_claimed";
	public static final String ASSUMED_LIMB_REWARD_CLAIM_KEY =
			"hemomancy.artificer.assumed_limb_reward_claimed";
	public static final String D7_REWARD_CLAIM_KEY = "hemomancy.artificer.d7_reward_claimed";
	public static final String FORK_RESEARCH_REWARD_CLAIM_KEY =
			"hemomancy.artificer.fork_research_reward_claimed";
	public static final String FIRST_FORK_FAMILY_KEY = "hemomancy.artificer.first_fork_family";
	public static final String FIRST_D7_LINEAGE_KEY = "hemomancy.artificer.first_d7_lineage";

	public static final ResourceLocation WORN_VOW_BRIEFED = id("artificer_worn_vow_briefed");
	public static final ResourceLocation THREE_ANSWERS_BRIEFED = id("artificer_three_answers_briefed");
	public static final ResourceLocation THREE_ANSWERS_INSPECTED = id("artificer_three_answers_inspected");
	public static final ResourceLocation THREE_ANSWERS_COUNSELED = id("artificer_three_answers_counseled");
	public static final ResourceLocation THREE_ANSWERS_DEMONSTRATED = id("artificer_three_answers_demonstrated");
	public static final ResourceLocation CRIMSON_VESTMENT_BRIEFED = id("artificer_crimson_vestment_briefed");
	public static final ResourceLocation CRIMSON_VESTMENT_INSPECTED = id("artificer_crimson_vestment_inspected");
	public static final ResourceLocation CRIMSON_VESTMENT_COUNSELED = id("artificer_crimson_vestment_counseled");
	public static final ResourceLocation CRIMSON_VESTMENT_DEMONSTRATED = id("artificer_crimson_vestment_demonstrated");
	public static final ResourceLocation ASSUMED_LIMB_BRIEFED = id("artificer_assumed_limb_briefed");
	public static final ResourceLocation ASSUMED_LIMB_DEMONSTRATED = id("artificer_assumed_limb_demonstrated");
	public static final ResourceLocation WEIGHT_OF_FRAME_BRIEFED = id("artificer_weight_of_frame_briefed");
	public static final ResourceLocation WEIGHT_OF_FRAME_INSPECTED = id("artificer_weight_of_frame_inspected");
	public static final ResourceLocation WEIGHT_OF_FRAME_DEMONSTRATED = id("artificer_weight_of_frame_demonstrated");
	public static final ResourceLocation WEIGHT_OF_FRAME_LESSON_READY = id("artificer_weight_of_frame_lesson_ready");

	private ArtificerAssignments() {
	}

	public static void onArmaturePlaced(ServerPlayer player) {
		HarbingerAdvancementGranter.grantIfNotDone(player,
				HarbingerAdvancementGranter.ADV_ARTIFICER_ARMATURE_PLACED);
	}

	public static void onArmatureUpgrade(ServerPlayer player, ItemStack upgraded, int requiredDegree) {
		if (ArmorSetHelper.isHematicIronArmorPiece(upgraded)) {
			HarbingerAdvancementGranter.grantIfNotDone(player,
					HarbingerAdvancementGranter.ADV_ARTIFICER_FIRST_HEMATIC_UPGRADE);
		}
		if (ArmorSetHelper.isForkArmorPiece(upgraded)) {
			recordForkFamily(player, forkFamily(upgraded));
			HarbingerAdvancementGranter.grantIfNotDone(player,
					HarbingerAdvancementGranter.ADV_ARTIFICER_FIRST_FORK_UPGRADE);
		}
		if (ArmorSetHelper.isBloodLustArmorPiece(upgraded)) {
			HarbingerAdvancementGranter.grantIfNotDone(player,
					HarbingerAdvancementGranter.ADV_ARTIFICER_FIRST_BLOOD_LUST_UPGRADE);
		}
		if (ArmorSetHelper.isD7ArmorPiece(upgraded)) {
			recordD7Lineage(player, d7Lineage(upgraded));
			HarbingerAdvancementGranter.grantIfNotDone(player,
					HarbingerAdvancementGranter.ADV_ARTIFICER_FIRST_D7_UPGRADE);
		}
		syncReadyToClaimAdvancements(player);
	}

	public static void onArmatureTierApplied(ServerPlayer player, ArmatureUpgradeRules.ArmatureTier tier) {
		if (tier.id() >= ArmatureUpgradeRules.ArmatureTier.VICAR_CONSECRATED.id()) {
			HarbingerAdvancementGranter.grantIfNotDone(player,
					HarbingerAdvancementGranter.ADV_ARTIFICER_FRAME_CONSECRATED);
		}
		if (tier.id() >= ArmatureUpgradeRules.ArmatureTier.MONOLITHIC.id()) {
			HarbingerAdvancementGranter.grantIfNotDone(player,
					HarbingerAdvancementGranter.ADV_ARTIFICER_MONOLITHIC_FRAME);
		}
	}

	public static void onLivingWeaponGraftComplete(ServerPlayer player) {
		HarbingerAdvancementGranter.grantIfNotDone(player,
				HarbingerAdvancementGranter.ADV_ARTIFICER_FIRST_LIVING_GRAFT);
		syncReadyToClaimAdvancements(player);
	}

	public static void brief(ServerPlayer player, ResourceLocation briefing) {
		HarbingerAdvancementGranter.grantIfNotDone(player, briefing);
		syncReadyToClaimAdvancements(player);
	}

	public static boolean inspectThreeAnswers(ServerPlayer player) {
		if (!has(player, THREE_ANSWERS_BRIEFED)
				|| !HarbingerAdvancementGranter.isArtificerFirstForkUpgrade(player)
				|| firstForkFamily(player) == ForkFamily.NONE) return false;
		HarbingerAdvancementGranter.grantIfNotDone(player, THREE_ANSWERS_INSPECTED);
		syncReadyToClaimAdvancements(player);
		return true;
	}

	public static boolean inspectCrimsonVestment(ServerPlayer player) {
		if (!has(player, CRIMSON_VESTMENT_BRIEFED)
				|| !HarbingerAdvancementGranter.isArtificerFrameConsecrated(player)) return false;
		HarbingerAdvancementGranter.grantIfNotDone(player, CRIMSON_VESTMENT_INSPECTED);
		syncReadyToClaimAdvancements(player);
		return true;
	}

	public static ItemStack inspectWeightOfFrame(ServerPlayer player) {
		if (!has(player, WEIGHT_OF_FRAME_BRIEFED)
				|| !HarbingerAdvancementGranter.isArtificerFirstD7Upgrade(player)
				|| firstD7Lineage(player) == D7Lineage.NONE
				|| has(player, WEIGHT_OF_FRAME_INSPECTED)) return ItemStack.EMPTY;
		HarbingerAdvancementGranter.grantIfNotDone(player, WEIGHT_OF_FRAME_INSPECTED);
		ItemStack reward = claimD7Material(player);
		syncReadyToClaimAdvancements(player);
		return reward;
	}

	public static ItemStack counselThreeAnswers(ServerPlayer player) {
		if (!has(player, THREE_ANSWERS_INSPECTED)
				|| has(player, THREE_ANSWERS_COUNSELED)
				|| isArtificerLessonRewardClaimed(player, THREE_ANSWERS_REWARD_CLAIM_KEY)) return ItemStack.EMPTY;
		ItemStack reward = forkReward(firstForkFamily(player));
		if (reward.isEmpty()) return ItemStack.EMPTY;
		HarbingerAdvancementGranter.grantIfNotDone(player, THREE_ANSWERS_COUNSELED);
		markArtificerLessonRewardClaimed(player, THREE_ANSWERS_REWARD_CLAIM_KEY);
		syncReadyToClaimAdvancements(player);
		return reward;
	}

	public static int forkResearchRecordedCount(ServerPlayer player) {
		Set<String> recorded = HemoCapabilityAccess.getSpecimenBestiary(player)
				.map(progress -> progress.recordedSpecimens()).orElse(Set.of());
		return ArtificerProgressionRules.recordedForkResearchCount(firstForkFamily(player), recorded);
	}

	public static boolean isForkResearchRewardClaimed(ServerPlayer player) {
		return player.getPersistentData().getBoolean(FORK_RESEARCH_REWARD_CLAIM_KEY);
	}

	public static boolean canClaimForkResearchReward(ServerPlayer player) {
		Set<String> recorded = HemoCapabilityAccess.getSpecimenBestiary(player)
				.map(progress -> progress.recordedSpecimens()).orElse(Set.of());
		boolean correspondenceComplete = has(player, THREE_ANSWERS_COUNSELED)
				|| isArtificerLessonRewardClaimed(player, THREE_ANSWERS_REWARD_CLAIM_KEY);
		return ArtificerProgressionRules.canClaimForkResearchReward(firstForkFamily(player), recorded,
				correspondenceComplete, isForkResearchRewardClaimed(player));
	}

	public static ItemStack claimForkResearchReward(ServerPlayer player) {
		if (!canClaimForkResearchReward(player)) return ItemStack.EMPTY;
		ItemStack reward = forkReward(firstForkFamily(player));
		if (!reward.isEmpty()) player.getPersistentData().putBoolean(FORK_RESEARCH_REWARD_CLAIM_KEY, true);
		return reward;
	}

	public static ItemStack claimWornVowInspection(ServerPlayer player) {
		if (!has(player, WORN_VOW_BRIEFED)
				|| !HarbingerAdvancementGranter.isArtificerArmaturePlaced(player)
				|| !HarbingerAdvancementGranter.isArtificerFirstHematicUpgrade(player)
				|| isArtificerLessonRewardClaimed(player, WORN_VOW_REWARD_CLAIM_KEY)) return ItemStack.EMPTY;
		markArtificerLessonRewardClaimed(player, WORN_VOW_REWARD_CLAIM_KEY);
		syncReadyToClaimAdvancements(player);
		return new ItemStack(ItemInit.hematic_iron_scrap.get(), 4);
	}

	public static ItemStack claimAssumedLimbInspection(ServerPlayer player) {
		if (!has(player, ASSUMED_LIMB_BRIEFED)
				|| !HarbingerAdvancementGranter.isArtificerFirstLivingGraft(player)
				|| isArtificerLessonRewardClaimed(player, ASSUMED_LIMB_REWARD_CLAIM_KEY)) return ItemStack.EMPTY;
		markArtificerLessonRewardClaimed(player, ASSUMED_LIMB_REWARD_CLAIM_KEY);
		syncReadyToClaimAdvancements(player);
		return new ItemStack(ItemInit.hematic_memory.get());
	}

	public static ItemStack counselCrimsonVestment(ServerPlayer player) {
		if (!has(player, CRIMSON_VESTMENT_INSPECTED)
				|| has(player, CRIMSON_VESTMENT_COUNSELED)
				|| isArtificerLessonRewardClaimed(player, CRIMSON_VESTMENT_REWARD_CLAIM_KEY)) return ItemStack.EMPTY;
		HarbingerAdvancementGranter.grantIfNotDone(player, CRIMSON_VESTMENT_COUNSELED);
		markArtificerLessonRewardClaimed(player, CRIMSON_VESTMENT_REWARD_CLAIM_KEY);
		syncReadyToClaimAdvancements(player);
		return new ItemStack(ItemInit.crimson_lacquer.get());
	}

	public static ItemStack claimD7Material(ServerPlayer player) {
		if (!has(player, WEIGHT_OF_FRAME_INSPECTED) && !HarbingerAdvancementGranter.isArtificerD7Fitting(player)
				|| player.getPersistentData().getBoolean(D7_REWARD_CLAIM_KEY)) return ItemStack.EMPTY;
		ItemStack reward = d7Reward(firstD7Lineage(player));
		if (reward.isEmpty()) return ItemStack.EMPTY;
		player.getPersistentData().putBoolean(D7_REWARD_CLAIM_KEY, true);
		return reward;
	}

	public static void onForkDemonstrated(ServerPlayer player, ForkFamily family) {
		if (family == firstForkFamily(player)
				&& ArtificerProgressionRules.canDemonstrate(has(player, THREE_ANSWERS_BRIEFED),
					has(player, THREE_ANSWERS_COUNSELED),
					HarbingerAdvancementGranter.isArtificerFirstForkUpgrade(player), hasFullForkFamily(player, family))) {
			HarbingerAdvancementGranter.grantIfNotDone(player, THREE_ANSWERS_DEMONSTRATED);
			syncReadyToClaimAdvancements(player);
		}
	}

	public static void onBloodLustDemonstrated(ServerPlayer player) {
		if (ArtificerProgressionRules.canDemonstrate(has(player, CRIMSON_VESTMENT_BRIEFED),
				has(player, CRIMSON_VESTMENT_COUNSELED),
				HarbingerAdvancementGranter.isArtificerFirstBloodLustUpgrade(player),
				ArmorSetHelper.hasFullBloodLustSet(player))) {
			HarbingerAdvancementGranter.grantIfNotDone(player, CRIMSON_VESTMENT_DEMONSTRATED);
			syncReadyToClaimAdvancements(player);
		}
	}

	public static void onLivingArsenalKill(ServerPlayer player) {
		if (has(player, ASSUMED_LIMB_BRIEFED)
				&& HarbingerAdvancementGranter.isArtificerFirstLivingGraft(player)) {
			HarbingerAdvancementGranter.grantIfNotDone(player, ASSUMED_LIMB_DEMONSTRATED);
			syncReadyToClaimAdvancements(player);
		}
	}

	public static void onD7AbilityActivated(ServerPlayer player) {
		if (has(player, WEIGHT_OF_FRAME_BRIEFED)
				&& HarbingerAdvancementGranter.isArtificerFirstD7Upgrade(player)
				&& hasFullD7Lineage(player, firstD7Lineage(player))) {
			HarbingerAdvancementGranter.grantIfNotDone(player, WEIGHT_OF_FRAME_DEMONSTRATED);
			syncReadyToClaimAdvancements(player);
		}
	}

	public static void syncReadyToClaimAdvancements(ServerPlayer player) {
		if (has(player, WORN_VOW_BRIEFED)) grantLessonReadyIfUnclaimed(player,
				HarbingerAdvancementGranter.ADV_ARTIFICER_FIRST_HEMATIC_UPGRADE,
				WORN_VOW_REWARD_CLAIM_KEY,
				HarbingerAdvancementGranter.ADV_ARTIFICER_WORN_VOW_LESSON_READY);
		if (has(player, THREE_ANSWERS_BRIEFED)) grantLessonReadyIfUnclaimed(player,
				HarbingerAdvancementGranter.ADV_ARTIFICER_FIRST_FORK_UPGRADE,
				THREE_ANSWERS_REWARD_CLAIM_KEY,
				HarbingerAdvancementGranter.ADV_ARTIFICER_THREE_ANSWERS_LESSON_READY);
		if (has(player, CRIMSON_VESTMENT_BRIEFED)) grantLessonReadyIfUnclaimed(player,
				HarbingerAdvancementGranter.ADV_ARTIFICER_FRAME_CONSECRATED,
				CRIMSON_VESTMENT_REWARD_CLAIM_KEY,
				HarbingerAdvancementGranter.ADV_ARTIFICER_CRIMSON_VESTMENT_LESSON_READY);
		if (has(player, ASSUMED_LIMB_BRIEFED)) grantLessonReadyIfUnclaimed(player,
				HarbingerAdvancementGranter.ADV_ARTIFICER_FIRST_LIVING_GRAFT,
				ASSUMED_LIMB_REWARD_CLAIM_KEY,
				HarbingerAdvancementGranter.ADV_ARTIFICER_ASSUMED_LIMB_LESSON_READY);
		if (has(player, WEIGHT_OF_FRAME_BRIEFED)
				&& HarbingerAdvancementGranter.isArtificerFirstD7Upgrade(player)
				&& !player.getPersistentData().getBoolean(D7_REWARD_CLAIM_KEY)) {
			HarbingerAdvancementGranter.grantIfNotDone(player, WEIGHT_OF_FRAME_LESSON_READY);
		}

		if (has(player, WORN_VOW_BRIEFED)
				&& isArtificerLessonRewardClaimed(player, WORN_VOW_REWARD_CLAIM_KEY)
				&& !HarbingerAdvancementGranter.isArtificerHematicIronFitting(player)
				&& ArmorSetHelper.hasFullHematicIronSet(player)) {
			HarbingerAdvancementGranter.grantIfNotDone(player,
					HarbingerAdvancementGranter.ADV_ARTIFICER_WORN_VOW_FITTING_READY);
		}
		if (!HarbingerAdvancementGranter.isArtificerForkFitting(player)
				&& has(player, THREE_ANSWERS_DEMONSTRATED)
				&& hasFullForkFamily(player, firstForkFamily(player))) {
			HarbingerAdvancementGranter.grantIfNotDone(player,
					HarbingerAdvancementGranter.ADV_ARTIFICER_THREE_ANSWERS_FITTING_READY);
		}
		if (!HarbingerAdvancementGranter.isArtificerBloodLustFitting(player)
				&& has(player, CRIMSON_VESTMENT_DEMONSTRATED)
				&& ArmorSetHelper.hasFullBloodLustSet(player)) {
			HarbingerAdvancementGranter.grantIfNotDone(player,
					HarbingerAdvancementGranter.ADV_ARTIFICER_CRIMSON_VESTMENT_FITTING_READY);
		}
		if (!HarbingerAdvancementGranter.isArtificerD7Fitting(player)
				&& has(player, WEIGHT_OF_FRAME_DEMONSTRATED)
				&& hasFullD7Lineage(player, firstD7Lineage(player))) {
			HarbingerAdvancementGranter.grantIfNotDone(player,
					HarbingerAdvancementGranter.ADV_ARTIFICER_WEIGHT_OF_THE_FRAME_FITTING_READY);
		}
		if (!HarbingerAdvancementGranter.isArtificerLivingArsenalFitting(player)
				&& has(player, ASSUMED_LIMB_DEMONSTRATED) && knowsFullLivingArsenal(player)) {
			HarbingerAdvancementGranter.grantIfNotDone(player,
					HarbingerAdvancementGranter.ADV_ARTIFICER_ASSUMED_LIMB_FITTING_READY);
		}
	}

	public static boolean isArtificerLessonRewardClaimed(ServerPlayer player, String claimKey) {
		return player.getPersistentData().getBoolean(claimKey);
	}

	public static void markArtificerLessonRewardClaimed(ServerPlayer player, String claimKey) {
		player.getPersistentData().putBoolean(claimKey, true);
	}

	private static void grantLessonReadyIfUnclaimed(ServerPlayer player, net.minecraft.resources.ResourceLocation prerequisite,
			String claimKey, net.minecraft.resources.ResourceLocation readyAdvancement) {
		if (HarbingerAdvancementGranter.hasAdvancement(player, prerequisite)
				&& !isArtificerLessonRewardClaimed(player, claimKey)) {
			HarbingerAdvancementGranter.grantIfNotDone(player, readyAdvancement);
		}
	}

	public static int knownLivingWeaponFormCount(ServerPlayer player) {
		return HemoCapabilityAccess.getKnownManipulations(player)
				.map(known -> {
					int count = 0;
					for (LivingWeaponForm form : LivingWeaponForm.values()) {
						boolean knownForm = known.getKnownManips().keySet().stream()
								.anyMatch(manip -> manip != null
										&& form.manipulationName().equals(manip.getName()));
						if (knownForm) {
							count++;
						}
					}
					return count;
				}).orElse(0);
	}

	public static boolean knowsFullLivingArsenal(ServerPlayer player) {
		return knownLivingWeaponFormCount(player) >= LivingWeaponForm.values().length;
	}

	public static ItemStack earnedHematicIronFitting(ServerPlayer player) {
		if (HarbingerAdvancementGranter.isArtificerHematicIronFitting(player)) {
			return new ItemStack(ItemInit.worn_vow_fitting.get());
		}
		return ItemStack.EMPTY;
	}

	public static ItemStack tryGrantHematicIronFitting(ServerPlayer player) {
		if (!canGrantHematicIronFitting(player)) {
			return ItemStack.EMPTY;
		}
		HarbingerAdvancementGranter.grantIfNotDone(player,
				HarbingerAdvancementGranter.ADV_ARTIFICER_HEMATIC_IRON_FITTING);
		return new ItemStack(ItemInit.worn_vow_fitting.get());
	}

	public static boolean canGrantHematicIronFitting(ServerPlayer player) {
		return has(player, WORN_VOW_BRIEFED)
				&& isArtificerLessonRewardClaimed(player, WORN_VOW_REWARD_CLAIM_KEY)
				&& ArmorSetHelper.hasFullHematicIronSet(player);
	}

	public static ItemStack earnedForkFitting(ServerPlayer player) {
		if (HarbingerAdvancementGranter.isArtificerBarbedFitting(player)) {
			return new ItemStack(ItemInit.barbed_fitting.get());
		}
		if (HarbingerAdvancementGranter.isArtificerChitiniteFitting(player)) {
			return new ItemStack(ItemInit.chitinite_fitting.get());
		}
		if (HarbingerAdvancementGranter.isArtificerPrismaticFitting(player)) {
			return new ItemStack(ItemInit.prismatic_fitting.get());
		}
		return ItemStack.EMPTY;
	}

	public static ItemStack tryGrantForkFitting(ServerPlayer player) {
		ItemStack alreadyEarned = earnedForkFitting(player);
		if (!alreadyEarned.isEmpty()) {
			return alreadyEarned;
		}
		if (!has(player, THREE_ANSWERS_DEMONSTRATED)) return ItemStack.EMPTY;
		if (firstForkFamily(player) == ForkFamily.BARBED && ArmorSetHelper.hasFullBarbedSet(player)) {
			HarbingerAdvancementGranter.grantIfNotDone(player,
					HarbingerAdvancementGranter.ADV_ARTIFICER_BARBED_FITTING);
			return new ItemStack(ItemInit.barbed_fitting.get());
		}
		if (firstForkFamily(player) == ForkFamily.CHITINITE && ArmorSetHelper.hasFullChitiniteSet(player)) {
			HarbingerAdvancementGranter.grantIfNotDone(player,
					HarbingerAdvancementGranter.ADV_ARTIFICER_CHITINITE_FITTING);
			return new ItemStack(ItemInit.chitinite_fitting.get());
		}
		if (firstForkFamily(player) == ForkFamily.PRISMATIC && ArmorSetHelper.hasFullPrismaticSet(player)) {
			HarbingerAdvancementGranter.grantIfNotDone(player,
					HarbingerAdvancementGranter.ADV_ARTIFICER_PRISMATIC_FITTING);
			return new ItemStack(ItemInit.prismatic_fitting.get());
		}
		return ItemStack.EMPTY;
	}

	public static ItemStack earnedBloodLustFitting(ServerPlayer player) {
		if (HarbingerAdvancementGranter.isArtificerBloodLustFitting(player)) {
			return new ItemStack(ItemInit.crimson_vestment_fitting.get());
		}
		return ItemStack.EMPTY;
	}

	public static ItemStack tryGrantBloodLustFitting(ServerPlayer player) {
		if (!has(player, CRIMSON_VESTMENT_DEMONSTRATED) || !ArmorSetHelper.hasFullBloodLustSet(player)) {
			return ItemStack.EMPTY;
		}
		HarbingerAdvancementGranter.grantIfNotDone(player,
				HarbingerAdvancementGranter.ADV_ARTIFICER_BLOOD_LUST_FITTING);
		return new ItemStack(ItemInit.crimson_vestment_fitting.get());
	}

	public static ItemStack earnedD7Fitting(ServerPlayer player) {
		if (HarbingerAdvancementGranter.isArtificerD7Fitting(player)) {
			return new ItemStack(ItemInit.monolithic_frame_fitting.get());
		}
		return ItemStack.EMPTY;
	}

	public static ItemStack tryGrantD7Fitting(ServerPlayer player) {
		if (!has(player, WEIGHT_OF_FRAME_DEMONSTRATED)
				|| !hasFullD7Lineage(player, firstD7Lineage(player))) {
			return ItemStack.EMPTY;
		}
		HarbingerAdvancementGranter.grantIfNotDone(player,
				HarbingerAdvancementGranter.ADV_ARTIFICER_D7_FITTING);
		return new ItemStack(ItemInit.monolithic_frame_fitting.get());
	}

	public static ItemStack earnedLivingArsenalFitting(ServerPlayer player) {
		if (HarbingerAdvancementGranter.isArtificerLivingArsenalFitting(player)) {
			return new ItemStack(ItemInit.assumed_limb_fitting.get());
		}
		return ItemStack.EMPTY;
	}

	public static ItemStack tryGrantLivingArsenalFitting(ServerPlayer player) {
		if (!has(player, ASSUMED_LIMB_DEMONSTRATED) || !knowsFullLivingArsenal(player)) {
			return ItemStack.EMPTY;
		}
		HarbingerAdvancementGranter.grantIfNotDone(player,
				HarbingerAdvancementGranter.ADV_ARTIFICER_LIVING_ARSENAL_FITTING);
		return new ItemStack(ItemInit.assumed_limb_fitting.get());
	}

	public static ForkFamily firstForkFamily(ServerPlayer player) {
		ForkFamily stored = ForkFamily.fromSerializedName(player.getPersistentData().getString(FIRST_FORK_FAMILY_KEY));
		if (stored != ForkFamily.NONE) return stored;
		if (HarbingerAdvancementGranter.isArtificerBarbedFitting(player)) return ForkFamily.BARBED;
		if (HarbingerAdvancementGranter.isArtificerChitiniteFitting(player)) return ForkFamily.CHITINITE;
		if (HarbingerAdvancementGranter.isArtificerPrismaticFitting(player)) return ForkFamily.PRISMATIC;
		return ForkFamily.NONE;
	}

	public static D7Lineage firstD7Lineage(ServerPlayer player) {
		return D7Lineage.fromSerializedName(player.getPersistentData().getString(FIRST_D7_LINEAGE_KEY));
	}

	public static void recordForkFamily(ServerPlayer player, ForkFamily family) {
		if (family != ForkFamily.NONE && firstForkFamily(player) == ForkFamily.NONE) {
			player.getPersistentData().putString(FIRST_FORK_FAMILY_KEY, family.serializedName());
		}
	}

	public static void recordD7Lineage(ServerPlayer player, D7Lineage lineage) {
		if (lineage != D7Lineage.NONE && firstD7Lineage(player) == D7Lineage.NONE) {
			player.getPersistentData().putString(FIRST_D7_LINEAGE_KEY, lineage.serializedName());
		}
	}

	public static boolean has(ServerPlayer player, ResourceLocation advancement) {
		return HarbingerAdvancementGranter.hasAdvancement(player, advancement);
	}

	public static boolean hasFullForkFamily(ServerPlayer player, ForkFamily family) {
		return switch (family) {
			case BARBED -> ArmorSetHelper.hasFullBarbedSet(player);
			case CHITINITE -> ArmorSetHelper.hasFullChitiniteSet(player);
			case PRISMATIC -> ArmorSetHelper.hasFullPrismaticSet(player);
			case NONE -> false;
		};
	}

	public static boolean hasFullD7Lineage(ServerPlayer player, D7Lineage lineage) {
		return switch (lineage) {
			case SILENT_ARCHON -> ArmorSetHelper.hasFullSilentArchonSet(player);
			case EDACIOUS -> ArmorSetHelper.hasFullEdaciousBloodlust(player);
			case SHEOLIC -> ArmorSetHelper.hasFullSheolicBloodlust(player);
			case PHANTASMAL -> ArmorSetHelper.hasFullPhantasmalBloodlust(player);
			case NONE -> false;
		};
	}

	public static boolean hasFitting(ServerPlayer player, Item fitting) {
		for (ItemStack stack : player.getInventory().items) if (stack.is(fitting)) return true;
		for (ItemStack stack : player.getInventory().armor) if (stack.is(fitting)) return true;
		for (ItemStack stack : player.getInventory().offhand) if (stack.is(fitting)) return true;
		return HemoCapabilityAccess.getEquipment(player).map(equipment -> {
			for (int slot = 0; slot < equipment.getSlots(); slot++) {
				if (equipment.getStackInSlot(slot).is(fitting)) return true;
			}
			return false;
		}).orElse(false);
	}

	private static ForkFamily forkFamily(ItemStack stack) {
		if (isAny(stack, ItemInit.barbed_helm.get(), ItemInit.barbed_chestplate.get(),
				ItemInit.barbed_leggings.get(), ItemInit.barbed_boots.get())) return ForkFamily.BARBED;
		if (isAny(stack, ItemInit.chitinite_helm.get(), ItemInit.chitinite_chestplate.get(),
				ItemInit.chitinite_leggings.get(), ItemInit.chitinite_boots.get())) return ForkFamily.CHITINITE;
		if (isAny(stack, ItemInit.prismatic_helm.get(), ItemInit.prismatic_chestplate.get(),
				ItemInit.prismatic_leggings.get(), ItemInit.prismatic_boots.get())) return ForkFamily.PRISMATIC;
		return ForkFamily.NONE;
	}

	private static D7Lineage d7Lineage(ItemStack stack) {
		if (isAny(stack, ItemInit.silent_archon_helm.get(), ItemInit.silent_archon_chestplate.get(),
				ItemInit.silent_archon_leggings.get(), ItemInit.silent_archon_boots.get())) return D7Lineage.SILENT_ARCHON;
		if (isAny(stack, ItemInit.edacious_blood_lust_helm.get(), ItemInit.edacious_blood_lust_chest.get(),
				ItemInit.edacious_blood_lust_legs.get(), ItemInit.edacious_blood_lust_boots.get())) return D7Lineage.EDACIOUS;
		if (isAny(stack, ItemInit.sheolic_blood_lust_helm.get(), ItemInit.sheolic_blood_lust_chest.get(),
				ItemInit.sheolic_blood_lust_legs.get(), ItemInit.sheolic_blood_lust_boots.get())) return D7Lineage.SHEOLIC;
		if (isAny(stack, ItemInit.phantasmal_blood_lust_helm.get(), ItemInit.phantasmal_blood_lust_chest.get(),
				ItemInit.phantasmal_blood_lust_legs.get(), ItemInit.phantasmal_blood_lust_boots.get())) return D7Lineage.PHANTASMAL;
		return D7Lineage.NONE;
	}

	private static ItemStack forkReward(ForkFamily family) {
		return switch (family) {
			case BARBED -> new ItemStack(ItemInit.aculeate_vitriol.get());
			case CHITINITE -> new ItemStack(ItemInit.sclerotic_oleum.get());
			case PRISMATIC -> new ItemStack(ItemInit.chromatic_sublimate.get());
			case NONE -> ItemStack.EMPTY;
		};
	}

	private static ItemStack d7Reward(D7Lineage lineage) {
		return switch (lineage) {
			case SILENT_ARCHON -> new ItemStack(ItemInit.monolith_imbued_cloth.get());
			case EDACIOUS -> new ItemStack(ItemInit.fargone_proboscis.get());
			case SHEOLIC -> new ItemStack(ItemInit.fervent_husk.get());
			case PHANTASMAL -> new ItemStack(ItemInit.mnemonic_ambergris.get());
			case NONE -> ItemStack.EMPTY;
		};
	}

	private static boolean isAny(ItemStack stack, Item... items) {
		for (Item item : items) if (stack.is(item)) return true;
		return false;
	}

	private static ResourceLocation id(String path) {
		return Hemomancy.rloc("hemomancy/" + path);
	}
}
