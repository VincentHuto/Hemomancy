package com.vincenthuto.hemomancy.common.manipulation.animus;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.common.entity.projectile.BloodShotEntity;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationRank;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationType;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationCombatHelper;
import com.vincenthuto.hutoslib.math.Quaternion;
import com.vincenthuto.hutoslib.math.Vector3;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class BloodShotManip extends BloodManipulation {
	private final Mode mode;

	public BloodShotManip(String name, double cost, double alignLevel,double xpCost, EnumManipulationType type,
			EnumManipulationRank rank, EnumBloodTendency tendency, EnumVeinSections section) {
		this(name, cost, alignLevel, xpCost, type, rank, tendency, section, Mode.BASELINE);
	}

	public BloodShotManip(String name, double cost, double alignLevel, double xpCost, EnumManipulationType type,
			EnumManipulationRank rank, EnumBloodTendency tendency, EnumVeinSections section, Mode mode) {
		super(name, cost, alignLevel, xpCost, type, rank, tendency, section);
		this.mode = mode;
	}

	@Override
	public void getAction(Player player, Level world, ItemStack heldItemMainhand, BlockPos position) {
		getAction(player, world, heldItemMainhand, position, getRequiredChargeTicks());
	}

	@Override
	public int getRequiredChargeTicks() {
		return mode == Mode.MORTAR ? 40 : super.getRequiredChargeTicks();
	}

	@Override
	public void getAction(Player player, Level world, ItemStack heldItemMainhand, BlockPos position,
			float chargeTicks) {
		if (mode == Mode.HALO) {
			for (int i = 0; i < 5; i++) {
				BloodShotEntity shot = shot(world, player, heldItemMainhand);
				shot.configureOrbit(player, i);
				world.addFreshEntity(shot);
			}
			return;
		}
		Vec3 vector3d1 = player.getUpVector(1.0F);
		Quaternion quaternion = new Quaternion(new Vector3(vector3d1), 0.0f, true);
		Vec3 vector3d = player.getViewVector(1.0F);
		Vector3 vector3f = new Vector3(vector3d);
		vector3f.transform(quaternion);
		BloodShotEntity shot = shot(world, player, heldItemMainhand);
		if (mode == Mode.GUIDED) {
			shot.setHomingTarget(ManipulationCombatHelper.aimedTarget(player, world, 24.0D, 0.5D), 60);
		}
		if (mode == Mode.MORTAR) {
			shot.setMortar(true);
			shot.shoot(vector3f.x, vector3f.y + 0.35D, vector3f.z, 2.2F, 0.5F);
		} else {
			shot.shoot(vector3f.x, vector3f.y, vector3f.z, 4.5F, 1.0F);
		}
		world.addFreshEntity(shot);
	}

	@Override
	protected boolean canPerformAction(Player player, ItemStack heldItemMainhand, float chargeTicks) {
		if (mode == Mode.HALO && player.level().getEntitiesOfClass(BloodShotEntity.class,
				player.getBoundingBox().inflate(4), shot -> shot.isOrbitingFor(player.getUUID())).size() > 0) {
			player.displayClientMessage(Component.literal("A sanguine halo already surrounds you."), true);
			return false;
		}
		return super.canPerformAction(player, heldItemMainhand, chargeTicks);
	}

	private BloodShotEntity shot(Level world, Player player, ItemStack heldItemMainhand) {
		BloodShotEntity shot = new BloodShotEntity(world, player, heldItemMainhand);
		shot.setDamageTendency(getTend());
		shot.setSecondaryDamageTendency(getSecondaryTend());
		return shot;
	}

	public enum Mode {
		BASELINE,
		GUIDED,
		MORTAR,
		HALO
	}

}
