package com.vincenthuto.hemomancy.common.manipulation.animus;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.common.manipulation.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class CrimsonCoronationManip extends BloodManipulation {
	private static final int CHARGE_TICKS = 80;

	public CrimsonCoronationManip(String name, double cost, double alignment, double xpCost, EnumManipulationType type,
			EnumManipulationRank rank, EnumBloodTendency tendency, EnumVeinSections section) {
		super(name, cost, alignment, xpCost, type, rank, tendency, section);
	}

	@Override
	public int getRequiredChargeTicks() {
		return CHARGE_TICKS;
	}

	@Override
	protected boolean canPerformAction(Player player, ItemStack heldItem, float ticks) {
		if (ManipulationReactiveEvents.coronationActive(player)) {
			player.displayClientMessage(net.minecraft.network.chat.Component.translatable("message.hemomancy.crimson_coronation_manip.your_crown_still_holds_its_swords"), true);
			return false;
		}
		return super.canPerformAction(player, heldItem, ticks);
	}

	@Override
	public void getAction(Player player, Level world, ItemStack heldItemMainhand, BlockPos position, float heldTicks) {
        try (var schoolCast = com.vincenthuto.hemomancy.common.damage.SchoolDamage.cast(this, player, getRequiredChargeTicks() <= 0 ? 1 : heldTicks / getRequiredChargeTicks())) {

		float charge = ManipulationCastingRules.chargeFraction(heldTicks, CHARGE_TICKS);
		ManipulationReactiveEvents.armCoronation(player,
				ManipulationScalingRules.scaledCount(1, 8, heldTicks, CHARGE_TICKS), charge);
	        }
    }
}
