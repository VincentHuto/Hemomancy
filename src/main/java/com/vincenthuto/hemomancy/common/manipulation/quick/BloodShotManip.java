package com.vincenthuto.hemomancy.common.manipulation.quick;

import com.vincenthuto.hemomancy.common.capability.player.kinship.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.common.entity.blood.BloodShotEntity;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationRank;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationType;
import com.vincenthuto.hutoslib.math.Quaternion;
import com.vincenthuto.hutoslib.math.Vector3;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class BloodShotManip extends BloodManipulation {

	public BloodShotManip(String name, double cost, double alignLevel,double xpCost, EnumManipulationType type,
			EnumManipulationRank rank, EnumBloodTendency tendency, EnumVeinSections section) {
		super(name, cost, alignLevel, xpCost, type, rank, tendency, section);
	}

	@Override
	public void getAction(Player player, Level world, ItemStack heldItemMainhand, BlockPos position) {
		Vec3 vector3d1 = player.getUpVector(1.0F);
		Quaternion quaternion = new Quaternion(new Vector3(vector3d1), 0.0f, true);
		Vec3 vector3d = player.getViewVector(1.0F);
		Vector3 vector3f = new Vector3(vector3d);
		vector3f.transform(quaternion);
		BloodShotEntity shot = new BloodShotEntity(world, player);
		shot.shoot(vector3f.x, vector3f.y, vector3f.z, 4.5F, 1.0f);
		world.addFreshEntity(shot);
	}

}
