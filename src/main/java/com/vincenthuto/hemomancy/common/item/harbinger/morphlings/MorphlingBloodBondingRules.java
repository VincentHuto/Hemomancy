package com.vincenthuto.hemomancy.common.item.harbinger.morphlings;

public final class MorphlingBloodBondingRules {
	private static final double EPSILON = 0.0001D;

	public record Progress(int stage, double absorbed) {
	}

	private MorphlingBloodBondingRules() {
	}

	public static double requiredBlood(int maturity, double fledgling, double developing, double mature) {
		return switch (maturity) {
			case 1 -> Math.max(0.0D, fledgling);
			case 2 -> Math.max(0.0D, developing);
			case 3 -> Math.max(0.0D, mature);
			default -> 0.0D;
		};
	}

	public static Progress recordAbsorption(int storedStage, double storedAbsorbed, int currentStage,
			double actualDrain, double requiredBlood) {
		double base = storedStage == currentStage ? Math.max(0.0D, storedAbsorbed) : 0.0D;
		double next = base + Math.max(0.0D, actualDrain);
		if (requiredBlood > 0.0D) {
			next = Math.min(next, requiredBlood);
		}
		return new Progress(currentStage, next);
	}

	public static boolean isReady(int maturity, double absorbed, boolean passiveUpkeepEnabled,
			double fledgling, double developing, double mature) {
		if (!passiveUpkeepEnabled) {
			return true;
		}
		double required = requiredBlood(maturity, fledgling, developing, mature);
		return required <= 0.0D || Math.max(0.0D, absorbed) + EPSILON >= required;
	}

	public static int selectEnzymeSlots(double[] contributions, double neededPower) {
		if (contributions == null || contributions.length == 0 || neededPower <= 0.0D) {
			return 0;
		}
		int slotCount = Math.min(contributions.length, Integer.SIZE - 1);
		int bestMask = 0;
		double bestExcess = Double.POSITIVE_INFINITY;
		int bestItems = Integer.MAX_VALUE;
		for (int mask = 1; mask < (1 << slotCount); mask++) {
			double total = 0.0D;
			for (int slot = 0; slot < slotCount; slot++) {
				if ((mask & (1 << slot)) != 0) {
					total += Math.max(0.0D, contributions[slot]);
				}
			}
			if (total + EPSILON < neededPower) {
				continue;
			}
			double excess = total - neededPower;
			int items = Integer.bitCount(mask);
			if (excess + EPSILON < bestExcess
					|| (Math.abs(excess - bestExcess) <= EPSILON && items < bestItems)
					|| (Math.abs(excess - bestExcess) <= EPSILON && items == bestItems && mask < bestMask)) {
				bestMask = mask;
				bestExcess = excess;
				bestItems = items;
			}
		}
		return bestMask;
	}

	public static float nextEnzymeTarget(int maturity, float[] thresholds) {
		if (thresholds == null || maturity < 0 || maturity >= PrimalMorphlingRules.APEX_LEVEL
				|| maturity + 1 >= thresholds.length) {
			return -1.0F;
		}
		return thresholds[maturity + 1];
	}
}
