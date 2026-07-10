package com.vincenthuto.hemomancy.common.mission;

import java.util.List;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.VialRackItem;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/** Server-authoritative state and rewards for the Degree-2 First Separation assignment. */
public final class FirstSeparationAssignmentHelper {
	public static final ResourceLocation ADV_BRIEFED =
			Hemomancy.rloc("hemomancy/first_separation_briefed");
	public static final ResourceLocation ADV_REWARD_CLAIMED =
			Hemomancy.rloc("hemomancy/first_separation_reward_claimed");

	private FirstSeparationAssignmentHelper() {
	}

	public static boolean canBrief(ServerPlayer player) {
		return HemoCapabilityAccess.getPlayerDegreeNumber(player) >= 2 && !isBriefed(player);
	}

	public static boolean isBriefed(ServerPlayer player) {
		return HarbingerAdvancementGranter.hasAdvancement(player, ADV_BRIEFED);
	}

	public static boolean markBriefed(ServerPlayer player) {
		HarbingerAdvancementGranter.grantIfNotDone(player, ADV_BRIEFED);
		return isBriefed(player);
	}

	public static boolean canClaim(ServerPlayer player) {
		return HemoCapabilityAccess.getPlayerDegreeNumber(player) >= 2
				&& isBriefed(player)
				&& HarbingerAdvancementGranter.isFirstSeparationStarted(player)
				&& HarbingerAdvancementGranter.isFirstSeparationComplete(player)
				&& !isClaimed(player);
	}

	public static boolean isClaimed(ServerPlayer player) {
		return HarbingerAdvancementGranter.hasAdvancement(player, ADV_REWARD_CLAIMED);
	}

	public static boolean markClaimed(ServerPlayer player) {
		HarbingerAdvancementGranter.grantIfNotDone(player, ADV_REWARD_CLAIMED);
		return isClaimed(player);
	}

	public static List<ItemStack> rewardStacks() {
		ItemStack rack = new ItemStack(ItemInit.vial_rack.get());
		VialRackItem.ensureInitialized(rack);
		return List.of(new ItemStack(ItemInit.living_syringe.get()), rack);
	}
}
