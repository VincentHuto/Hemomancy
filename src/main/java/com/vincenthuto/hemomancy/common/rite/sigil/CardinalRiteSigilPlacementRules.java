package com.vincenthuto.hemomancy.common.rite.sigil;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Keeps an entire sigil footprint clear of boundary anchors and other sigils.
 */
public final class CardinalRiteSigilPlacementRules {
	public static final double MINIMUM_NODE_CLEARANCE = 2.0D;
	private static final int MAX_OUTWARD_SHIFTS = 64;

	private CardinalRiteSigilPlacementRules() {
	}

	public static BlockPos resolveSupportPlacement(BlockPos requested,
			List<IchorianSigilDefinition.Node> nodes, Set<BlockPos> occupied) {
		int outwardX = Integer.signum(requested.getX());
		int outwardZ = Integer.signum(requested.getZ());
		if (outwardX == 0 && outwardZ == 0) outwardX = 1;

		BlockPos candidate = requested;
		for (int shift = 0; shift <= MAX_OUTWARD_SHIFTS; shift++) {
			if (clearOfOccupiedTargets(footprint(candidate, nodes), occupied)) return candidate;
			candidate = candidate.offset(outwardX, 0, outwardZ);
		}
		throw new IllegalStateException("Unable to place ichorian sigil clear of occupied anchors");
	}

	public static BlockPos resolveNearestPlacement(BlockPos requested,
			List<IchorianSigilDefinition.Node> nodes, Set<BlockPos> occupied) {
		for (int radius = 0; radius <= MAX_OUTWARD_SHIFTS; radius++) {
			BlockPos best = null;
			int bestDistance = Integer.MAX_VALUE;
			for (int x = -radius; x <= radius; x++) {
				for (int z = -radius; z <= radius; z++) {
					if (Math.max(Math.abs(x), Math.abs(z)) != radius) continue;
					BlockPos candidate = requested.offset(x, 0, z);
					if (!clearOfOccupiedTargets(footprint(candidate, nodes), occupied)) continue;
					int distance = x * x + z * z;
					if (distance < bestDistance) {
						best = candidate;
						bestDistance = distance;
					}
				}
			}
			if (best != null) return best;
		}
		throw new IllegalStateException("Unable to place ichorian sigil clear of ritual targets");
	}

	public static Set<BlockPos> resolvedFootprint(BlockPos requested,
			List<IchorianSigilDefinition.Node> nodes, Set<BlockPos> occupied) {
		return footprint(resolveNearestPlacement(requested, nodes, occupied), nodes);
	}

	public static Set<BlockPos> footprint(BlockPos placement,
			List<IchorianSigilDefinition.Node> nodes) {
		Set<BlockPos> positions = new LinkedHashSet<>();
		for (IchorianSigilDefinition.Node node : nodes) {
			positions.add(placement.offset(
					(int) Math.round(node.x()), 0, (int) Math.round(node.z())));
		}
		return positions;
	}

	public static int matchingSigilIndex(ResourceLocation target, List<ResourceLocation> candidates) {
		for (int i = 0; i < candidates.size(); i++) {
			if (target.equals(candidates.get(i))) return i;
		}
		return -1;
	}

	private static boolean clearOfOccupiedTargets(Set<BlockPos> footprint, Set<BlockPos> occupied) {
		double minimumDistanceSqr = MINIMUM_NODE_CLEARANCE * MINIMUM_NODE_CLEARANCE;
		for (BlockPos position : footprint) {
			for (BlockPos occupiedPosition : occupied) {
				double x = position.getX() - occupiedPosition.getX();
				double z = position.getZ() - occupiedPosition.getZ();
				if (x * x + z * z < minimumDistanceSqr) return false;
			}
		}
		return true;
	}
}
