package com.vincenthuto.hemomancy.common.event.worldevent;

import com.vincenthuto.hemomancy.common.world.LobeEnvelope;
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
		if (LobeEnvelope.contains(heart, stakes, pos, HEART_RADIUS, STAKE_RADIUS, LobeEnvelope.UNBOUNDED)) {
			return true;
		}
		return heart == null && legacyCenter != null
				&& LobeEnvelope.inLobe(pos, legacyCenter, HEART_RADIUS, LobeEnvelope.UNBOUNDED);
	}

	public double effectStrength(BlockPos pos) {
		BlockPos center = heart != null ? heart : legacyCenter;
		if (center == null || !contains(pos)) {
			return 0.0D;
		}
		return LobeEnvelope.strength(center, stakes, pos, HEART_RADIUS, STAKE_RADIUS, 0.65D);
	}

	public boolean canAddStake(BlockPos pos, int budget) {
		return LobeEnvelope.canAddLobe(heart, stakes, pos, HEART_RADIUS, STAKE_RADIUS,
				Math.min(budget, MAX_STAKE_BUDGET), LobeEnvelope.UNBOUNDED);
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

	/** The heart or stake a stake chains back through; null for a stake cut off by a removal (it keeps working). */
	public BlockPos chainedFrom(BlockPos stake) {
		return LobeEnvelope.chainedFrom(heart, stakes, stake, HEART_RADIUS, STAKE_RADIUS, LobeEnvelope.UNBOUNDED);
	}

	/** The widest reach from the heart: its own radius or the far edge of its farthest stake. */
	public double maxRadius() {
		return LobeEnvelope.maxRadius(heart, stakes, HEART_RADIUS, STAKE_RADIUS);
	}

	public boolean containsStake(BlockPos pos) {
		return stakes.contains(pos);
	}

	public static int stakeBudget(int playerMembers, int npcMembers) {
		return Math.min(MAX_STAKE_BUDGET, BASE_STAKE_BUDGET + Math.max(0, playerMembers) + Math.max(0, npcMembers));
	}
}
