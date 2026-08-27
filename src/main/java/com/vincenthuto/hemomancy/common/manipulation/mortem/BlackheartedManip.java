package com.vincenthuto.hemomancy.common.manipulation.mortem;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationRank;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationType;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

public class BlackheartedManip extends BloodManipulation {
	public BlackheartedManip(String name, double cost, double alignLevel, double xpCost,
			EnumManipulationType type, EnumManipulationRank rank, EnumBloodTendency tendency,
			EnumVeinSections section) {
		super(name, cost, alignLevel, xpCost, type, rank, tendency, section);
	}

	@Override
	protected boolean canPerformAction(Player player, float chargeTicks) {
		player.displayClientMessage(Component.translatable("message.hemomancy.blackhearted.passive")
				.withStyle(ChatFormatting.DARK_GRAY), true);
		return false;
	}
}
