package com.vincenthuto.hemomancy.common.rite.sigil;

import com.vincenthuto.hemomancy.common.rite.harbinger.CardinalRiteAnchorVisualRules;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.LevelReader;

import java.util.List;
import java.util.function.IntPredicate;

public final class CardinalRiteSigilRules {
	public static final int FALSE_STROKE_BLOOD_ML = 5;
	public static final int FALSE_STROKE_INSTABILITY = 1;
	public static final int FALSE_STROKE_COOLDOWN_TICKS = 20;

	private CardinalRiteSigilRules() {}

	public enum StrokeDisposition {
		NONE,
		COMPLETED,
		EXPECTED,
		FALSE
	}

	public static String responseProgressKey(int wave, ResourceLocation id) {
		return "wave:" + wave + ":" + id;
	}

	public static int surfaceAirY(int baseY, IntPredicate solidAtY) {
		int highestSolid = Integer.MIN_VALUE;
		for (int y = baseY - 2; y <= baseY + 3; y++) {
			if (solidAtY.test(y)) highestSolid = y;
		}
		return highestSolid == Integer.MIN_VALUE ? baseY + 1 : highestSolid + 1;
	}

	public static BlockPos surfaceAirPosition(LevelReader level, BlockPos center, int offsetX, int offsetZ) {
		int x = center.getX() + offsetX;
		int z = center.getZ() + offsetZ;
		int surfaceY = surfaceAirY(center.getY(), y -> {
			BlockPos scan = new BlockPos(x, y, z);
			return level.getBlockState(scan).blocksMotion();
		});
		return new BlockPos(x, surfaceY, z);
	}

	public static int closestNodeIndex(List<BlockPos> nodeAirPositions, BlockPos target, double radius) {
		double maximumDistance = radius * radius;
		double closestDistance = maximumDistance;
		int closest = -1;
		for (int i = 0; i < nodeAirPositions.size(); i++) {
			BlockPos node = nodeAirPositions.get(i);
			double distance = Math.min(target.distSqr(node), target.distSqr(node.below()));
			if (distance < closestDistance) {
				closestDistance = distance;
				closest = i;
			}
		}
		return closest;
	}

	public static StrokeDisposition strokeDisposition(int touchedNode, int completedNodes) {
		if (touchedNode < 0) return StrokeDisposition.NONE;
		if (touchedNode < completedNodes) return StrokeDisposition.COMPLETED;
		if (touchedNode == completedNodes) return StrokeDisposition.EXPECTED;
		return StrokeDisposition.FALSE;
	}

	public static boolean isActionableNode(int touchedNode, int completedNodes, int totalNodes) {
		return touchedNode >= completedNodes && completedNodes < totalNodes;
	}

	public static List<Integer> raycastNodeIndices(int completedNodes, int totalNodes) {
		if (totalNodes <= 0 || completedNodes >= totalNodes) return List.of();
		List<Integer> indices = new java.util.ArrayList<>(totalNodes);
		for (int index = 0; index < totalNodes; index++) indices.add(index);
		return List.copyOf(indices);
	}

	public static int nodeCompletionStorageMl(int capacityMl, int totalNodes) {
		if (capacityMl <= 0 || totalNodes <= 0) return 0;
		return Math.min(50, (int) Math.ceil(capacityMl / (double) totalNodes));
	}

	public static float formingNodeRadius(int storedBloodMl) {
		return CardinalRiteAnchorVisualRules.formingSigilRadius(storedBloodMl, 50);
	}

	public static int falseStrokeBloodRequest(double projectionRate) {
		return Math.max(1, Math.min(FALSE_STROKE_BLOOD_ML, (int) Math.floor(projectionRate)));
	}

	public static boolean falseStrokePenaltyReady(int currentPhaseTick, int lastPenaltyPhaseTick) {
		return currentPhaseTick < lastPenaltyPhaseTick
				|| currentPhaseTick - lastPenaltyPhaseTick >= FALSE_STROKE_COOLDOWN_TICKS;
	}
}
