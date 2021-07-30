package com.huto.hemomancy.manipulation.quick;

import com.huto.hemomancy.capa.tendency.EnumBloodTendency;
import com.huto.hemomancy.capa.vascular.EnumVeinSections;
import com.huto.hemomancy.entity.projectile.EntityBloodCloudCarrier;
import com.huto.hemomancy.manipulation.BloodManipulation;
import com.huto.hemomancy.manipulation.EnumManipulationRank;
import com.huto.hemomancy.manipulation.EnumManipulationType;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.level.Level;

public class ManipBloodCloud extends BloodManipulation {

	public ManipBloodCloud(String name, double cost, double alignLevel, EnumManipulationType type,
			EnumManipulationRank rank, EnumBloodTendency tendency, EnumVeinSections section) {
		super(name, cost, alignLevel, type, rank, tendency, section);
	}

	@Override
	public void getAction(Player player, Level world, ItemStack heldItemMainhand, BlockPos position) {
		if (!player.isSilent()) {
			world.playEvent((PlayerEntity) null, 1016, player.getPosition(), 0);
		}
		Vector3d looking = player.getLookVec();
		if (looking != null) {

			EntityBloodCloudCarrier fireballentity = new EntityBloodCloudCarrier(world, player, 0, 1, 0);
			fireballentity.setMotion(looking.x * 0.5, looking.y * 0.5, looking.z * 0.5);
			fireballentity.accelerationX = fireballentity.getMotion().x * 0.01D;
			fireballentity.accelerationY = fireballentity.getMotion().y * 0.01D;
			fireballentity.accelerationZ = fireballentity.getMotion().z * 0.01D;
			fireballentity.setMotion(fireballentity.getMotion().scale(0.6));
			fireballentity.setPosition(player.getPosX(), player.getPosYHeight(0.75D), fireballentity.getPosZ());
			world.addEntity(fireballentity);
		}
	}

}
