package com.vincenthuto.hemomancy.common.manipulation.animus;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.common.entity.projectile.BloodNeedleEntity;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationRank;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationType;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationCastingRules;
import com.vincenthuto.hutoslib.math.Vector3;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class BloodNeedleManip extends BloodManipulation {
	private static final int CHARGE_TICKS = 20;

	public BloodNeedleManip(String name, double cost, double alignLevel, double xpCost, EnumManipulationType type,
			EnumManipulationRank rank, EnumBloodTendency tendency, EnumVeinSections section) {
		super(name, cost, alignLevel, xpCost, type, rank, tendency, section);
	}

	@Override
	public void getAction(Player player, Level world, ItemStack heldItemMainhand, BlockPos position) {
		getAction(player, world, heldItemMainhand, position, CHARGE_TICKS);
	}

	@Override
	public int getRequiredChargeTicks() {
		return CHARGE_TICKS;
	}

	@Override
	public void getAction(Player player, Level world, ItemStack heldItemMainhand, BlockPos position,
			float chargeTicks) {
		Vec3 vector3d = player.getLookAngle();
		Vector3 vector3f = new Vector3(vector3d);
		float strength = ManipulationCastingRules.chargeFraction(chargeTicks, CHARGE_TICKS);
		int count = Math.max(1, (int) Math.ceil((world.random.nextInt(11) + 10) * strength));
		for (int i = 0; i < count; i++) {
			BloodNeedleEntity needle = new BloodNeedleEntity(world, player, heldItemMainhand);
			needle.setDamageTendency(getTend());
			needle.setSecondaryDamageTendency(getSecondaryTend());
			needle.shoot(vector3f.x, vector3f.y, vector3f.z, world.random.nextInt(5) + 4,
					world.random.nextInt(20) - world.random.nextInt(20));
			world.addFreshEntity(needle);
		}
	}

}
