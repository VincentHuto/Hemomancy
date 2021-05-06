package com.huto.hemomancy.manipulation.quick;

import com.huto.hemomancy.capa.tendency.EnumBloodTendency;
import com.huto.hemomancy.capa.vascular.EnumVeinSections;
import com.huto.hemomancy.entity.projectile.EntityBloodNeedle;
import com.huto.hemomancy.manipulation.BloodManipulation;
import com.huto.hemomancy.manipulation.EnumManipulationRank;
import com.huto.hemomancy.manipulation.EnumManipulationType;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.World;

public class ManipBloodNeedle extends BloodManipulation {

	public ManipBloodNeedle(String name, double cost, double alignLevel, EnumManipulationType type,
			EnumManipulationRank rank, EnumBloodTendency tendency, EnumVeinSections section) {
		super(name, cost, alignLevel, type, rank, tendency, section);
	}

	@Override
	public void getAction(PlayerEntity player, World world, ItemStack heldItemMainhand, BlockPos position) {
		Vector3d vector3d = player.getLookVec();
		Vector3f vector3f = new Vector3f(vector3d);
		int randInt = world.rand.nextInt(11) + 10;
		EntityBloodNeedle[] needles = new EntityBloodNeedle[randInt];
		for (int i = 0; i < needles.length; i++) {
			needles[i] = new EntityBloodNeedle(world, player);
			needles[i].shoot((double) vector3f.getX(), (double) vector3f.getY(), (double) vector3f.getZ(),
					world.rand.nextInt(5) + 4, world.rand.nextInt(20) - world.rand.nextInt(20));
			world.addEntity(needles[i]);
		}

	}

}
