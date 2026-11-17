package com.vincenthuto.hemomancy.common.manipulation;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.EnumBloodTemper;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.EnumVeinSections;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * A memory with nothing to cast: it works only as a Sculpting of the verbs. Pressing R says so
 * ({@code message.hemomancy.<id>.verbs_only}) and costs nothing. Living Land and Venous Cling (core-loop Track 9).
 */
public class VerbsOnlyManip extends BloodManipulation {
	public VerbsOnlyManip(String name, double alignLevel, EnumManipulationRank rank, EnumBloodTemper temper,
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
