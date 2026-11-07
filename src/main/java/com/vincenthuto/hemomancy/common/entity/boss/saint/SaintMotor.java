package com.vincenthuto.hemomancy.common.entity.boss.saint;

import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

/**
 * The one way a kit Saint's body moves: a walk, a navigator path (no goals), a glide, a lunge, a turn and a stop.
 * Every target point is clamped to the arena's leash, and the motor pulls the body back inside the chamber box after
 * each tick's movement.
 */
public final class SaintMotor {
	private final SaintTrialEntity saint;
	private Vec3 lungeStep = Vec3.ZERO;
	private int lungeTicks;

	SaintMotor(SaintTrialEntity saint) {
		this.saint = saint;
	}

	public void walk(Vec3 direction, double speed) {
		Vec3 flat = new Vec3(direction.x, 0.0D, direction.z);
		if (flat.lengthSqr() < 1.0E-8D) {
			stop();
			return;
		}
		Vec3 point = leash(saint.position().add(flat.normalize().scale(2.0D)));
		saint.getNavigation().stop();
		saint.getMoveControl().setWantedPosition(point.x, point.y, point.z, speed);
	}

	/** The vanilla navigator toward a point, no goals. */
	public void pathTo(Vec3 point, double speed) {
		Vec3 leashed = leash(point);
		saint.getNavigation().moveTo(leashed.x, leashed.y, leashed.z, speed);
	}

	/** Straight through the air toward the point, holding {@code hover} blocks over it. */
	public void glide(Vec3 point, double speed, double hover) {
		Vec3 leashed = leash(point.add(0.0D, hover, 0.0D));
		Vec3 delta = leashed.subtract(saint.position());
		double length = delta.length();
		saint.getNavigation().stop();
		if (length < 1.0E-4D) {
			saint.setDeltaMovement(Vec3.ZERO);
			return;
		}
		saint.setDeltaMovement(delta.scale(Math.min(speed, length) / length));
	}

	/** Covers {@code distance} along {@code direction} over {@code ticks}, whatever the move control says. */
	public void lunge(Vec3 direction, double distance, int ticks) {
		Vec3 flat = new Vec3(direction.x, 0.0D, direction.z);
		if (flat.lengthSqr() < 1.0E-8D || ticks <= 0) return;
		lungeStep = flat.normalize().scale(distance / ticks);
		lungeTicks = ticks;
		saint.getNavigation().stop();
	}

	public boolean lunging() {
		return lungeTicks > 0;
	}

	/** Turns body, head and yaw toward the point by at most {@code maxStep} degrees. */
	public void face(Vec3 point, float maxStep) {
		double dx = point.x - saint.getX(), dz = point.z - saint.getZ();
		if (dx * dx + dz * dz < 1.0E-6D) return;
		float wanted = (float) (Mth.atan2(dz, dx) * Mth.RAD_TO_DEG) - 90.0F;
		float yaw = Mth.approachDegrees(saint.getYRot(), wanted, maxStep);
		saint.setYRot(yaw);
		saint.setYBodyRot(yaw);
		saint.setYHeadRot(yaw);
	}

	public void stop() {
		saint.getNavigation().stop();
		saint.getMoveControl().setWantedPosition(saint.getX(), saint.getY(), saint.getZ(), 0.0D);
		lungeTicks = 0;
		Vec3 motion = saint.getDeltaMovement();
		saint.setDeltaMovement(0.0D, motion.y, 0.0D);
	}

	/** The kit's per-tick step: the lunge, then the leash. */
	void tick() {
		if (lungeTicks > 0) {
			lungeTicks--;
			Vec3 motion = saint.getDeltaMovement();
			saint.setDeltaMovement(lungeStep.x, motion.y, lungeStep.z);
		}
		SaintArena arena = saint.arena();
		if (arena != null && !arena.contains(saint.position())) {
			Vec3 inside = arena.clamp(saint.position());
			saint.teleportTo(inside.x, inside.y, inside.z);
		}
	}

	private Vec3 leash(Vec3 point) {
		SaintArena arena = saint.arena();
		return arena == null ? point : arena.clamp(point);
	}
}
