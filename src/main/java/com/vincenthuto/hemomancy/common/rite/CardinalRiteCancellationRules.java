package com.vincenthuto.hemomancy.common.rite;

import java.util.UUID;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** Pure validation and timing rules for deliberately absorbing an active rite. */
public final class CardinalRiteCancellationRules {
	public static final int DAEMON_ABSORPTION_TICKS = 50;
	public static final int STAFF_DISSOLUTION_TICKS = 30;
	public static final int TOTAL_TICKS = DAEMON_ABSORPTION_TICKS + STAFF_DISSOLUTION_TICKS;
	private static final double STAFF_TARGET_HORIZONTAL_MARGIN = 0.35D;
	private static final double STAFF_TARGET_MIN_Y = 0.35D;
	private static final double STAFF_TARGET_MAX_Y = 4.75D;

	private CardinalRiteCancellationRules() {
	}

	public static boolean canChannel(UUID caster, UUID owner, BlockPos lookedAt, BlockPos center,
			boolean hasEscrowedStaff, boolean terminal) {
		return caster != null
				&& caster.equals(owner)
				&& lookedAt != null
				&& lookedAt.equals(center)
				&& hasEscrowedStaff
				&& !terminal;
	}

	public static boolean aimsAtPlantedStaff(Vec3 eyePosition, Vec3 viewVector, double range,
			BlockPos center) {
		if (eyePosition == null || viewVector == null || center == null || range <= 0.0D
				|| viewVector.lengthSqr() < 1.0E-7D) {
			return false;
		}
		Vec3 rayEnd = eyePosition.add(viewVector.normalize().scale(range));
		AABB renderedStaffBounds = new AABB(
				center.getX() - STAFF_TARGET_HORIZONTAL_MARGIN,
				center.getY() + STAFF_TARGET_MIN_Y,
				center.getZ() - STAFF_TARGET_HORIZONTAL_MARGIN,
				center.getX() + 1.0D + STAFF_TARGET_HORIZONTAL_MARGIN,
				center.getY() + STAFF_TARGET_MAX_Y,
				center.getZ() + 1.0D + STAFF_TARGET_HORIZONTAL_MARGIN);
		return renderedStaffBounds.contains(eyePosition)
				|| renderedStaffBounds.clip(eyePosition, rayEnd).isPresent();
	}

	public static int nextChannelTicks(int currentTicks, boolean channeling) {
		return channeling ? Math.min(TOTAL_TICKS, Math.max(0, currentTicks) + 1) : 0;
	}

	public static double daemonAbsorptionProgress(int channelTicks) {
		return clamp(channelTicks / (double) DAEMON_ABSORPTION_TICKS);
	}

	public static double staffDissolutionProgress(int channelTicks) {
		return clamp((channelTicks - DAEMON_ABSORPTION_TICKS)
				/ (double) STAFF_DISSOLUTION_TICKS);
	}

	public static boolean isComplete(int channelTicks) {
		return channelTicks >= TOTAL_TICKS;
	}

	public static boolean canAnimateDaemon(boolean daemonAlreadyExists) {
		return daemonAlreadyExists;
	}

	private static double clamp(double value) {
		return Math.max(0.0D, Math.min(1.0D, value));
	}
}
