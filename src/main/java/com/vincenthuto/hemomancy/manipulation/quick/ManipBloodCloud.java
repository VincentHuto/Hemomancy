package com.vincenthuto.hemomancy.manipulation.quick;

import com.vincenthuto.hemomancy.capa.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.capa.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.entity.blood.EntityBloodCloudCarrier;
import com.vincenthuto.hemomancy.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.manipulation.EnumManipulationRank;
import com.vincenthuto.hemomancy.manipulation.EnumManipulationType;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class ManipBloodCloud extends BloodManipulation {

	public ManipBloodCloud(String name, double cost, double alignLevel, EnumManipulationType type,
			EnumManipulationRank rank, EnumBloodTendency tendency, EnumVeinSections section) {
		super(name, cost, alignLevel, type, rank, tendency, section);
	}

	@Override
	public void getAction(Player player, Level world, ItemStack heldItemMainhand, BlockPos position) {
		if (!player.isSilent()) {
			world.levelEvent((Player) null, 1016, player.blockPosition(), 0);
		}
		Vec3 looking = player.getLookAngle();
		if (looking != null) {

			EntityBloodCloudCarrier fireballentity = new EntityBloodCloudCarrier(world, player, 0, 1, 0);
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
