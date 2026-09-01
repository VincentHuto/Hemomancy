package com.vincenthuto.hemomancy.common.rite.harbinger;

import com.vincenthuto.hemomancy.common.rite.CardinalRiteAllyRole;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;

public final class CardinalRiteNpcStationRules {
	public static final double HOLD_RADIUS = 1.25D;
	public static final double PARTICIPATION_RADIUS = 3.0D;
	public static final double RECALL_RADIUS = 8.0D;
	public static final double FALL_RECALL_DEPTH = 2.0D;
	private static final double MAX_STATION_HEIGHT_DIFFERENCE = 2.0D;
	private static final double MAX_APPROACH_HEIGHT_DIFFERENCE = 6.0D;

	private CardinalRiteNpcStationRules() {
	}

	public static boolean stationSafe(boolean loaded, boolean sturdySupport, boolean collisionFree) {
		return loaded && sturdySupport && collisionFree;
	}

	public static List<UUID> assignedNpcAllies(Map<UUID, CardinalRiteAllyRole> assignments,
			Predicate<UUID> isNpcAlly) {
		return assignments.keySet().stream().filter(isNpcAlly).toList();
	}

	public static Vec3 faneReturnPosition(BlockPos recallPoint) {
		return new Vec3(recallPoint.getX() + 0.5D, recallPoint.getY() + 1.0D,
				recallPoint.getZ() + 0.5D);
	}

	public static boolean participates(Vec3 position, BlockPos station, boolean safeStation) {
		if (!safeStation || position == null || station == null) return false;
		return horizontalDistanceSqr(position, station) <= PARTICIPATION_RADIUS * PARTICIPATION_RADIUS
				&& Math.abs(position.y - station.getY()) <= MAX_STATION_HEIGHT_DIFFERENCE;
	}

	public static Correction correction(Vec3 position, BlockPos station, boolean safeStation) {
		if (!safeStation || position == null || station == null) return Correction.UNAVAILABLE;
		double horizontalDistanceSqr = horizontalDistanceSqr(position, station);
		double verticalDifference = position.y - station.getY();
		if (horizontalDistanceSqr > RECALL_RADIUS * RECALL_RADIUS
				|| verticalDifference < -FALL_RECALL_DEPTH
				|| verticalDifference > MAX_APPROACH_HEIGHT_DIFFERENCE) {
			return Correction.RECALL;
		}
		if (horizontalDistanceSqr > HOLD_RADIUS * HOLD_RADIUS
				|| Math.abs(verticalDifference) > 1.0D) {
			return Correction.APPROACH;
		}
		return Correction.HOLD;
	}

	private static double horizontalDistanceSqr(Vec3 position, BlockPos station) {
		double dx = position.x - (station.getX() + 0.5D);
		double dz = position.z - (station.getZ() + 0.5D);
		return dx * dx + dz * dz;
	}

	public enum Correction {
		HOLD,
		APPROACH,
		RECALL,
		UNAVAILABLE
	}
}
