package com.vincenthuto.hemomancy.common.entity.mob.animal;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

public final class LuminalCicadaRules {
	public static final double FLASH_RANGE_SQUARED = 16.0D;
	public static final int FLASH_COOLDOWN_TICKS = 100;

	private LuminalCicadaRules() {
	}

	public static boolean canCling(boolean treeSurface, boolean openSpace) {
		return treeSurface && openSpace;
	}

	public static boolean shouldFlash(double playerDistanceSquared, int cooldownTicks) {
		return cooldownTicks <= 0 && playerDistanceSquared <= FLASH_RANGE_SQUARED;
	}

	public static boolean canNaturalSpawn(boolean nearbyTree, boolean openAir) {
		return nearbyTree && openAir;
	}

	public static float clingBodyYaw(Direction face) {
		return face.toYRot();
	}

	public static float clingTiltDegrees(Direction face) {
		return face.getAxis().isHorizontal() ? 90.0F : 0.0F;
	}

	public static Vec3 clingAnchor(BlockPos log, Direction face, double halfWidth) {
		double surfaceOffset = 0.5D + halfWidth + 0.01D;
		return Vec3.atCenterOf(log).add(face.getStepX() * surfaceOffset, 0.0D,
				face.getStepZ() * surfaceOffset);
	}

	public static boolean shouldPickIdleDestination(boolean hasTreeTarget, boolean hasIdleTarget,
			double idleDistanceSquared, int idleTicks) {
		return !hasTreeTarget && (!hasIdleTarget || idleDistanceSquared < 0.36D || idleTicks <= 0);
	}

	public static float legRoll(boolean clinging, boolean right) {
		float leftRoll = clinging ? 0.35F : -0.75F;
		return right ? -leftRoll : leftRoll;
	}

	public static float tailGlowScale(float ageInTicks) {
		return 1.0F + (float) Math.sin(ageInTicks * 0.23F) * 0.05F
				+ (float) Math.sin(ageInTicks * 0.071F + 1.7F) * 0.025F;
	}

	public static float abdomenPitch(boolean clinging, float ageInTicks) {
		return clinging ? 0.0F : -0.18F + (float) Math.sin(ageInTicks * 0.18F) * 0.025F;
	}
}
