package com.vincenthuto.hemomancy.common.manipulation.tenebris;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.common.manipulation.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class EclipseWellManip extends BloodManipulation {
	private static final int CHARGE_TICKS = 80;

	public EclipseWellManip(String name, double cost, double alignment, double xpCost, EnumManipulationType type,
			EnumManipulationRank rank, EnumBloodTendency tendency, EnumVeinSections section) {
		super(name, cost, alignment, xpCost, type, rank, tendency, section);
	}

	@Override public int getRequiredChargeTicks() { return CHARGE_TICKS; }

	@Override
	public void getAction(Player player, Level world, ItemStack heldItemMainhand, BlockPos position, float heldTicks) {
		if (!(world instanceof ServerLevel level)) return;
		double radius = ManipulationScalingRules.scaled(2, 7, heldTicks, CHARGE_TICKS);
		int duration = ManipulationScalingRules.scaledInt(40, 200, heldTicks, CHARGE_TICKS);
		Vec3 center = player.getEyePosition().add(player.getLookAngle().scale(12));
		ManipulationReactiveEvents.createEclipseWell(level, center, radius, duration, player.getUUID());
	}
}
