package com.vincenthuto.hemomancy.common.event.worldevent;

import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.List;

public record FaneFootprint(BlockPos heart, List<BlockPos> stakes, BlockPos legacyCenter) {
	public static final double HEART_RADIUS = 40.0D;
	public static final double STAKE_RADIUS = 32.0D;
	public static final int BASE_STAKE_BUDGET = 3;
	public static final int MAX_STAKE_BUDGET = 12;

	public FaneFootprint {
		stakes = List.copyOf(stakes);
	}

	public boolean contains(BlockPos pos) {
		if (heart != null && within(pos, heart, HEART_RADIUS)) {
			return true;
		}
		for (BlockPos stake : stakes) {
			if (within(pos, stake, STAKE_RADIUS)) {
				return true;
			}
		}
		return heart == null && legacyCenter != null && within(pos, legacyCenter, HEART_RADIUS);
	}

	public double effectStrength(BlockPos pos) {
		BlockPos center = heart != null ? heart : legacyCenter;
		if (center == null || !contains(pos)) {
			return 0.0D;
		}
		double dist = Math.sqrt(distanceSqr(pos, center));
		double maxDistance = maxDistanceFromHeart();
		if (maxDistance <= 0.0D) {
			return 1.0D;
		}
		double normalized = Math.max(0.0D, Math.min(1.0D, dist / maxDistance));
		return 1.0D - normalized * 0.65D;
	}

	public boolean canAddStake(BlockPos pos, int budget) {
		if (heart == null || stakes.size() >= Math.min(budget, MAX_STAKE_BUDGET) || containsStake(pos)) {
			return false;
		}
		if (within(pos, heart, HEART_RADIUS + STAKE_RADIUS)) {
			return true;
		}
		for (BlockPos stake : stakes) {
			if (within(pos, stake, STAKE_RADIUS * 2.0D)) {
				return true;
			}
		}
		return false;
	}

	public FaneFootprint withHeart(BlockPos newHeart) {
		return new FaneFootprint(newHeart, stakes, legacyCenter);
	}

	public FaneFootprint withoutHeart() {
		return new FaneFootprint(null, List.of(), legacyCenter);
	}

	public FaneFootprint withStake(BlockPos pos) {
		List<BlockPos> next = new ArrayList<>(stakes);
		if (!next.contains(pos)) {
			next.add(pos);
		}
		return new FaneFootprint(heart, next, legacyCenter);
	}

	public FaneFootprint withoutStake(BlockPos pos) {
		List<BlockPos> next = new ArrayList<>(stakes);
		next.remove(pos);
		return new FaneFootprint(heart, next, legacyCenter);
	}

	public boolean containsStake(BlockPos pos) {
		return stakes.contains(pos);
	}

	public static int stakeBudget(int playerMembers, int npcMembers) {
		return Math.min(MAX_STAKE_BUDGET, BASE_STAKE_BUDGET + Math.max(0, playerMembers) + Math.max(0, npcMembers));
	}

	private double maxDistanceFromHeart() {
		BlockPos center = heart != null ? heart : legacyCenter;
		if (center == null) {
			return 0.0D;
		}
		double max = HEART_RADIUS;
		for (BlockPos stake : stakes) {
			max = Math.max(max, Math.sqrt(distanceSqr(stake, center)) + STAKE_RADIUS);
		}
		return max;
	}

	private static boolean within(BlockPos pos, BlockPos center, double radius) {
		return distanceSqr(pos, center) <= radius * radius;
	}

	private static double distanceSqr(BlockPos pos, BlockPos center) {
		double dx = pos.getX() + 0.5D - (center.getX() + 0.5D);
		double dz = pos.getZ() + 0.5D - (center.getZ() + 0.5D);
		return dx * dx + dz * dz;
	}
}
