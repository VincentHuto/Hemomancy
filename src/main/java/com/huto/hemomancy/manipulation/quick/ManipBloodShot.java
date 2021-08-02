package com.huto.hemomancy.manipulation.quick;

import com.huto.hemomancy.capa.tendency.EnumBloodTendency;
import com.huto.hemomancy.capa.vascular.EnumVeinSections;
import com.huto.hemomancy.entity.projectile.EntityBloodShot;
import com.huto.hemomancy.manipulation.BloodManipulation;
import com.huto.hemomancy.manipulation.EnumManipulationRank;
import com.huto.hemomancy.manipulation.EnumManipulationType;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class ManipBloodShot extends BloodManipulation {

	public ManipBloodShot(String name, double cost, double alignLevel, EnumManipulationType type,
			EnumManipulationRank rank, EnumBloodTendency tendency, EnumVeinSections section) {
		super(name, cost, alignLevel, type, rank, tendency, section);
	}
	@Override
	public void getAction(Player player, Level world, ItemStack heldItemMainhand, BlockPos position) {
		Vec3 vector3d1 = player.getUpVector(1.0F);
		Quaternion quaternion = new Quaternion(new Vector3f(vector3d1), 0.0f, true);
		Vec3 vector3d = player.getLook(1.0F);
		Vector3f vector3f = new Vector3f(vector3d);
		vector3f.transform(quaternion);
		EntityBloodShot shot = new EntityBloodShot(world, player);
		shot.shoot((double) vector3f.getX(), (double) vector3f.getY(), (double) vector3f.getZ(), 4.5F, 1.0f);
		world.addEntity(shot);
	}

}
