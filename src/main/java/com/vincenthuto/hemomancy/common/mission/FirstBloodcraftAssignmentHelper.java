package com.vincenthuto.hemomancy.common.mission;

import java.util.List;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public final class FirstBloodcraftAssignmentHelper {
	public static final ResourceLocation ADV_REWARD_CLAIMED =
			Hemomancy.rloc("hemomancy/first_bloodcraft_reward_claimed");

	private FirstBloodcraftAssignmentHelper() {
	}

	public static boolean canClaim(ServerPlayer player) {
		return HarbingerAdvancementGranter.isVesselFilled(player)
				&& HarbingerAdvancementGranter.isLiberSanguinumCrafted(player)
				&& HarbingerAdvancementGranter.isHematicIronBlockCrafted(player)
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
		return List.of(
				new ItemStack(ItemInit.hematic_iron_scrap.get(), 4),
				new ItemStack(BlockInit.befouling_ash_trail.get().asItem(), 8),
				new ItemStack(ItemInit.sanguine_formation.get(), 2));
	}
}
