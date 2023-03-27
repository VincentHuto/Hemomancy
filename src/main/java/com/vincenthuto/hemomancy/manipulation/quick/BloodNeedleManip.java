package com.vincenthuto.hemomancy.manipulation.quick;

import com.vincenthuto.hemomancy.capa.player.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.capa.player.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.entity.blood.BloodNeedleEntity;
import com.vincenthuto.hemomancy.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.manipulation.EnumManipulationRank;
import com.vincenthuto.hemomancy.manipulation.EnumManipulationType;
import com.vincenthuto.hutoslib.math.Vector3;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class BloodNeedleManip extends BloodManipulation {

	public BloodNeedleManip(String name, double cost, double alignLevel, double xpCost, EnumManipulationType type,
			EnumManipulationRank rank, EnumBloodTendency tendency, EnumVeinSections section) {
		super(name, cost, alignLevel, xpCost, type, rank, tendency, section);
	}

	@Override
	public void getAction(Player player, Level world, ItemStack heldItemMainhand, BlockPos position) {
		Vec3 vector3d = player.getLookAngle();
		Vector3 vector3f = new Vector3(vector3d);
		int randInt = world.random.nextInt(11) + 10;
		BloodNeedleEntity[] needles = new BloodNeedleEntity[randInt];
		for (int i = 0; i < needles.length; i++) {
			needles[i] = new BloodNeedleEntity(world, player);
			needles[i].shoot(vector3f.x, vector3f.y, vector3f.z, world.random.nextInt(5) + 4,
					world.random.nextInt(20) - world.random.nextInt(20));
			world.addFreshEntity(needles[i]);
		}

	}

}
