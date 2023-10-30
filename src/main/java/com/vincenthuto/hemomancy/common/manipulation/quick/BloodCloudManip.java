package com.vincenthuto.hemomancy.common.manipulation.quick;

import com.vincenthuto.hemomancy.common.capability.player.kinship.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.common.entity.blood.BloodCloudCarrierEntity;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationRank;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationType;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class BloodCloudManip extends BloodManipulation {

	public BloodCloudManip(String name, double cost, double alignLevel,double xpCost, EnumManipulationType type,
			EnumManipulationRank rank, EnumBloodTendency tendency, EnumVeinSections section) {
		super(name, cost, alignLevel, xpCost, type, rank, tendency, section);
	}

	@Override
	public void getAction(Player player, Level world, ItemStack heldItemMainhand, BlockPos position) {
		if (!player.isSilent()) {
			world.levelEvent((Player) null, 1016, player.blockPosition(), 0);
		}
		Vec3 looking = player.getLookAngle();
		if (looking != null) {

			BloodCloudCarrierEntity fireballentity = new BloodCloudCarrierEntity(world, player, 0, 1, 0);
			fireballentity.setDeltaMovement(looking.x * 0.5, looking.y * 0.5, looking.z * 0.5);
			fireballentity.xPower = fireballentity.getDeltaMovement().x * 0.01D;
			fireballentity.yPower = fireballentity.getDeltaMovement().y * 0.01D;
			fireballentity.zPower = fireballentity.getDeltaMovement().z * 0.01D;
			fireballentity.setDeltaMovement(fireballentity.getDeltaMovement().scale(0.6));
			fireballentity.setPos(player.getX(), player.getY(0.75D), fireballentity.getZ());
			world.addFreshEntity(fireballentity);
		}
	}

}
