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
		}
		if (ArmorSetHelper.isForkArmorPiece(upgraded)) {
			HarbingerAdvancementGranter.grantIfNotDone(player,
					HarbingerAdvancementGranter.ADV_ARTIFICER_FIRST_FORK_UPGRADE);
		}
		if (ArmorSetHelper.isBloodLustArmorPiece(upgraded)) {
			HarbingerAdvancementGranter.grantIfNotDone(player,
					HarbingerAdvancementGranter.ADV_ARTIFICER_FIRST_BLOOD_LUST_UPGRADE);
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
		if (!ArmorSetHelper.hasFullHematicIronSet(player)) {
			return ItemStack.EMPTY;
		}
		HarbingerAdvancementGranter.grantIfNotDone(player,
				HarbingerAdvancementGranter.ADV_ARTIFICER_HEMATIC_IRON_FITTING);
		return new ItemStack(ItemInit.worn_vow_fitting.get());
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
