package com.vincenthuto.hemomancy.common.entity.boss.endgame;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;

/** Walks the mount toward the controller's wanted point with a capped turn rate, slowing while it turns. */
final class VesperMountMoveControl extends MoveControl {
	static final float GROUND_TURN_DEGREES = 9.0F;

	VesperMountMoveControl(Mob mob) {
		super(mob);
	}

	/** Sidesteps relative to the current facing, which the controller keeps on the target. */
	void strafe(float forward, float right, double speed) {
		strafe(forward, right);
		speedModifier = speed;
	}

	@Override
	public void tick() {
		if (operation == Operation.STRAFE) {
			super.tick();
			return;
		}
		if (operation != Operation.MOVE_TO) {
			mob.setZza(0.0F);
			mob.setXxa(0.0F);
			return;
		}
		operation = Operation.WAIT;
		double dx = wantedX - mob.getX();
		double dz = wantedZ - mob.getZ();
		if (dx * dx + dz * dz < 0.25D) {
			mob.setZza(0.0F);
			return;
		}
		float desired = (float) (Mth.atan2(dz, dx) * Mth.RAD_TO_DEG) - 90.0F;
		float yaw = VesperMountControllerRules.turnToward(mob.getYRot(), desired, GROUND_TURN_DEGREES);
		mob.setYRot(yaw);
		mob.setYBodyRot(yaw);
		float alignment = Mth.cos(Mth.wrapDegrees(desired - yaw) * Mth.DEG_TO_RAD);
		float speed = (float) (speedModifier * mob.getAttributeValue(Attributes.MOVEMENT_SPEED));
		mob.setSpeed(speed * Mth.clamp(alignment, 0.2F, 1.0F));
	}
}
