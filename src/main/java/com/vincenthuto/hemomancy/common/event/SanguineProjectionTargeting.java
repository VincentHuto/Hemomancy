package com.vincenthuto.hemomancy.common.event;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * One current-facing ray shared by projection gameplay and its particle
 * presentation. Avoids partial-tick yaw interpolation selecting an old or
 * wrapped direction at cardinal headings.
 */
public final class SanguineProjectionTargeting {
	public static final double PROJECTION_REACH = 5.5D;

	private SanguineProjectionTargeting() {
	}

	public static HitResult pick(Level level, Entity projector, double reach, boolean hitFluids) {
		Vec3 eye = projector.getEyePosition();
		Vec3 end = rayEnd(eye, projector.getLookAngle(), reach);
		return level.clip(new ClipContext(eye, end, ClipContext.Block.OUTLINE,
				hitFluids ? ClipContext.Fluid.ANY : ClipContext.Fluid.NONE, projector));
	}

	public static Vec3 rayEnd(Vec3 eye, Vec3 look, double reach) {
		if (look.lengthSqr() < 1.0E-12D) return eye;
		return eye.add(look.normalize().scale(Math.max(0.0D, reach)));
	}
}
