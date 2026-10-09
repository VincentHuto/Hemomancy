package com.vincenthuto.hemomancy.common.manipulation.animus;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationRank;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationType;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Grows a limb the body never had: a clawed blood-arm erupts between the shoulder blades and slaps across the front.
 * Hits caught inside its brief window are turned aside; see {@link HematicRiposteEvents}.
 */
public class HematicRiposteManip extends BloodManipulation {

	public HematicRiposteManip(String name, double cost, double alignLevel, double xpCost, EnumManipulationType type,
			EnumManipulationRank rank, EnumBloodTendency tendency, EnumVeinSections section) {
		super(name, cost, alignLevel, xpCost, type, rank, tendency, section);
	}

	@Override
	public void getAction(Player player, Level world, ItemStack heldItemMainhand, BlockPos position) {
		if (!(player instanceof ServerPlayer serverPlayer)) return;
		int side = serverPlayer.getRandom().nextBoolean() ? 1 : -1;
		int tilt = serverPlayer.getRandom().nextInt(HematicRiposteRules.MAX_TILT + 1);
		HematicRiposteEvents.arm(serverPlayer, side, tilt);
	}

	@Override
	public boolean usesDefaultActivationParticles() {
		return false;
	}
}
