package com.vincenthuto.hemomancy.common.summon;

public final class PuppeteerSummonRules {
	public static final int THREAD_CAPACITY = 256;
	public static final int THREAD_PER_ITEM = 1;
	public static final double BASE_COMMAND_RANGE = 16.0;
	public static final double COMMAND_RANGE_PER_TETHER_LEVEL = 8.0;
	public static final int THREAD_CAPACITY_PER_BOUND_COMMAND_LEVEL = 32;
	public static final int CROSSBAR_DISMISSAL_TICKS = 100;
	public static final int DISMISSAL_GRACE_PER_BOUND_COMMAND_LEVEL = 20;

	private PuppeteerSummonRules() {
	}

	public static boolean canUnlockAtDegree(PuppeteerSummonDefinition definition, int degree) {
		return definition != null && degree >= definition.requiredDegree();
	}

	public static int activeSummonCap(int puppetSkeinLevel) {
		return 1 + Math.max(0, puppetSkeinLevel);
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

	public static int threadCapacity(int boundCommandLevel) {
		return THREAD_CAPACITY + Math.max(0, boundCommandLevel) * THREAD_CAPACITY_PER_BOUND_COMMAND_LEVEL;
	}

	public static int refilledThread(int currentThread, int addedThread) {
		return refilledThread(currentThread, addedThread, 0);
	}

	public static int refilledThread(int currentThread, int addedThread, int boundCommandLevel) {
		return Math.min(threadCapacity(boundCommandLevel), Math.max(0, currentThread) + Math.max(0, addedThread));
	}

	public static double threadCostMultiplier(int threadEconomyLevel) {
		return Math.max(0.7, 1.0 - Math.max(0, threadEconomyLevel) * 0.05);
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
}
