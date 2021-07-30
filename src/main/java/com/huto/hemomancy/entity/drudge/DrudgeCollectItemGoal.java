package com.huto.hemomancy.entity.drudge;

import java.util.EnumSet;
import java.util.List;

import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.vector.Vector3d;

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
		this.setMutexFlags(EnumSet.of(Goal.Flag.MOVE));
	}

	@Override
	public boolean shouldExecute() {
		if (creature.getRoleTitle() == EnumDrudgeRoles.COLLECTOR) {
			if (creature.getDrudgeInventory().getStackInSlot(0) == ItemStack.EMPTY) {
				List<ItemEntity> list = creature.world.getEntitiesWithinAABB(ItemEntity.class,
						creature.getBoundingBox().grow(maxTargetDistance));
				for (ItemEntity ent : list) {
					this.targetEntity = ent;
					if (this.targetEntity == null) {
						return false;
					} else if (this.targetEntity.getDistanceSq(
							this.creature) > (double) (this.maxTargetDistance * this.maxTargetDistance)) {
						return false;
					} else {
						Vector3d vector3d = this.targetEntity.getPositionVec();
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
	public boolean shouldContinueExecuting() {
		return !this.creature.getNavigator().noPath() && this.targetEntity.isAlive() && this.targetEntity
				.getDistanceSq(this.creature) < (double) (this.maxTargetDistance * this.maxTargetDistance);
	}

	@Override

	public void resetTask() {
		this.targetEntity = null;
	}

	@Override
	public void startExecuting() {
		this.creature.getNavigator().setPath(
				this.creature.getNavigator().pathfind(this.movePosX, this.movePosY, this.movePosZ, 0), this.speed);
	}
}