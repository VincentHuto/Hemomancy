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
 * The Tendon Line: a memory with nothing to cast on R. It works only through the verbs ({@link TendonLineSculpting}):
 * hold Projection to throw and root the line, hold Absorption to haul yourself along it. Pressing R says so and costs
 * nothing.
 */
public class TendonLineManip extends BloodManipulation {
	public TendonLineManip(String name, double cost, double alignLevel, double xpCost, EnumManipulationType type,
			EnumManipulationRank rank, EnumBloodTemper temper, EnumVeinSections section) {
		super(name, cost, alignLevel, xpCost, type, rank, temper, section);
	}

	@Override
	protected boolean canPerformAction(Player player, ItemStack heldItemMainhand, float chargeTicks) {
		player.displayClientMessage(Component.translatable("message.hemomancy.tendon_line.verbs_only")
				.withStyle(ChatFormatting.GRAY), true);
		return false;
	}
}
