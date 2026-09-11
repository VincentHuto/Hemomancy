package com.vincenthuto.hemomancy.common.rite.harbinger;

import com.vincenthuto.hemomancy.common.rite.CardinalRiteAllyRole;
import com.vincenthuto.hemomancy.common.rite.sigil.CardinalRiteSigilPlacementRules;
import com.vincenthuto.hemomancy.common.rite.sigil.IchorianSigilDefinition;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Set;
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

	/** Keeps the helper body and its hold radius away from targets and their approach lanes. */
	public static Map<CardinalRiteAllyRole, BlockPos> roleMarkers(Set<BlockPos> targets) {
		Set<BlockPos> occupied = new HashSet<>();
		occupied.add(BlockPos.ZERO);
		for (BlockPos target : targets) {
			int steps = Math.max(Math.abs(target.getX()), Math.abs(target.getZ()));
			for (int step = 1; step <= steps; step++) {
				occupied.add(new BlockPos(Math.round((float) target.getX() * step / steps), 0,
						Math.round((float) target.getZ() * step / steps)));
			}
		}
		Map<CardinalRiteAllyRole, BlockPos> result = new EnumMap<>(CardinalRiteAllyRole.class);
		for (CardinalRiteAllyRole role : CardinalRiteAllyRole.values()) {
			BlockPos preferred = switch (role) {
				case ANCHOR -> new BlockPos(-3, 0, -3);
				case ATTENDANT -> new BlockPos(3, 0, -3);
				case WARDEN -> new BlockPos(3, 0, 3);
			};
			BlockPos resolved = CardinalRiteSigilPlacementRules.resolveNearestPlacement(preferred,
					List.of(new IchorianSigilDefinition.Node(0, 0)), occupied);
			result.put(role, resolved.above());
			// Reserve room for both helpers' hold radii, rather than stacking different roles.
			for (int x = -1; x <= 1; x++) {
				for (int z = -1; z <= 1; z++) occupied.add(resolved.offset(x, 0, z));
			}
		}
		return result;
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
