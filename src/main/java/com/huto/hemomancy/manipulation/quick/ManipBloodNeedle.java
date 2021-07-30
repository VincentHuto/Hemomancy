package com.huto.hemomancy.manipulation.quick;

import com.huto.hemomancy.capa.tendency.EnumBloodTendency;
import com.huto.hemomancy.capa.vascular.EnumVeinSections;
import com.huto.hemomancy.entity.projectile.EntityBloodNeedle;
import com.huto.hemomancy.manipulation.BloodManipulation;
import com.huto.hemomancy.manipulation.EnumManipulationRank;
import com.huto.hemomancy.manipulation.EnumManipulationType;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.level.Level;

public class ManipBloodNeedle extends BloodManipulation {

	public ManipBloodNeedle(String name, double cost, double alignLevel, EnumManipulationType type,
			EnumManipulationRank rank, EnumBloodTendency tendency, EnumVeinSections section) {
		super(name, cost, alignLevel, type, rank, tendency, section);
	}

	@Override
	public void getAction(Player player, Level world, ItemStack heldItemMainhand, BlockPos position) {
		Vec3 vector3d = player.getLookVec();
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
