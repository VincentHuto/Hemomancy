package com.vincenthuto.hemomancy.common.mission;

import com.vincenthuto.hemomancy.common.armor.ArmorSetHelper;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.item.component.LivingWeaponForm;
import com.vincenthuto.hemomancy.common.recipe.ArmatureUpgradeRules;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public final class HarbingerArtificerAssignmentHelper {
	public static final String WORN_VOW_REWARD_CLAIM_KEY =
			"hemomancy.artificer.worn_vow_reward_claimed";
	public static final String THREE_ANSWERS_REWARD_CLAIM_KEY =
			"hemomancy.artificer.three_answers_reward_claimed";
	public static final String CRIMSON_VESTMENT_REWARD_CLAIM_KEY =
			"hemomancy.artificer.crimson_vestment_reward_claimed";
	public static final String ASSUMED_LIMB_REWARD_CLAIM_KEY =
			"hemomancy.artificer.assumed_limb_reward_claimed";

	private HarbingerArtificerAssignmentHelper() {
	}

	public static void onArmaturePlaced(ServerPlayer player) {
		HarbingerAdvancementGranter.grantIfNotDone(player,
				HarbingerAdvancementGranter.ADV_ARTIFICER_ARMATURE_PLACED);
	}

	public static void onArmatureUpgrade(ServerPlayer player, ItemStack upgraded, int requiredDegree) {
		if (ArmorSetHelper.isHematicIronArmorPiece(upgraded)) {
			HarbingerAdvancementGranter.grantIfNotDone(player,
					HarbingerAdvancementGranter.ADV_ARTIFICER_FIRST_HEMATIC_UPGRADE);
			grantLessonReadyIfUnclaimed(player,
					HarbingerAdvancementGranter.ADV_ARTIFICER_FIRST_HEMATIC_UPGRADE,
					WORN_VOW_REWARD_CLAIM_KEY,
					HarbingerAdvancementGranter.ADV_ARTIFICER_WORN_VOW_LESSON_READY);
		}
		if (ArmorSetHelper.isForkArmorPiece(upgraded)) {
			HarbingerAdvancementGranter.grantIfNotDone(player,
					HarbingerAdvancementGranter.ADV_ARTIFICER_FIRST_FORK_UPGRADE);
			grantLessonReadyIfUnclaimed(player,
					HarbingerAdvancementGranter.ADV_ARTIFICER_FIRST_FORK_UPGRADE,
					THREE_ANSWERS_REWARD_CLAIM_KEY,
					HarbingerAdvancementGranter.ADV_ARTIFICER_THREE_ANSWERS_LESSON_READY);
		}
		if (ArmorSetHelper.isBloodLustArmorPiece(upgraded)) {
			HarbingerAdvancementGranter.grantIfNotDone(player,
					HarbingerAdvancementGranter.ADV_ARTIFICER_FIRST_BLOOD_LUST_UPGRADE);
			grantLessonReadyIfUnclaimed(player,
					HarbingerAdvancementGranter.ADV_ARTIFICER_FIRST_BLOOD_LUST_UPGRADE,
					CRIMSON_VESTMENT_REWARD_CLAIM_KEY,
					HarbingerAdvancementGranter.ADV_ARTIFICER_CRIMSON_VESTMENT_LESSON_READY);
		}
		if (requiredDegree >= 7 || ArmorSetHelper.isD7ArmorPiece(upgraded)) {
			HarbingerAdvancementGranter.grantIfNotDone(player,
					HarbingerAdvancementGranter.ADV_ARTIFICER_FIRST_D7_UPGRADE);
		}
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
		grantLessonReadyIfUnclaimed(player,
				HarbingerAdvancementGranter.ADV_ARTIFICER_FIRST_LIVING_GRAFT,
				ASSUMED_LIMB_REWARD_CLAIM_KEY,
				HarbingerAdvancementGranter.ADV_ARTIFICER_ASSUMED_LIMB_LESSON_READY);
	}

	public static void syncReadyToClaimAdvancements(ServerPlayer player) {
		grantLessonReadyIfUnclaimed(player,
				HarbingerAdvancementGranter.ADV_ARTIFICER_FIRST_HEMATIC_UPGRADE,
				WORN_VOW_REWARD_CLAIM_KEY,
				HarbingerAdvancementGranter.ADV_ARTIFICER_WORN_VOW_LESSON_READY);
		grantLessonReadyIfUnclaimed(player,
				HarbingerAdvancementGranter.ADV_ARTIFICER_FIRST_FORK_UPGRADE,
				THREE_ANSWERS_REWARD_CLAIM_KEY,
				HarbingerAdvancementGranter.ADV_ARTIFICER_THREE_ANSWERS_LESSON_READY);
		grantLessonReadyIfUnclaimed(player,
				HarbingerAdvancementGranter.ADV_ARTIFICER_FIRST_BLOOD_LUST_UPGRADE,
				CRIMSON_VESTMENT_REWARD_CLAIM_KEY,
				HarbingerAdvancementGranter.ADV_ARTIFICER_CRIMSON_VESTMENT_LESSON_READY);
		grantLessonReadyIfUnclaimed(player,
				HarbingerAdvancementGranter.ADV_ARTIFICER_FIRST_LIVING_GRAFT,
				ASSUMED_LIMB_REWARD_CLAIM_KEY,
				HarbingerAdvancementGranter.ADV_ARTIFICER_ASSUMED_LIMB_LESSON_READY);

		if (!HarbingerAdvancementGranter.isArtificerHematicIronFitting(player)
				&& ArmorSetHelper.hasFullHematicIronSet(player)) {
			HarbingerAdvancementGranter.grantIfNotDone(player,
					HarbingerAdvancementGranter.ADV_ARTIFICER_WORN_VOW_FITTING_READY);
		}
		if (!HarbingerAdvancementGranter.isArtificerForkFitting(player)
				&& ArmorSetHelper.hasFullForkSet(player)) {
			HarbingerAdvancementGranter.grantIfNotDone(player,
					HarbingerAdvancementGranter.ADV_ARTIFICER_THREE_ANSWERS_FITTING_READY);
		}
		if (!HarbingerAdvancementGranter.isArtificerBloodLustFitting(player)
				&& ArmorSetHelper.hasFullBloodLustSet(player)) {
			HarbingerAdvancementGranter.grantIfNotDone(player,
					HarbingerAdvancementGranter.ADV_ARTIFICER_CRIMSON_VESTMENT_FITTING_READY);
		}
		if (!HarbingerAdvancementGranter.isArtificerD7Fitting(player)
				&& ArmorSetHelper.hasFullD7Set(player)) {
			HarbingerAdvancementGranter.grantIfNotDone(player,
					HarbingerAdvancementGranter.ADV_ARTIFICER_WEIGHT_OF_THE_FRAME_FITTING_READY);
		}
		if (!HarbingerAdvancementGranter.isArtificerLivingArsenalFitting(player)
				&& knowsFullLivingArsenal(player)) {
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
		return ArmorSetHelper.hasFullHematicIronSet(player);
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
		if (ArmorSetHelper.hasFullBarbedSet(player)) {
			HarbingerAdvancementGranter.grantIfNotDone(player,
					HarbingerAdvancementGranter.ADV_ARTIFICER_BARBED_FITTING);
			return new ItemStack(ItemInit.barbed_fitting.get());
		}
		if (ArmorSetHelper.hasFullChitiniteSet(player)) {
			HarbingerAdvancementGranter.grantIfNotDone(player,
					HarbingerAdvancementGranter.ADV_ARTIFICER_CHITINITE_FITTING);
			return new ItemStack(ItemInit.chitinite_fitting.get());
		}
		if (ArmorSetHelper.hasFullPrismaticSet(player)) {
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
		if (!ArmorSetHelper.hasFullBloodLustSet(player)) {
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
		if (!ArmorSetHelper.hasFullD7Set(player)) {
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
		if (!knowsFullLivingArsenal(player)) {
			return ItemStack.EMPTY;
		}
		HarbingerAdvancementGranter.grantIfNotDone(player,
				HarbingerAdvancementGranter.ADV_ARTIFICER_LIVING_ARSENAL_FITTING);
		return new ItemStack(ItemInit.assumed_limb_fitting.get());
	}
}
