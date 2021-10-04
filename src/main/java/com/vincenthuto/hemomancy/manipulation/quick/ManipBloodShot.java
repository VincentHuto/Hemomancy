package com.vincenthuto.hemomancy.manipulation.quick;

import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import com.vincenthuto.hemomancy.capa.player.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.capa.player.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.entity.blood.EntityBloodShot;
import com.vincenthuto.hemomancy.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.manipulation.EnumManipulationRank;
import com.vincenthuto.hemomancy.manipulation.EnumManipulationType;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class ManipBloodShot extends BloodManipulation {

	public ManipBloodShot(String name, double cost, double alignLevel,double xpCost, EnumManipulationType type,
			EnumManipulationRank rank, EnumBloodTendency tendency, EnumVeinSections section) {
		super(name, cost, alignLevel, xpCost, type, rank, tendency, section);
	}

	@Override
	public void getAction(Player player, Level world, ItemStack heldItemMainhand, BlockPos position) {
		Vec3 vector3d1 = player.getUpVector(1.0F);
		Quaternion quaternion = new Quaternion(new Vector3f(vector3d1), 0.0f, true);
		Vec3 vector3d = player.getViewVector(1.0F);
		Vector3f vector3f = new Vector3f(vector3d);
		vector3f.transform(quaternion);
		EntityBloodShot shot = new EntityBloodShot(world, player);
		shot.shoot(vector3f.x(), vector3f.y(), vector3f.z(), 4.5F, 1.0f);
		world.addFreshEntity(shot);
	}

}
