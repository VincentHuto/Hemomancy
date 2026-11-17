package com.vincenthuto.hemomancy.common.manipulation.animus;

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
 * Blood Lash (core-loop Track 12, CL-23): the first strand a Harbinger meets, a weak Animus lash that teaches the
 * motion before Searing Lash. It has nothing to cast on R: held Projection puts out the strand, and how the player
 * sweeps it decides what it does. Pressing R says so and costs nothing.
 */
public class BloodLashManip extends BloodManipulation {
	public BloodLashManip(String name, double cost, double alignLevel, double xpCost, EnumManipulationType type,
			EnumManipulationRank rank, EnumBloodTemper temper, EnumVeinSections section) {
		super(name, cost, alignLevel, xpCost, type, rank, temper, section);
	}

	@Override
	protected boolean canPerformAction(Player player, ItemStack heldItemMainhand, float chargeTicks) {
		player.displayClientMessage(Component.translatable("message.hemomancy.blood_lash.verbs_only")
				.withStyle(ChatFormatting.GRAY), true);
		return false;
	}
}
