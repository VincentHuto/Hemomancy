package com.vincenthuto.hemomancy.entity.drudge;

import java.util.EnumSet;
import java.util.List;

import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public class DrudgeCollectItemGoal extends Goal {
	private final EntityDrudge creature;
	private ItemEntity targetEntity;
	private double movePosX;
	private double movePosY;
	private double movePosZ;
	private final double speed;
	private final float maxTargetDistance;

	public DrudgeCollectItemGoal(EntityDrudge creature, double speedIn, float targetMaxDistance) {
		this.creature = creature;
		this.speed = speedIn;
		this.maxTargetDistance = targetMaxDistance;
		this.setFlags(EnumSet.of(Goal.Flag.MOVE));
	}

	@Override
	public boolean canUse() {
		if (creature.getRoleTitle() == EnumDrudgeRoles.COLLECTOR) {
			if (creature.getDrudgeInventory().getItem(0) == ItemStack.EMPTY) {
				List<ItemEntity> list = creature.level.getEntitiesOfClass(ItemEntity.class,
						creature.getBoundingBox().inflate(maxTargetDistance));
				for (ItemEntity ent : list) {
					this.targetEntity = ent;
					if (this.targetEntity == null) {
						return false;
					} else if (this.targetEntity.distanceToSqr(
							this.creature) > (double) (this.maxTargetDistance * this.maxTargetDistance)) {
						return false;
					} else {
						Vec3 vector3d = this.targetEntity.position();
						if (vector3d == null) {
							return false;
						} else {
							this.movePosX = vector3d.x + 0.5;
							this.movePosY = vector3d.y;
							this.movePosZ = vector3d.z + 0.5;
							return true;
						}
					}
				}
				return false;
			}
		}
		return false;
	}

	@Override
	public boolean canContinueToUse() {
		return !this.creature.getNavigation().isDone() && this.targetEntity.isAlive() && this.targetEntity
				.distanceToSqr(this.creature) < (double) (this.maxTargetDistance * this.maxTargetDistance);
	}

	@Override

	public void stop() {
		this.targetEntity = null;
	}

	@Override
	public void start() {
		this.creature.getNavigation().moveTo(
				this.creature.getNavigation().createPath(this.movePosX, this.movePosY, this.movePosZ, 0), this.speed);
	}
}