package com.huto.hemomancy.manipulation;

import com.huto.hemomancy.capa.tendency.EnumBloodTendency;
import com.huto.hemomancy.capa.vascular.EnumVeinSections;
import com.huto.hemomancy.entity.projectile.EntityBloodShot;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.server.ServerWorld;

public class BloodShotManipulation extends BloodManipulation {

	public BloodShotManipulation(String name, double cost, double alignLevel, EnumManipulationType type,
			EnumManipulationRank rank, EnumBloodTendency tendency, EnumVeinSections section) {
		super(name, cost, alignLevel, type, rank, tendency, section);
	}

	@Override
	public void performAction(PlayerEntity player, ServerWorld world, ItemStack heldItemMainhand, BlockPos position) {
		// super.performAction(player, world, heldItemMainhand, position);
		EntityBloodShot shot = new EntityBloodShot(world, player);
		Vector3d vector3d1 = player.getUpVector(1.0F);
		Quaternion quaternion = new Quaternion(new Vector3f(vector3d1), 0.0f, true);
		Vector3d vector3d = player.getLook(1.0F);
		Vector3f vector3f = new Vector3f(vector3d);
		vector3f.transform(quaternion);
		shot.shoot((double) vector3f.getX(), (double) vector3f.getY(), (double) vector3f.getZ(), 3.15F, 1.0f);
		world.addEntity(shot);
	}

}
