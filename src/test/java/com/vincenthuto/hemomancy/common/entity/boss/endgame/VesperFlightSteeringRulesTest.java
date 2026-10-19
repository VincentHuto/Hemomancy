package com.vincenthuto.hemomancy.common.entity.boss.endgame;

import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

final class VesperFlightSteeringRulesTest {
	private static final double MAX_SPEED = 0.6D;
	private static final double ACCEL = 0.05D;
	private static final float TURN = 6.0F;

	@Test
	void speedNeverExceedsMax() {
		Vec3 pos = Vec3.ZERO;
		Vec3 vel = Vec3.ZERO;
		float yaw = 0.0F;
		for (int i = 0; i < 200; i++) {
			VesperFlightSteeringRules.Steer steer = VesperFlightSteeringRules.step(pos, vel, yaw,
					new Vec3(40.0D, -10.0D, 25.0D), MAX_SPEED, ACCEL, TURN);
			assertTrue(steer.velocity().length() <= MAX_SPEED + 1.0E-6D);
			pos = pos.add(steer.velocity());
			vel = steer.velocity();
			yaw = steer.yaw();
		}
	}

	@Test
	void yawChangesAtMostMaxTurnPerStep() {
		VesperFlightSteeringRules.Steer steer = VesperFlightSteeringRules.step(Vec3.ZERO, Vec3.ZERO, 0.0F,
				new Vec3(-10.0D, 0.0D, -10.0D), MAX_SPEED, ACCEL, TURN);
		assertTrue(Math.abs(Mth.wrapDegrees(steer.yaw())) <= TURN + 1.0E-4F);
	}

	@Test
	void bankSignFollowsTurnDirectionAndIsClamped() {
		// Facing +z (yaw 0); a target to the -x side needs yaw to grow.
		VesperFlightSteeringRules.Steer one = VesperFlightSteeringRules.step(Vec3.ZERO, new Vec3(0, 0, 0.4D), 0.0F,
				new Vec3(-20.0D, 0.0D, 2.0D), MAX_SPEED, ACCEL, TURN);
		VesperFlightSteeringRules.Steer other = VesperFlightSteeringRules.step(Vec3.ZERO, new Vec3(0, 0, 0.4D), 0.0F,
				new Vec3(20.0D, 0.0D, 2.0D), MAX_SPEED, ACCEL, TURN);
		assertTrue(one.bank() * other.bank() < 0.0F, "turning opposite ways banks opposite ways");
		assertTrue(Math.abs(one.bank()) <= VesperFlightSteeringRules.BANK_MAX);
		assertTrue(Math.signum(one.bank()) == Math.signum(Mth.wrapDegrees(one.yaw())));
	}

	@Test
	void pitchNegativeWhenDescending() {
		VesperFlightSteeringRules.Steer steer = VesperFlightSteeringRules.step(Vec3.ZERO, new Vec3(0, -0.4D, 0.2D),
				0.0F, new Vec3(0.0D, -20.0D, 4.0D), MAX_SPEED, ACCEL, TURN);
		assertTrue(steer.pitch() < 0.0F);
		assertTrue(steer.pitch() >= -VesperFlightSteeringRules.PITCH_MAX);
	}

	@Test
	void velocityEasesTowardDesiredInsteadOfReversing() {
		Vec3 vel = new Vec3(0.0D, 0.0D, 0.5D);
		VesperFlightSteeringRules.Steer steer = VesperFlightSteeringRules.step(Vec3.ZERO, vel, 0.0F,
				new Vec3(0.0D, 0.0D, -30.0D), MAX_SPEED, ACCEL, TURN);
		assertTrue(steer.velocity().dot(vel) > 0.0D, "a target behind is reached by turning, not by snapping backwards");
	}

	@Test
	void circlePointSitsOnTheRadiusAtTheRequestedAltitude() {
		Vec3 point = VesperFlightSteeringRules.circlePoint(new Vec3(10.0D, 64.0D, -4.0D), 8.0D, Math.PI / 2.0D, 71.0D);
		assertEquals(10.0D, point.x, 1.0E-6D);
		assertEquals(71.0D, point.y, 1.0E-6D);
		assertEquals(4.0D, point.z, 1.0E-6D);
	}
}
