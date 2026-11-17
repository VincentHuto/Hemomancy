package com.vincenthuto.hemomancy.common.manipulation.ferric;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.EnumBloodTemper;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationRank;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationType;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Lode Draw and Hematic Hewing (core-loop Track 8): the Staff's projection mining. Both are Sculptings and nothing
 * else; pressing R says so and costs nothing. Held Projection on an ore (Lode Draw) or a face (Hematic Hewing) does
 * the work, from the Staff at its full reach or from the cell hand at a bare hand's.
 */
public class StaffMiningManip extends BloodManipulation {
	public StaffMiningManip(String name, double alignLevel, EnumManipulationRank rank, EnumBloodTemper temper,
			EnumVeinSections section) {
		super(name, 0, alignLevel, 0, EnumManipulationType.QUICK, rank, temper, section);
	}

	@Override
	protected boolean canPerformAction(Player player, ItemStack heldItemMainhand, float chargeTicks) {
		player.displayClientMessage(Component.translatable("message.hemomancy." + getName() + ".verbs_only")
				.withStyle(ChatFormatting.GRAY), true);
		return false;
	}
}
