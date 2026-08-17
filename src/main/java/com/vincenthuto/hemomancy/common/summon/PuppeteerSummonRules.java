package com.vincenthuto.hemomancy.common.summon;

import java.util.UUID;

public final class PuppeteerSummonRules {
	public static final int THREAD_CAPACITY = 256;
	public static final int THREAD_PER_ITEM = 8;
	public static final double BASE_COMMAND_RANGE = 16.0;
	public static final double COMMAND_RANGE_PER_TETHER_LEVEL = 8.0;
	public static final int THREAD_CAPACITY_PER_BOUND_COMMAND_LEVEL = 32;
	public static final int CROSSBAR_DISMISSAL_TICKS = 100;
	public static final int DISMISSAL_GRACE_PER_BOUND_COMMAND_LEVEL = 20;
	public static final int CLAIMED_WILL_UPKEEP_PER_MINUTE = 16;
	public static final double MORPHLING_TETHER_RANGE_MULTIPLIER = 0.75;
	public static final double MORPHLING_THREAD_UPKEEP_MULTIPLIER = 1.5;

	private PuppeteerSummonRules() {
	}

	public static int projectedShapedCount(int currentShaped, int recalledMatching, int replacements) {
		return Math.max(0, currentShaped - Math.max(0, recalledMatching)) + Math.max(0, replacements);
	}

	public static boolean canUnlockAtDegree(PuppeteerSummonDefinition definition, int degree) {
		return definition != null && degree >= definition.requiredDegree();
	}

	public static int activeSummonCap(int puppetSkeinLevel) {
		return 1 + Math.max(0, puppetSkeinLevel);
	}

	public static boolean canRetainBody(boolean claimedWill, int keptTotal, int keptShapedBodies,
			int shapedBodyCap, int claimedWillBonusCap) {
		int shapedCap = Math.max(0, shapedBodyCap);
		int totalCap = shapedCap + Math.max(0, claimedWillBonusCap);
		return Math.max(0, keptTotal) < totalCap
				&& (claimedWill || Math.max(0, keptShapedBodies) < shapedCap);
	}

	public static double healthMultiplier(int livingSinewLevel) {
		return 1.0 + Math.max(0, livingSinewLevel) * 0.15;
	}

	public static double damageMultiplier(int livingSinewLevel) {
		return 1.0 + Math.max(0, livingSinewLevel) * 0.10;
	}

	public static double commandRange(int farTetherLevel) {
		return BASE_COMMAND_RANGE + Math.max(0, farTetherLevel) * COMMAND_RANGE_PER_TETHER_LEVEL;
	}

	public static double commandRange(int farTetherLevel, int boundCommandLevel) {
		return commandRange(farTetherLevel) + Math.max(0, boundCommandLevel) * 4.0;
	}

	public static double effectiveCommandRange(int farTetherLevel, int boundCommandLevel,
			boolean morphlingEquipped) {
		double range = commandRange(farTetherLevel, boundCommandLevel);
		return morphlingEquipped ? range * MORPHLING_TETHER_RANGE_MULTIPLIER : range;
	}

	public static int threadCapacity(int boundCommandLevel) {
		return THREAD_CAPACITY + Math.max(0, boundCommandLevel) * THREAD_CAPACITY_PER_BOUND_COMMAND_LEVEL;
	}

	public static int threadChargeFromItems(int itemCount) {
		return Math.max(0, itemCount) * THREAD_PER_ITEM;
	}

	public static int threadItemsAccepted(int availableChargeSpace, int availableItems) {
		return Math.min(Math.max(0, availableItems), Math.max(0, availableChargeSpace) / THREAD_PER_ITEM);
	}

	public static int refilledThread(int currentThread, int addedThread) {
		return refilledThread(currentThread, addedThread, 0);
	}

	public static int refilledThread(int currentThread, int addedThread, int boundCommandLevel) {
		return Math.min(threadCapacity(boundCommandLevel), Math.max(0, currentThread) + Math.max(0, addedThread));
	}

	public static int clampThreadToCapacity(int currentThread, int capacity) {
		return Math.min(Math.max(0, currentThread), Math.max(1, capacity));
	}

	public static double threadCostMultiplier(int threadEconomyLevel) {
		return Math.max(0.7, 1.0 - Math.max(0, threadEconomyLevel) * 0.05);
	}

	public static int adjustedThreadCost(int baseCost, int threadEconomyLevel) {
		return Math.max(1, (int) Math.ceil(Math.max(0, baseCost) * threadCostMultiplier(threadEconomyLevel)));
	}

	public static int interferedThreadUpkeep(int adjustedUpkeep, boolean morphlingEquipped) {
		if (!morphlingEquipped) {
			return Math.max(0, adjustedUpkeep);
		}
		return (int) Math.ceil(Math.max(0, adjustedUpkeep) * MORPHLING_THREAD_UPKEEP_MULTIPLIER);
	}

	public static int dismissalGraceTicks(int boundCommandLevel) {
		return CROSSBAR_DISMISSAL_TICKS + Math.max(0, boundCommandLevel) * DISMISSAL_GRACE_PER_BOUND_COMMAND_LEVEL;
	}

	public static double dismissalAlpha(int remainingDismissalTicks, float partialTick) {
		if (remainingDismissalTicks <= 0) {
			return 1.0;
		}
		double remaining = Math.max(0.0, remainingDismissalTicks - Math.max(0.0F, partialTick));
		return Math.max(0.0, Math.min(1.0, remaining / CROSSBAR_DISMISSAL_TICKS));
	}

	public static boolean shouldRenderDismissingSummon(int tickCount, int remainingDismissalTicks) {
		if (remainingDismissalTicks <= 0) {
			return true;
		}
		double alpha = dismissalAlpha(remainingDismissalTicks, 0.0F);
		int cycle = 8;
		int visibleTicks = Math.max(1, (int) Math.ceil(cycle * alpha));
		return Math.floorMod(tickCount, cycle) < visibleTicks;
	}

	public static boolean canAttuneCrossbar(UUID currentOwner, UUID player) {
		return player != null && (currentOwner == null || currentOwner.equals(player));
	}

	public static long nextUpkeepGameTime(long currentGameTime) {
		return Math.max(0L, currentGameTime) + 1200L;
	}

	public static boolean upkeepDue(long currentGameTime, long nextUpkeepGameTime) {
		return nextUpkeepGameTime > 0L && currentGameTime >= nextUpkeepGameTime;
	}

	public static boolean shouldUnravelForDimension(boolean ownerInSameDimension) {
		return !ownerInSameDimension;
	}

	public static boolean qualifiesForMorphlingInterference(boolean alive, boolean loaded,
			boolean ownerBound, boolean trialSummon, boolean sameDimension, boolean ownerSessionMatches) {
		return alive && loaded && ownerBound && !trialSummon && sameDimension && ownerSessionMatches;
	}

	public static boolean shouldDespawnInPeaceful(boolean trialSummon, UUID ownerUuid) {
		return trialSummon || ownerUuid == null;
	}

	public static boolean withinTetherRange(double distanceSquared, double tetherRange) {
		double range = Math.max(0.0, tetherRange);
		return distanceSquared <= range * range;
	}
}
